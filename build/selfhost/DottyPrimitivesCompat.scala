package dotty.tools.backend.jvm

// Scala 3.9 renamed+moved `dotty.tools.backend.jvm.DottyPrimitives` to
// `dotty.tools.backend.ScalaPrimitives` (same shape, same one-arg Context
// constructor). vendor/scala-native's own nscplugin source
// (nscplugin/src/main/scala-3/scala/scalanative/nscplugin/NirPrimitives.scala)
// still imports/extends the old name unpatched -- deliberately: it's the
// exact same file scala-native's own internal sbt build compiles against
// its own separately-pinned, pre-3.9 dotty (see build/01b-build-patched-javalib.sh),
// where the *old* name is still the real one. Patching that file directly
// would break that other build. This same-named, same-shape forwarding
// shim lets that one unmodified nscplugin file resolve under *our* real
// Scala 3.9 compile too, without needing two divergent copies of it.
// See build/selfhost/gen-file-list.sh.
class DottyPrimitives(ctx: dotty.tools.dotc.core.Contexts.Context)
    extends dotty.tools.backend.ScalaPrimitives(ctx)
