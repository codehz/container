package one.codehz.container.base

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class BaseViewHolder<in T>(view: View) : RecyclerView.ViewHolder(view) {
    abstract infix fun updateData(data: T)
}