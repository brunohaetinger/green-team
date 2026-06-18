class FenwickTree(size: Int) {

    private val tree = LongArray(size + 1)
    val size: Int get() = tree.size - 1

    constructor(values: LongArray) : this(values.size) {
        for (i in values.indices) {
            tree[i + 1] = values[i]
        }
        for (i in 1..size) {
            val parent = i + getLeastSignificantBit(i)
            if (parent <= size) {
                tree[parent] += tree[i]
            }
        }
    }

    private fun getLeastSignificantBit(i: Int): Int = i and -i

    fun getTree(): LongArray {
        return tree
    }

    fun update(index: Int, delta: Long) {
        require(index in 1..size) { "index $index out of range [1..$size]" }
        var i = index
        while (i <= size) {
            tree[i] += delta
            i += getLeastSignificantBit(i)
        }
    }

    fun prefixSum(index: Int): Long {
        require(index in 0..size) { "index $index out of range [0..$size]" }
        var i = index
        var sum = 0L
        while (i > 0) {
            sum += tree[i]
            i -= getLeastSignificantBit(i)
        }
        return sum
    }


    fun rangeSum(left: Int, right: Int): Long {
        require(left in 1..right && right <= size)
        return prefixSum(right) - prefixSum(left - 1)
    }
}
