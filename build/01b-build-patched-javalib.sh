#!/usr/bin/env bash
# Builds a patched org.scala-native:javalib_native0.5_3 jar via scala-native's
# own real sbt build, so patches/scala-native-0003 (UnixPath/WindowsPath#toUri
# missing "//" authority -- broke scalino-lsp's definition/references URIs)
# actually takes effect. Without this, scalino-dotc/scalino-lsp link against
# the published (unpatched) jar and that fix is silently inert -- see
# docs/findings.md's "Self-hosting scalino-lsp" javalib section for the full
# story of why a direct from-source recompile (bypassing sbt, the trick used
# everywhere else in this pipeline) turned out not to be tractable here.
#
# sbt's own build compiles against the PUBLISHED scala3-compiler jar --
# patches/scala-native-0001/0002 (nscplugin/tools changes that only compile
# against THIS project's self-hosted dotc) make that fail, so they're
# reverted for the duration of this one build and restored right after,
# success or failure.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"
source ./00-env.sh

require sbt

VENDOR="$ROOT/vendor/scala-native"
[[ -d "$VENDOR/.git" ]] || { echo "missing $VENDOR -- run 00b-setup-vendor.sh first" >&2; exit 1; }

cd "$VENDOR"
REVERTED=()
for p in "$ROOT"/patches/scala-native-000{1,2}-*.patch; do
  [[ -e "$p" ]] || continue
  git apply -R "$p"
  REVERTED+=("$p")
done

# cd explicitly here (not relying on the caller's cwd at trap-fire time --
# an EXIT trap runs after the script's own `cd - ` below has already left
# $VENDOR, and a failing `git apply` inside a trap does NOT flip the
# script's exit code, so a cwd mistake here would silently leave the repo
# unpatched while still reporting success).
restore() {
  cd "$VENDOR"
  for p in "${REVERTED[@]}"; do
    git apply "$p"
  done
}
trap restore EXIT

# Confirmed via CI (only ever on a real tag push, e.g. our own release.yml):
# scala-native's own project/ScalaNativeBuildInfo.scala reads GITHUB_REF_TYPE/
# GITHUB_REF_NAME (meant for THEIR OWN release CI, asserting the pushed tag
# equals their own pinned version) -- sbt inherits our full environment, so
# our own tag ("v0.0.1") leaks in and trips their assertion ("tag does not
# match expected version") even though we're only vendoring their source, not
# cutting a scala-native release ourselves. Unset for this subprocess only,
# falling their version logic through to the ordinary CI-snapshot branch.
#
# Also unset CI itself: that same ScalaNativeBuildInfo.scala has a separate
# branch for `envOrNone("CI").isDefined` that stamps the published version as
# "$baseVersion-<commitDate>-<gitHash>-SNAPSHOT" instead of the plain
# "$baseVersion-SNAPSHOT" our build scripts (03/04/08) hardcode via
# $SCALA_NATIVE_VERSION-SNAPSHOT when locating this jar. On a real CI runner
# (GitHub Actions sets CI=true) that mismatch made every LOCAL_JAVALIB_JAR
# check silently miss, so patches/scala-native-0009 (ZipFileSystemProvider)
# never made it into scalino-linkdriver's own build there -- fine as long as
# the old jar-extraction workaround covered for it, fatal once that workaround
# was removed in favor of the patch actually taking effect (see release
# v0.0.3's CI failure). Works locally already since CI is normally unset.
env -u GITHUB_REF_TYPE -u GITHUB_REF_NAME -u GITHUB_REF -u CI sbt javalib3/publishLocal
cd - > /dev/null

echo "OK: patched javalib published to ~/.ivy2/local/org.scala-native/javalib_native0.5_3/${SCALA_NATIVE_VERSION}-SNAPSHOT/"
