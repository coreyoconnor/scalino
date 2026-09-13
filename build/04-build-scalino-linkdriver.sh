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

DRIVER_CP="$(cat "$WORK/compiler.cp")$CP_SEP$(cat "$WORK/tools.cp")$CP_SEP$(to_native_path "$WORK/driver-classes")"

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

"$JAVA" \
  -cp "$DRIVER_CP" \
    LinkDriver \
    "$(to_native_path "$NATIVE_DRIVER_CP")" \
    "$(to_native_path "$LINK_WORK")" \
    LinkDriver \
    "$CLANG" \
    "$CLANGPP" \
    info \
    --mode release-size

BUILT="$LINK_WORK/LinkDriver"
[[ -f "$BUILT" ]] || BUILT="$LINK_WORK/LinkDriver.exe"
[[ -f "$BUILT" ]] || { echo "link did not produce $BUILT" >&2; exit 1; }
cp "$BUILT" "$DIST/scalino-linkdriver"
chmod +x "$DIST/scalino-linkdriver"

echo "OK: $DIST/scalino-linkdriver"
