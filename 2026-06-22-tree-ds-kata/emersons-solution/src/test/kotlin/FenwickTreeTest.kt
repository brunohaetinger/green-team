import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FenwickTreeTest {

    @Test
    fun `should verify internal tree structure layout after construction`() {
        val values = longArrayOf(1, 2, 3, 4, 5)

        val fenwickTree = FenwickTree(values)

        val expectedTreeStructure = longArrayOf(0, 1, 3, 3, 10, 5)

        assertContentEquals(expectedTreeStructure, fenwickTree.getTree())
    }

    @Test
    fun `should verify internal tree structure layout after point update`() {
        val values = longArrayOf(1, 2, 3, 4, 5)
        val fenwickTree = FenwickTree(values)

        fenwickTree.update(3, 5)

        val expectedTreeStructureAfterUpdate = longArrayOf(0, 1, 3, 8, 15, 5)

        assertContentEquals(expectedTreeStructureAfterUpdate, fenwickTree.getTree())
    }

    @Test
    fun `should calculate correct prefix sums after construction with initial values`() {
        val values = longArrayOf(1, 2, 3, 4, 5)

        val fenwickTree = FenwickTree(values)

        assertEquals(1L, fenwickTree.prefixSum(1))
        assertEquals(3L, fenwickTree.prefixSum(2))
        assertEquals(6L, fenwickTree.prefixSum(3))
        assertEquals(10L, fenwickTree.prefixSum(4))
        assertEquals(15L, fenwickTree.prefixSum(5))
    }

    @Test
    fun `should propagate delta correctly to subsequent prefix sums after point update`() {
        val values = longArrayOf(1, 2, 3, 4, 5)
        val fenwickTree = FenwickTree(values)

        fenwickTree.update(3, 5)

        assertEquals(1L, fenwickTree.prefixSum(1))
        assertEquals(3L, fenwickTree.prefixSum(2))
        assertEquals(11L, fenwickTree.prefixSum(3))
        assertEquals(15L, fenwickTree.prefixSum(4))
        assertEquals(20L, fenwickTree.prefixSum(5))
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