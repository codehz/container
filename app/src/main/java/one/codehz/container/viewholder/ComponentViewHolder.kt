package one.codehz.container.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import one.codehz.container.R
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.ext.get
import one.codehz.container.models.ComponentInfoModel

class ComponentViewHolder(parent: ViewGroup) : BaseViewHolder<ComponentInfoModel>(LayoutInflater.from(parent.context).inflate(R.layout.component_info_item, parent, false)) {
    val typeView by lazy<TextView> { itemView[R.id.type_text] }
    val titleView by lazy<TextView> { itemView[R.id.title] }
    val countView by lazy<TextView> { itemView[R.id.count_text] }
    var id: Long = 0

    override fun updateData(data: ComponentInfoModel) {
        typeView.text = data.type
        titleView.text = data.name
        id = data.id
        countView.text = data.count
    }
}