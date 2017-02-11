package one.codehz.container.base

interface SameAsAble<T : SameAsAble<T>> {
    infix fun sameAs(other: T): Boolean = this == other
    infix fun getPayloads(other: T): Any? = null
}