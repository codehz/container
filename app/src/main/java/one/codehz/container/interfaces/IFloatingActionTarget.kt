package one.codehz.container.interfaces

interface IFloatingActionTarget {
    val canBeFloatingActionTarget: Boolean

    fun onFloatingAction()

    fun getFloatingDrawable(): Int
}
