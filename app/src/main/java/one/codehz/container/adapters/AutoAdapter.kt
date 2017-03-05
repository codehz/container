package one.codehz.container.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.lody.virtual.helper.utils.VLog
import one.codehz.container.base.BaseAdapter
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.base.SameAsAble
import one.codehz.container.ext.AutoAdapterModel
import one.codehz.container.ext.ViewBinding
import one.codehz.container.ext.toBindingMap

class AutoAdapter<T : AutoAdapterModel<T>>(val itemRes: Int, val bindingMap: Map<Int, ViewBinding<T, Any>>, val onClick: (T) -> Unit) : BaseAdapter<AutoAdapter<T>.ViewHolder, T>() {
    companion object {
        operator inline fun <reified T : AutoAdapterModel<T>> invoke(itemRes: Int, noinline onClick: (T) -> Unit) : AutoAdapter<T> = AutoAdapter(itemRes, T::class.toBindingMap(), onClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onSetupViewHolder(holder: ViewHolder, data: T) = holder updateData data

    inner class ViewHolder(parent: ViewGroup) : BaseViewHolder<T>(LayoutInflater.from(parent.context).inflate(itemRes, parent, false)) {
        lateinit var cache: T

        init {
            itemView.setOnClickListener {
                onClick(cache)
            }
        }

        override fun updateData(data: T) {
            cache = data
            bindingMap.forEach {
                VLog.d("AA", it.toString())
                it.value.first(itemView.findViewById(it.key), it.value.second(data))
            }
        }
    }
}