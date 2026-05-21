private fun assertEquals(expected: Any?, actual: Any?, name: String) {
    if (expected != actual) {
        throw AssertionError("[$name] expected: $expected, actual: $actual")
    }
    println("  OK - $name")
}

private fun snapshot(fenwick: RangeUpdateFenwick, n: Int): List<Long> =
    (1..n).map { fenwick.pointQuery(it) }

private fun initialArrayIsAllZeros() {
    println("Test: initial array is all zeros")
    val n = 8
    val fenwick = RangeUpdateFenwick(n)
    assertEquals(List(n) { 0L }, snapshot(fenwick, n), "initial state")
}

private fun rangeUpdateAppliesDeltaToRange() {
    println("Test: rangeUpdate(2, 5, +3)")
    val n = 8
    val fenwick = RangeUpdateFenwick(n)
    fenwick.rangeUpdate(2, 5, 3)
    val expected = listOf(0L, 3L, 3L, 3L, 3L, 0L, 0L, 0L)
    assertEquals(expected, snapshot(fenwick, n), "after +3 on [2..5]")
}

private fun overlappingRangeUpdatesAccumulate() {
    println("Test: overlapping range updates accumulate")
    val n = 8
    val fenwick = RangeUpdateFenwick(n)
    fenwick.rangeUpdate(2, 5, 3)
    fenwick.rangeUpdate(4, 8, 10)
    val expected = listOf(0L, 3L, 3L, 13L, 13L, 10L, 10L, 10L)
    assertEquals(expected, snapshot(fenwick, n), "after +3 on [2..5] and +10 on [4..8]")
}

private fun negativeDeltaSubtractsFromRange() {
    println("Test: negative delta subtracts from range")
    val n = 8
    val fenwick = RangeUpdateFenwick(n)
    fenwick.rangeUpdate(2, 5, 3)
    fenwick.rangeUpdate(4, 8, 10)
    fenwick.rangeUpdate(1, 3, -1)
    val expected = listOf(-1L, 2L, 2L, 13L, 13L, 10L, 10L, 10L)
    assertEquals(expected, snapshot(fenwick, n), "after -1 on [1..3]")
}

private fun pointQueryReturnsAccumulatedValue() {
    println("Test: pointQuery at index 5 reflects accumulated updates")
    val n = 8
    val fenwick = RangeUpdateFenwick(n)
    fenwick.rangeUpdate(2, 5, 3)
    fenwick.rangeUpdate(4, 8, 10)
    fenwick.rangeUpdate(1, 3, -1)
    assertEquals(13L, fenwick.pointQuery(5), "pointQuery(5)")
}

fun main() {
    initialArrayIsAllZeros()
    rangeUpdateAppliesDeltaToRange()
    overlappingRangeUpdatesAccumulate()
    negativeDeltaSubtractsFromRange()
    pointQueryReturnsAccumulatedValue()
    println("\nAll tests passed.")
}
