package one.codehz.container.interfaces

import android.graphics.drawable.Drawable

interface IFloatingActionTarget {
    val canBeFloatingActionTarget: Boolean

    fun onFloatingAction()

    fun getFloatingDrawable(): Int
}
