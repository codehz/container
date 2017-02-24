package one.codehz.container.models

import one.codehz.container.base.SameAsAble

class SimpleComponentModel(val name: String) : SameAsAble<SimpleComponentModel> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as SimpleComponentModel

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}