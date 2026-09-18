#!/usr/bin/env bash
# Builds scalino-linkdriver: a standalone binary wrapping scala-native's
# tools_3 library (NIR -> LLVM IR -> clang -> native executable). Self-hosted
# like the rest of the toolchain: dotc compiles LinkDriver.scala to NIR, then
# LinkDriver links itself (run on the JVM, same bootstrap step 03/08 reuse).
#
# Uses tools-patched.cp (04a-patch-tools.sh): fixes scala-native's
# object-file caching for vendored C/S dependencies, which was otherwise
# effectively always-recompile regardless of whether anything changed --
# see docs/findings.md "Native-library object-file caching was inert".
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"
source ./00-env.sh

[[ -f "$WORK/tools-patched.cp" ]] || { echo "run 04a-patch-tools.sh first" >&2; exit 1; }
[[ -f "$WORK/tools-patched-jvm.cp" ]] || { echo "run 04a-patch-tools.sh first" >&2; exit 1; }

# tools-patched-jvm.cp (04a-patch-tools.sh), NOT the raw tools.cp: LinkDriver
# is compiled (below) against the *native*-targeted patched classpath
# (NATIVE_DRIVER_CP), so its bytecode can reference e.g.
# NativeConfig.withLLVMDirectCodeGen -- but it then actually *runs* here, on
# a plain JVM, against DRIVER_CP. Those two classpaths must agree on
# NativeConfig's shape or this throws NoSuchMethodError at runtime for
# everyone, not just users of that experimental flag.
DRIVER_CP="$(cat "$WORK/compiler.cp")$CP_SEP$(cat "$WORK/tools-patched-jvm.cp")$CP_SEP$(to_native_path "$WORK/driver-classes")"

# Substitute the published javalib_native0.5_3 jar for the locally-built one
# with patches/scala-native-0009 (ZipFileSystemProvider) actually applied --
# see build/01b-build-patched-javalib.sh. This classpath becomes the actual
# runtime javalib baked into the linked scalino-linkdriver binary itself (not
# just this build step's own tooling), which is what determines whether the
# jar classpath entries LinkDriver hands to `Build` at RUNTIME (any real
# `scalino build`, not just this bootstrap) can be read via NIO's "jar:"
# FileSystemProvider instead of needing extractJarIfNeeded's workaround.
TOOLS_PATCHED_JAVALIB_CP="$WORK/tools-patched-javalib.cp"
LOCAL_JAVALIB_JAR="$HOME/.ivy2/local/org.scala-native/javalib_native0.5_3/${SCALA_NATIVE_VERSION}-SNAPSHOT/jars/javalib_native0.5_3.jar"
if [[ -f "$LOCAL_JAVALIB_JAR" ]]; then
  { tr "$CP_SEP" '\n' < "$WORK/tools-patched.cp" | grep -v '/javalib_native0\.5_3-'; echo "$LOCAL_JAVALIB_JAR"; } | paste -sd"$CP_SEP" - > "$TOOLS_PATCHED_JAVALIB_CP"
  echo "  using locally-built, patched javalib jar: $LOCAL_JAVALIB_JAR"
else
  cp "$WORK/tools-patched.cp" "$TOOLS_PATCHED_JAVALIB_CP"
  echo "  WARNING: locally-built javalib jar not found ($LOCAL_JAVALIB_JAR) -- run build/01b-build-patched-javalib.sh first, or patches/scala-native-0009 (ZipFileSystemProvider) will NOT take effect and scalino-linkdriver will fall back to needing jar-extraction workarounds. See docs/findings.md."
fi

NATIVE_DRIVER_CP="$(to_native_path "$WORK/driver-classes")$CP_SEP$(cat "$TOOLS_PATCHED_JAVALIB_CP")"

NSCPLUGIN_JAR="$(cat "$WORK/nscplugin.jar.txt")"

rm -rf "$WORK/driver-classes"
mkdir -p "$WORK/driver-classes"
"$JAVA" -cp "$DRIVER_CP" dotty.tools.dotc.Main \
  -Xplugin:"$NSCPLUGIN_JAR" \
  -Xplugin-require:scalanative \
  -classpath "$NATIVE_DRIVER_CP" \
  -d "$WORK/driver-classes" \
  "$ROOT/src/LinkDriver.scala"

LINK_WORK="$WORK/driver-link"
rm -rf "$LINK_WORK"
mkdir -p "$LINK_WORK"

# EXPERIMENTAL (NativeConfig.useLLVMDirectCodeGen): best-effort, optional --
# if a compatible libLLVM is discoverable, link it straight into
# scalino-linkdriver itself so the @extern LLVM-C bindings compiled into its
# NIR (tools/native/.../codegen/llvm/direct/LLVMCApi.scala) resolve. This is
# separate from -- and does not require -- the flag actually being passed to
# *this* bootstrap build (which always runs the JVM-hosted tools_3, where the
# feature is a no-op); it only affects whether the resulting binary can use
# the feature later, at real `scalino build --experimental-direct-codegen`
# time. Never fails the build: if llvm-config/libLLVM aren't found, or the
# version is too old, the flag stays silently unavailable at runtime.
LLVM_DIRECT_CODEGEN_LINKING_OPTS=()
if command -v llvm-config >/dev/null 2>&1; then
  LLVM_CONFIG_VERSION="$(llvm-config --version 2>/dev/null || true)"
  LLVM_CONFIG_MAJOR="${LLVM_CONFIG_VERSION%%.*}"
  LLVM_CONFIG_LIBDIR="$(llvm-config --libdir 2>/dev/null || true)"
  if [[ "${LLVM_CONFIG_MAJOR:-0}" =~ ^[0-9]+$ ]] && (( LLVM_CONFIG_MAJOR >= 13 )) \
      && [[ -n "$LLVM_CONFIG_LIBDIR" && -d "$LLVM_CONFIG_LIBDIR" ]]; then
    # Prefer whatever libLLVM.* the libdir actually contains (versioned or
    # not) over guessing a name -- naming conventions vary a lot per distro.
    LLVM_LIB_NAME="$(
      ls "$LLVM_CONFIG_LIBDIR"/libLLVM.* "$LLVM_CONFIG_LIBDIR"/libLLVM-*.* 2>/dev/null \
        | head -n1 \
        | sed -E 's|.*/lib(LLVM[^./]*)\..*$|\1|'
    )"
    if [[ -n "$LLVM_LIB_NAME" ]]; then
      LLVM_DIRECT_CODEGEN_LINKING_OPTS=(--linking "-L$LLVM_CONFIG_LIBDIR" --linking "-l$LLVM_LIB_NAME")
      echo "  useLLVMDirectCodeGen: linking scalino-linkdriver against -l$LLVM_LIB_NAME ($LLVM_CONFIG_LIBDIR)"
    fi
  fi
fi
if [[ "${#LLVM_DIRECT_CODEGEN_LINKING_OPTS[@]}" -eq 0 ]]; then
  echo "  useLLVMDirectCodeGen: no compatible libLLVM found, feature will be unavailable at runtime (this is fine, it's opt-in)"
fi

"$JAVA" \
  -cp "$DRIVER_CP" \
    LinkDriver \
    "$(to_native_path "$NATIVE_DRIVER_CP")" \
    "$(to_native_path "$LINK_WORK")" \
    LinkDriver \
    "$CLANG" \
    "$CLANGPP" \
    info \
    --mode release-size \
    "${LLVM_DIRECT_CODEGEN_LINKING_OPTS[@]+"${LLVM_DIRECT_CODEGEN_LINKING_OPTS[@]}"}"

BUILT="$LINK_WORK/LinkDriver"
[[ -f "$BUILT" ]] || BUILT="$LINK_WORK/LinkDriver.exe"
[[ -f "$BUILT" ]] || { echo "link did not produce $BUILT" >&2; exit 1; }
cp "$BUILT" "$DIST/scalino-linkdriver"
chmod +x "$DIST/scalino-linkdriver"

echo "OK: $DIST/scalino-linkdriver"
