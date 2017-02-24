package one.codehz.container.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import one.codehz.container.base.BaseAdapter
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.ext.get
import one.codehz.container.models.SimpleComponentModel

class SimpleComponentListAdapter(val onClick: SimpleComponentListAdapter.(SimpleComponentModel) -> Unit) : BaseAdapter<SimpleComponentListAdapter.ViewHolder, SimpleComponentModel>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onSetupViewHolder(holder: ViewHolder, data: SimpleComponentModel) = holder updateData data

    inner class ViewHolder(parent: ViewGroup)
        : BaseViewHolder<SimpleComponentModel>(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_selectable_list_item, parent, false)) {
        val text1 by lazy<TextView> { itemView!![android.R.id.text1] }
        lateinit var model: SimpleComponentModel

        init {
            itemView.setOnClickListener { onClick(model) }
        }

        override fun updateData(data: SimpleComponentModel) {
            text1.text = data.name
            model = data
        }
    }
}