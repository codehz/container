package one.codehz.container.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import one.codehz.container.R
import one.codehz.container.base.BaseAdapter
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.ext.get
import one.codehz.container.models.SpaceManagerModel

class SpaceManagerAdapter : BaseAdapter<SpaceManagerAdapter.ViewHolder, SpaceManagerModel>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
    override fun onSetupViewHolder(holder: ViewHolder, data: SpaceManagerModel) = holder updateData data
    override fun onSetupViewHolder(holder: ViewHolder, data: SpaceManagerModel, payload: Any?) = holder updateValue payload as String

    inner class ViewHolder(parent: ViewGroup) : BaseViewHolder<SpaceManagerModel>(LayoutInflater.from(parent.context).inflate(R.layout.space_manager_item, parent, false)) {
        private var currentModel: SpaceManagerModel? = null
        private val titleView by lazy<TextView> { itemView[R.id.title] }
        private val valueView by lazy<TextView> { itemView[R.id.value] }
        private val button by lazy<Button> { itemView[R.id.button] }

        init {
            button.setOnClickListener {
                currentModel?.target?.apply {
                    itemView.context.startActivity(Intent(itemView.context, this.java))
                }
            }
        }

        override fun updateData(data: SpaceManagerModel) {
            currentModel = data
            titleView.text = data.title
            valueView.text = data.amount
        }

        infix fun updateValue(value: String) {
            valueView.text = value
        }
    }
}