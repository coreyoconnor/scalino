# heap-viewer

Interactive treemap of a scalino program's live heap, grouped by **retained
size** (an object that points at an array counts that array's bytes too,
correctly attributed via a dominator tree instead of double-counted).

## Usage

1. Run your scalino-compiled program with:
   ```
   SCALANATIVE_HEAP_GRAPH_FILE=/tmp/heap.graph ./your-program
   ```
   By default this OVERWRITES the file with just the most recent GC mark
   cycle's snapshot (bounded size, regardless of how long the program runs
   or how many collections happen) -- set `SCALANATIVE_HEAP_GRAPH_APPEND=1`
   to instead append every cycle as a real time series. Zero-cost when
   `SCALANATIVE_HEAP_GRAPH_FILE` is unset.

   **The actual filename always gets the process's PID inserted**, e.g.
   `heap.graph` becomes `heap.24717.graph` -- this matters because a single
   `scalino build`/`run`/`package` invocation spawns more than one
   scala-native process (`scalino-dotc`, `scalino-linkdriver`, ...), and
   without per-process filenames they'd all race writes to the same path
   (same class of bug as multiple processes sharing one core-dump or JFR
   file). Use `%p` in the path to control exactly where the PID goes (e.g.
   `/tmp/dumps/%p/heap.graph`); without it, the PID is auto-inserted right
   before the file extension. Each process prints the exact resolved path
   it's writing to on stderr (`[scalino GC] heap graph dump: ...`) --
   watch the build output to see which file is which.
2. Open `index.html` in a browser (no server needed).
3. Load the dump file, pick a cycle, and explore. Click a box to zoom into
   it; use the prefix filter to highlight matching classes.

   On a real toolchain-scale heap (a few million objects/edges) the
   dominator-tree analysis can take several seconds -- a progress line
   shows what's happening instead of the page looking frozen.

## Format

Produced by `gc/immix/Marker.c` (`GraphDump_*`), one block per mark cycle:

```
--- cycle=N wall_seconds=S ---
O,<addr_hex>,<size_bytes>,<is_array 0|1>,<class_name>
E,<from_addr_hex>,<to_addr_hex>
R,<addr_hex>                      (directly reachable from a root)
--- end cycle=N total_objects=X total_bytes=Y ---
```
