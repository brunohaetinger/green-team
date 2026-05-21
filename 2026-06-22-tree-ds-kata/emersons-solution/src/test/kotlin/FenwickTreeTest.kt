import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FenwickTreeTest {

    @Test
    fun `should build fenwick tree from initial values`() {

        val values = longArrayOf(1, 2, 3, 4, 5)

        val fenwickTree = FenwickTree(values)

        assertEquals(1L, fenwickTree.prefixSum(1))
        assertEquals(3L, fenwickTree.prefixSum(2))
        assertEquals(6L, fenwickTree.prefixSum(3))
        assertEquals(10L, fenwickTree.prefixSum(4))
        assertEquals(15L, fenwickTree.prefixSum(5))
    }

    @Test
    fun `should update value and recalculate sums correctly`() {
        val values = longArrayOf(1, 2, 3, 4, 5)
        val fenwickTree = FenwickTree(values)

        fenwickTree.update(3, 2) // index 3 becomes 5

        assertEquals(1L, fenwickTree.prefixSum(1))
        assertEquals(3L, fenwickTree.prefixSum(2))
        assertEquals(8L, fenwickTree.prefixSum(3))
        assertEquals(12L, fenwickTree.prefixSum(4))
        assertEquals(17L, fenwickTree.prefixSum(5))
    }

    @Test
    fun `should calculate range sum correctly`() {
        val values = longArrayOf(1, 2, 3, 4, 5)
        val fenwickTree = FenwickTree(values)

        assertEquals(9L, fenwickTree.rangeSum(2, 4))
        assertEquals(12L, fenwickTree.rangeSum(3, 5))
        assertEquals(15L, fenwickTree.rangeSum(1, 5))
    }

    @Test
    fun `should throw exception for invalid update index`() {
        val fenwickTree = FenwickTree(5)

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.update(0, 10)
        }

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.update(6, 10)
        }
    }

    @Test
    fun `should throw exception for invalid prefix sum index`() {
        val fenwickTree = FenwickTree(5)

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.prefixSum(-1)
        }

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.prefixSum(6)
        }
    }

    @Test
    fun `should throw exception for invalid range sum`() {
        val fenwickTree = FenwickTree(5)

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.rangeSum(0, 3)
        }

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.rangeSum(4, 2)
        }

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.rangeSum(1, 6)
        }
    }
}