package one.codehz.container.base

import android.support.v7.widget.RecyclerView
import one.codehz.container.ext.bind
import one.codehz.container.ext.updateFrom

abstract class BaseAdapter<T : RecyclerView.ViewHolder, D : SameAsAble<D>> : RecyclerView.Adapter<T>() {
    protected val models = mutableListOf<D>()
    protected val deleted = mutableListOf<D>()

    final override fun onBindViewHolder(holder: T, position: Int) = onSetupViewHolder(holder, models[position])
    final override fun onBindViewHolder(holder: T, position: Int, payloads: List<Any?>) = if (payloads.isEmpty())
        onBindViewHolder(holder, position)
    else
        onSetupViewHolder(holder, models[position], payloads[0])

    final override fun getItemCount() = models.size

    abstract fun onSetupViewHolder(holder: T, data: D)
    open fun onSetupViewHolder(holder: T, data: D, payload: Any?) = onSetupViewHolder(holder, data)

    fun updateModels(data: List<D>) = models updateFrom (data - deleted) bind this
    fun clear() {
        models.clear()
        notifyDataSetChanged()
    }

    fun dumpDeleted() = deleted.toList()

    fun clearDeleteQueue() {
        deleted.clear()
    }

    fun enqueueDelete(target: D): () -> Unit {
        deleted += target
        return {
            deleted -= target
        }
    }
}