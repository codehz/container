package one.codehz.container.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import one.codehz.container.R
import one.codehz.container.base.BaseAdapter
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.ext.get
import one.codehz.container.models.ComponentInfoModel

class ComponentListAdapter(val hasCount: Boolean, val onClick: (Long, String, String) -> Unit) : BaseAdapter<ComponentListAdapter.ViewHolder, ComponentInfoModel>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onSetupViewHolder(holder: ViewHolder, data: ComponentInfoModel) = holder updateData data

    inner class ViewHolder(parent: ViewGroup) : BaseViewHolder<ComponentInfoModel>(LayoutInflater.from(parent.context).inflate(R.layout.component_info_item, parent, false)) {
        val typeView by lazy<TextView> { itemView[R.id.type_text] }
        val titleView by lazy<TextView> { itemView[R.id.title] }
        val countView by lazy<TextView> { itemView[R.id.count_text] }
        var id: Long = 0

        init {
            itemView.setOnClickListener {
                onClick(id, typeView.text.toString(), titleView.text.toString())
            }
        }

        override fun updateData(data: ComponentInfoModel) {
            typeView.text = data.type
            titleView.text = data.name
            id = data.id
            if (hasCount)
                countView.text = data.count
            countView.visibility = if (hasCount) View.VISIBLE else View.GONE
        }
    }
}