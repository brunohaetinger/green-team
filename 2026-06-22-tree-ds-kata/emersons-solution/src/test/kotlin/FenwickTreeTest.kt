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
        val values = longArrayOf(7, 12, 5, 9, 14)
        val fenwickTree = FenwickTree(values)

        fenwickTree.update(3, 2)

        assertEquals(7L, fenwickTree.prefixSum(1))
        assertEquals(19L, fenwickTree.prefixSum(2))
        assertEquals(26L, fenwickTree.prefixSum(3))
        assertEquals(35L, fenwickTree.prefixSum(4))
        assertEquals(49L, fenwickTree.prefixSum(5))
    }

    @Test
    fun `should calculate range sum correctly`() {
        val values = longArrayOf(1, 23, -3, 10, 55, -9, -2, 45, 21, 32, 90, -10)
        val fenwickTree = FenwickTree(values)

        assertEquals(74L, fenwickTree.rangeSum(2, 7))
    }

    @Test
    fun `should throw exception when updating index zero`() {
        val fenwickTree = FenwickTree(5)

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.update(0, 10)
        }
    }

    @Test
    fun `should throw exception when updating index greater than tree size`() {
        val fenwickTree = FenwickTree(5)

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.update(6, 10)
        }
    }

    @Test
    fun `should throw exception when calculating prefix sum with negative index`() {
        val fenwickTree = FenwickTree(5)

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.prefixSum(-1)
        }
    }

    @Test
    fun `should throw exception when calculating prefix sum with index greater than tree size`() {
        val fenwickTree = FenwickTree(5)

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.prefixSum(6)
        }
    }

    @Test
    fun `should throw exception when calculating range sum with left index less than one`() {
        val fenwickTree = FenwickTree(5)

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.rangeSum(0, 3)
        }
    }

    @Test
    fun `should throw exception when calculating range sum with left index greater than right index`() {
        val fenwickTree = FenwickTree(5)

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.rangeSum(4, 2)
        }
    }

    @Test
    fun `should throw exception when calculating range sum with right index greater than tree size`() {
        val fenwickTree = FenwickTree(5)

        assertFailsWith<IllegalArgumentException> {
            fenwickTree.rangeSum(1, 6)
        }
    }
}