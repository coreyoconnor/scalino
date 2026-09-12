package scala.meta.internal.metals

import scala.language.unsafeNulls

/**
 * A wrapper around a bloom filter that is optimized for fast insertions of
 * strings with shared prefixes.
 *
 * To index a classpath, Metals walks through all possible prefixes of a given
 * classfile path. For example, given the string "InputStream.class" Metals
 * builds a bloom filter containing the set of the following strings: I, In,
 * Inp, Inpu, Input, S, St, Str, Stre, Strea and Stream.
 *
 * The naive approach to construct a bloom filter of all those prefix strings
 * is to create a `BloomFilter[CharSequence]` and insert all those prefixes.
 * This approach has sub-optimal performance because it requires a quadratic
 * number of iterations on the characters of the classfile path.
 *
 * This class implements an optimized approach to build that bloom filter in a
 * way that requires only a linear pass on the characters of the classfile
 * path. The trick is to incrementally build the hashcode of each prefix
 * string as we iterate over each character in the string.
 *
 * Additionally, this class exposes a `mightContain(Long)` method that speeds
 * up search queries by allowing the client to pre-compute the hash of the
 * query string and re-use `mightContain` calls to multiple bloom filters.
 *
 * Plain-stdlib reimplementation (no guava, no lz4/xxhash): a standard
 * bit-array bloom filter sized from `estimatedSize`/`maxFalsePositiveRatio`
 * via the textbook `m`/`k` formulas, using the Kirsch-Mitzenmacher trick to
 * derive `k` sub-hashes from a single 64-bit hash (so only one real hash
 * needs to be computed/streamed per string). The 64-bit hash itself is an
 * incremental FNV-1a (chosen because, like xxhash's streaming API this
 * replaces, it can be updated byte-by-byte and only needs the running
 * accumulator as state).
 */
class StringBloomFilter(estimatedSize: Int) {
  val maxFalsePositiveRatio = 0.01

  private val n = math.max(1, estimatedSize)
  private val numBits: Int = {
    val m = math.ceil(-(n.toDouble * math.log(maxFalsePositiveRatio)) / (math.log(2) * math.log(2)))
    math.max(64, m.toInt)
  }
  private val numHashes: Int =
    math.max(1, math.round((numBits.toDouble / n) * math.log(2)).toInt)

  private val bits = new java.util.BitSet(numBits)
  private var bitsSetCount = 0

  def isFull: Boolean = currentFalsePositiveRatio > maxFalsePositiveRatio

  private def currentFalsePositiveRatio: Double = {
    val ratio = bitsSetCount.toDouble / numBits
    math.pow(ratio, numHashes)
  }

  private val FnvOffsetBasis = 0xcbf29ce484222325L
  private val FnvPrime = 0x100000001b3L

  private var hash: Long = FnvOffsetBasis

  /**
   * Resets the hash value.
   */
  def reset(): Unit = hash = FnvOffsetBasis

  /**
   * Returns the current hash value.
   */
  def value(): Long = hash

  private def updateHashCode(char: Char): Unit = {
    hash = (hash ^ (char & 0xff)) * FnvPrime
    hash = (hash ^ ((char >> 8) & 0xff)) * FnvPrime
  }

  private def put(h: Long): Boolean = {
    val h1 = (h >>> 32)
    val h2 = (h & 0xffffffffL)
    var changed = false
    var i = 0
    while (i < numHashes) {
      val combined = h1 + i.toLong * h2
      val idx = (((combined % numBits) + numBits) % numBits).toInt
      if (!bits.get(idx)) {
        bits.set(idx)
        bitsSetCount += 1
        changed = true
      }
      i += 1
    }
    changed
  }

  private def mightContainHash(h: Long): Boolean = {
    val h1 = (h >>> 32)
    val h2 = (h & 0xffffffffL)
    var i = 0
    var result = true
    while (i < numHashes && result) {
      val combined = h1 + i.toLong * h2
      val idx = (((combined % numBits) + numBits) % numBits).toInt
      if (!bits.get(idx)) result = false
      i += 1
    }
    result
  }

  /**
   * Inserts a new string to the bloom filter that is the concatenation of the
   * current hash value and the given character.
   *
   * Use this method when inserting multiple strings that share the same prefix,
   * for example to insert all prefixes of "Simple" you can do
   * {{{
   *   putCharIncrementally('S') // insert S
   *   putCharIncrementally('i') // insert Si
   *   putCharIncrementally('m') // insert Sim
   *   putCharIncrementally('p') // insert Simp
   *   putCharIncrementally('l') // insert Simpl
   *   putCharIncrementally('e') // insert Simple
   * }}}
   */
  def putCharIncrementally(char: Char): Boolean = {
    updateHashCode(char)
    put(value())
  }

  /**
   * Insert a single string into the bloom filter.
   */
  def putCharSequence(chars: CharSequence): Boolean = {
    put(computeHashCode(chars))
  }

  /**
   * Computes the hascode of a single string that can be later passed to `mightContain(Long)`.
   */
  def computeHashCode(chars: CharSequence): Long = {
    reset()
    var i = 0
    val N = chars.length()
    while (i < N) {
      updateHashCode(chars.charAt(i))
      i += 1
    }
    value()
  }

  /**
   * Returns true if the bloom filter contains the given string.
   */
  def mightContain(chars: CharSequence): Boolean = {
    mightContainHash(computeHashCode(chars))
  }

  /**
   * Returns true if the bloom filter contains the string with the given hashcode.
   *
   * This method is can help improve performance when calling `mightContain`
   * with the same query string to multiple different bloom filters.
   */
  def mightContain(hashCode: Long): Boolean = {
    mightContainHash(hashCode)
  }

  def approximateElementCount(): Long = {
    // Standard bloom-filter cardinality estimator: -(m/k) * ln(1 - X/m)
    val ratio = bitsSetCount.toDouble / numBits
    if (ratio >= 1.0) Long.MaxValue
    else math.round(-(numBits.toDouble / numHashes) * math.log(1.0 - ratio))
  }

}
