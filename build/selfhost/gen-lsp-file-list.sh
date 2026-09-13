#!/usr/bin/env bash
# Reproduces the source-file list scalino-lsp's FRONTEND compile is
# self-hosted from -- used by build/08-build-scalino-lsp.sh. Backend is
# scala3-presentation-compiler (Metals' own PC engine), via a thin
# `PcLanguageServer` layer over the same hand-rolled JSON-RPC transport the
# old pre-Metals `DottyLanguageServer` backend used (removed, see
# patches/scala3-0017-lsp-pc-backend-switch.patch) -- extends
# gen-file-list.sh's dotc list with the LSP transport (Lsp.scala/Main.scala),
# scala3-presentation-compiler's own source, its mtags-shared port, and the
# small dotty.tools.languageserver-package glue (PcLanguageServer/BuildInfo)
# -- everything EXCEPT lsp-shim/lsp4j and lsp-shim/mtags-interfaces, which
# compile in a SEPARATE invocation against the real lsp4j/mtags-interfaces
# jars on this list's classpath instead (see build/08-build-scalino-lsp.sh:
# dotc's auto-application leniency for parenless method calls only applies
# to genuine Java-defined symbols, which a fresh Scala port compiled jointly
# with presentation-compiler's real source never is).
#
# Usage: build/selfhost/gen-lsp-file-list.sh > /tmp/lsp-file-list.txt
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

./gen-file-list.sh

ROOT="$(cd .. && cd .. && pwd)"

# LSP transport: stdio framing/dispatch (Main.scala) + hand-written
# jsoniter-scala wire model (Lsp.scala) -- no lsp4j.
echo "$ROOT/vendor/scala3/language-server/src/dotty/tools/languageserver/Lsp.scala"
echo "$ROOT/vendor/scala3/language-server/src/dotty/tools/languageserver/Main.scala"

# presentation-compiler's own ~59 files -- confirmed via recon: zero direct
# ASM/gson/guava usage (those live only in mtags-shared, handled separately),
# no source edits needed at all.
find "$ROOT/vendor/scala3/presentation-compiler/src/main" -name "*.scala"

# dotc's own real dotty.tools.dotc.semanticdb package (37 files) -- excluded
# from gen-file-list.sh's dotc list as "tooling-only" for scalino-dotc, but
# presentation-compiler's SemanticdbSymbols.scala/
# SemanticdbTextDocumentProvider.scala genuinely call into it
# (symbolsFromName/symbolToName/textDocumentBytes), and lsp-shim/mtags-shared's
# Compression.scala substitutes its own SemanticdbInputStream/OutputStream
# for real protobuf's CodedInputStream/OutputStream (the same substitution
# Metals' own sbt build applies, `replaceProtobuf` in
# vendor/scala3/project/Build.scala). Confirmed self-contained via import
# scan: only dotty.tools.dotc.*/java.io/java.nio/java.security/java.util,
# no real protobuf jar, no ASM.
find "$ROOT/vendor/scala3/compiler/src/dotty/tools/dotc/semanticdb" -name "*.scala"

# mtags-shared, ported+patched (bloom filter, protobuf substitution, Gson
# codecs) -- see lsp-shim/mtags-shared/'s own files for the fix points.
find "$ROOT/lsp-shim/mtags-shared" -name "*.scala"

# New glue living in dotty.tools.(languageserver|pc.buildinfo) package names
# but with no upstream-scala3 identity -- see lsp-shim/'s top-level files.
echo "$ROOT/lsp-shim/BuildInfo.scala"
echo "$ROOT/lsp-shim/PcLanguageServer.scala"
