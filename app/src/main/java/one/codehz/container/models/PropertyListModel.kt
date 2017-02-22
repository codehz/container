package one.codehz.container.models

import one.codehz.container.annotation.propertyField

open class PropertyListModel {
    fun getItems() = this.javaClass.declaredMethods
            .map { it.getAnnotation(propertyField::class.java) to it }
            .filter { it.first != null }
            .sortedBy { it.first.order }
            .map { PropertyListItemModel(it.first.nameRes, it.second.invoke(this).toString()) }
}