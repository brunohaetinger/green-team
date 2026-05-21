import kotlin.test.Test
import kotlin.test.assertEquals

class DollarQuoteAnalysisTest {

    @Test
    fun `should analyze dollar exchange rate variations`() {

        // Daily dollar exchange rates (USD -> BRL)
        val dollarQuotes = longArrayOf(
            510, // Day 1 -> 5.10
            515, // Day 2 -> 5.15
            520, // Day 3 -> 5.20
            518, // Day 4 -> 5.18
            530, // Day 5 -> 5.30
            540, // Day 6 -> 5.40
            535  // Day 7 -> 5.35
        )

        val fenwickTree = FenwickTree(dollarQuotes)

        // Total accumulated exchange rate from day 1 to day 7
        assertEquals(3668L, fenwickTree.rangeSum(1, 7))

        // Sum between day 1 and day 3
        assertEquals(1545L, fenwickTree.rangeSum(1, 3))

        // Sum between day 4 and day 6
        assertEquals(1588L, fenwickTree.rangeSum(4, 6))

        // Prefix sum until day 5
        assertEquals(2593L, fenwickTree.prefixSum(5))
    }

    @Test
    fun `should update exchange rate after market correction`() {

        val dollarQuotes = longArrayOf(
            510,
            515,
            520,
            518,
            530
        )

        val fenwickTree = FenwickTree(dollarQuotes)

        // Market correction on day 3
        // Previous: 5.20
        // New value: 5.35
        fenwickTree.update(3, 15)

        // Expected:
        // 510 + 515 + 535 = 1560
        assertEquals(1560L, fenwickTree.prefixSum(3))

        // Full accumulated value after correction
        assertEquals(2608L, fenwickTree.rangeSum(1, 5))
    }

    @Test
    fun `should analyze weekly exchange rate window`() {

        val dollarQuotes = longArrayOf(
            505,
            507,
            510,
            515,
            520,
            525,
            530
        )

        val fenwickTree = FenwickTree(dollarQuotes)

        // Weekly moving window (days 2 -> 6)
        val weeklyWindow = fenwickTree.rangeSum(2, 6)

        assertEquals(2577L, weeklyWindow)
    }

}