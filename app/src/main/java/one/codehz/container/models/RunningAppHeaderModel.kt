package one.codehz.container.models

import one.codehz.container.base.SameAsAble

class RunningAppHeaderModel(val uid: Int, val name: String) : RunningAppBaseModel(true) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as RunningAppHeaderModel

        if (uid != other.uid) return false

        return true
    }

    override fun hashCode(): Int {
        return uid
    }
}