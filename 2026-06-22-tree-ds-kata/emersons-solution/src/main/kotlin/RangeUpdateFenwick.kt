class RangeUpdateFenwick(size: Int) {

    private val bit = FenwickTree(size)
    val size: Int get() = bit.size

    fun rangeUpdate(left: Int, right: Int, delta: Long) {
        require(left in 1..right && right <= size) {
            "invalid range [$left..$right] for size $size"
        }
        bit.update(left, delta)
        if (right + 1 <= size) {
            bit.update(right + 1, -delta)
        }
    }

    fun pointQuery(index: Int): Long {
        require(index in 1..size) { "index $index out of range [1..$size]" }
        return bit.prefixSum(index)
    }
}
