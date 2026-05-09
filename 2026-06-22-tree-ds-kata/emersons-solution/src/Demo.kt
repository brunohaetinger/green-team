fun main() {
    val n = 8
    val fenwick = RangeUpdateFenwick(n)

    println("Array inicial: ${(1..n).map { fenwick.pointQuery(it) }}")

    fenwick.rangeUpdate(2, 5, 3)
    println("Depois de +3 em [2..5]: ${(1..n).map { fenwick.pointQuery(it) }}")

    fenwick.rangeUpdate(4, 8, 10)
    println("Depois de +10 em [4..8]: ${(1..n).map { fenwick.pointQuery(it) }}")

    fenwick.rangeUpdate(1, 3, -1)
    println("Depois de -1 em [1..3]: ${(1..n).map { fenwick.pointQuery(it) }}")

    println("Valor em a[5]: ${fenwick.pointQuery(5)}")
}
