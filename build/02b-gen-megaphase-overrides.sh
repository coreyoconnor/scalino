#!/usr/bin/env bash
# Generates MiniPhaseOverrides.scala: a build-time lookup table that
# replaces MegaPhase.defines' runtime `Class#getDeclaredMethods` reflection.
# Scala Native has no general reflection API (only the narrow, per-class
# opt-in @EnableReflectiveInstantiation) -- see docs/findings.md "Blocker A"
# and the doc comment on `defines` in vendor/scala3's patched MegaPhase.scala
# (patches/scala3-0003-*.patch). This tool computes the exact same fact
# (which of MiniPhase's overridable methods each concrete subclass
# redefines) using real JVM reflection, once, offline, over the same
# classpath scalino-dotc's self-hosted build bakes in.
#
# Must run after 01-fetch-deps.sh (needs compiler.cp/nscplugin.cp) and
# before 03-build-scalino-dotc.sh/08-build-scalino-lsp.sh, which compile
# this generated file in as part of dotc's own self-hosted source set (see
# build/selfhost/gen-file-list.sh).
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"
source ./00-env.sh

[[ -f "$WORK/compiler.cp" ]] || { echo "run 01-fetch-deps.sh first" >&2; exit 1; }
[[ -f "$WORK/nscplugin.cp" ]] || { echo "run 01-fetch-deps.sh first" >&2; exit 1; }
PATCHED_CLASSES="$WORK/selfhost-bootstrap/classes"
[[ -d "$PATCHED_CLASSES" ]] || { echo "missing $PATCHED_CLASSES -- run build/02a-build-compiler-patched.sh first" >&2; exit 1; }

GEN_DIR="$WORK/generated"
TOOL_CLASSES="$WORK/gen-tool-classes"
mkdir -p "$GEN_DIR" "$TOOL_CLASSES"

"$JAVAC" -d "$TOOL_CLASSES" tools/GenMiniPhaseOverrides.java
# $PATCHED_CLASSES listed first: it holds classes compiled from this
# project's own patched vendor/scala3+scala-native source (build/
# 02a-build-compiler-patched.sh) -- for any class name that also exists in
# the published jars below, URLClassLoader resolves from the first URL that
# defines it, so this ensures the *patched* shape wins, and it's the only
# place patch-added classes (no published-jar equivalent at all, e.g.
# transform/CollectEntryPoints.scala) are found in the first place.
"$JAVA" -cp "$TOOL_CLASSES" GenMiniPhaseOverrides \
  "$PATCHED_CLASSES" "$(cat "$WORK/compiler.cp")" "$(cat "$WORK/nscplugin.cp")" \
  "$GEN_DIR/MiniPhaseOverrides.scala"

echo "OK: $GEN_DIR/MiniPhaseOverrides.scala"
