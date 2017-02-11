package one.codehz.container.models

import one.codehz.container.R
import one.codehz.container.base.SameAsAble
import one.codehz.container.ext.virtualCore
import kotlin.reflect.KClass

class SpaceManagerModel(val title: String, val target: KClass<*>) : SameAsAble<SpaceManagerModel> {
    var amount: String = virtualCore.context.getString(R.string.calculating)

    constructor(model: SpaceManagerModel) : this(model.title, model.target) {
        amount = model.amount
    }

    fun clone() = SpaceManagerModel(this)

    override fun sameAs(other: SpaceManagerModel): Boolean = title == other.title

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as SpaceManagerModel

        if (title != other.title) return false
        if (amount != other.amount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }
}