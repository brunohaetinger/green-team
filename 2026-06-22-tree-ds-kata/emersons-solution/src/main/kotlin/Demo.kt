fun main() {
    val n = 8
    val fenwick = RangeUpdateFenwick(n)

    println("Initial array: ${(1..n).map { fenwick.pointQuery(it) }}")

    fenwick.rangeUpdate(2, 5, 3)
    println("After +3 on [2..5]: ${(1..n).map { fenwick.pointQuery(it) }}")

    fenwick.rangeUpdate(4, 8, 10)
    println("After +10 on [4..8]: ${(1..n).map { fenwick.pointQuery(it) }}")

    fenwick.rangeUpdate(1, 3, -1)
    println("After -1 on [1..3]: ${(1..n).map { fenwick.pointQuery(it) }}")

    println("Value at a[5]: ${fenwick.pointQuery(5)}")
}
