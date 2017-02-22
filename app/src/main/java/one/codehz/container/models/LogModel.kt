package one.codehz.container.models

import one.codehz.container.base.SameAsAble

class LogModel(val time:String, val data: String) : SameAsAble<LogModel> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as LogModel

        if (time != other.time) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }
}