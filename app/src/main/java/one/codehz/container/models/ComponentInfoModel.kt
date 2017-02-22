package one.codehz.container.models

import one.codehz.container.base.SameAsAble

class ComponentInfoModel(val id: Long, val name: String, val type: String, val count: String) : SameAsAble<ComponentInfoModel> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ComponentInfoModel

        if (id != other.id) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (count != other.count) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + count.hashCode()
        return result
    }
}