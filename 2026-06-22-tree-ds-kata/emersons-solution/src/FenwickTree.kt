class FenwickTree(size: Int) {

    private val tree = LongArray(size + 1)
    val size: Int get() = tree.size - 1

    fun update(index: Int, delta: Long) {
        require(index in 1..size) { "index $index out of range [1..$size]" }
        var i = index
        while (i <= size) {
            tree[i] += delta
            i += i and -i
        }
    }

    fun prefixSum(index: Int): Long {
        require(index in 0..size) { "index $index out of range [0..$size]" }
        var i = index
        var sum = 0L
        while (i > 0) {
            sum += tree[i]
            i -= i and -i
        }
        return sum
    }

    fun rangeSum(left: Int, right: Int): Long {
        require(left in 1..right && right <= size)
        return prefixSum(right) - prefixSum(left - 1)
    }
}
