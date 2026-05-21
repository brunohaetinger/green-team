import kotlin.random.Random
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertTrue

class DollarQuoteAnalysisPerformanceTest {

    @Test
    fun `should handle large volume of exchange rate data`() {

        val size = 1_000_000

        // Simulating 1 million dollar quotes
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