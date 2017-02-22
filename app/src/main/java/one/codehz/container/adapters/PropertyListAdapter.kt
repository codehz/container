package one.codehz.container.adapters

import android.content.ClipData
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import one.codehz.container.R
import one.codehz.container.base.BaseAdapter
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.ext.clipboardManager
import one.codehz.container.ext.get
import one.codehz.container.ext.virtualCore
import one.codehz.container.models.PropertyListItemModel
import one.codehz.container.models.PropertyListModel

class PropertyListAdapter<T : PropertyListModel>(val onClick: (String, String) -> Unit) : BaseAdapter<PropertyListAdapter<T>.PropertyListViewHolder, PropertyListItemModel>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PropertyListViewHolder(parent)
    override fun onSetupViewHolder(holder: PropertyListViewHolder, data: PropertyListItemModel) = holder updateData data

    inner class PropertyListViewHolder(parent: ViewGroup) : BaseViewHolder<PropertyListItemModel>(LayoutInflater.from(parent.context).inflate(R.layout.property_item, parent, false)) {
        val keyView by lazy<TextView> { itemView[R.id.prop_key] }
        val valueView by lazy<TextView> { itemView[R.id.prop_value] }

        init {
            itemView.setOnClickListener {
                onClick(keyView.text.toString(), valueView.text.toString())
            }
        }

        override fun updateData(data: PropertyListItemModel) {
            keyView.text = itemView.context.getString(data.key)
            valueView.text = data.value
        }
    }
}