package one.codehz.container.models

import com.lody.virtual.os.VUserInfo
import one.codehz.container.base.SameAsAble

class UserModel(from: VUserInfo) : SameAsAble<UserModel> {
    val id = from.id
    val name = from.name ?: "no name"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as UserModel

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        return result
    }
}