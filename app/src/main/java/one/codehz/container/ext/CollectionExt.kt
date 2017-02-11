package one.codehz.container.ext

infix fun <T> Collection<T>.differenceTo(others: Collection<T>): Boolean {
    if (this.size != others.size)
        return true
    else {
        for ((a, b) in zip(others)) {
            if (a != b) return true
            else continue
        }
        return false
    }
}
