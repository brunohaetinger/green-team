import kotlin.random.Random
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DollarQuoteAnalysisTest {

    @Test
    fun `should calculate total accumulated exchange rate of 2593 from day 1 to day 5 with prefixSum`() {

        val dollarQuotes = longArrayOf(
            510,
            515,
            520,
            518,
            530,
            540,
            535
        )

        val fenwickTree = FenwickTree(dollarQuotes)

        assertEquals(2593L, fenwickTree.prefixSum(5))
    }

    @Test
    fun `should calculate total accumulated exchange rate of 3668 from day 1 to day 7 with rangeSum`() {

        val dollarQuotes = longArrayOf(
            510,
            515,
            520,
            518,
            530,
            540,
            535
        )

        val fenwickTree = FenwickTree(dollarQuotes)

        assertEquals(3668L, fenwickTree.rangeSum(1, 7))
    }

    @Test
    fun `should calculate total accumulated exchange rate of 1588 from day 4 to day 6 with rangeSum`() {

        val dollarQuotes = longArrayOf(
            510,
            515,
            520,
            518,
            530,
            540,
            535
        )

        val fenwickTree = FenwickTree(dollarQuotes)

        assertEquals(1588L, fenwickTree.rangeSum(4, 6))
    }

    @Test
    fun `should recalculate prefix sum to 1560 and total sum to 2608 after updating day 3`() {

        val dollarQuotes = longArrayOf(
            510,
            515,
            520,
            518,
            530
        )

        val fenwickTree = FenwickTree(dollarQuotes)

        fenwickTree.update(3, 15)

        assertEquals(1560L, fenwickTree.prefixSum(3))
        assertEquals(2608L, fenwickTree.rangeSum(1, 5))
    }

    @Test
    fun `should process one million exchange rates with random queries and updates`() {

        val size = 1_000_000

        val dollarQuotes = LongArray(size) {
            Random.nextLong(420, 610)
        }

        val buildTime = measureTimeMillis {
            val fenwickTree = FenwickTree(dollarQuotes)

            val operationTime = measureTimeMillis {
                repeat(100_000) {
                    val left = Random.nextInt(1, size / 2)
                    val right = Random.nextInt(left, size)

                    fenwickTree.rangeSum(left, right)

                    val updateIndex = Random.nextInt(1, size)
                    val delta = Random.nextLong(-10, 10)

                    fenwickTree.update(updateIndex, delta)
                }
            }

            println("Operations execution time: ${operationTime}ms")
        }

        println("Fenwick tree build time: ${buildTime}ms")

        assertTrue(buildTime >= 0)
    }

}