package one.codehz.container.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import one.codehz.container.R
import one.codehz.container.base.BaseAdapter
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.ext.get
import one.codehz.container.models.AppModel

class AppListAdapter(val onClick: (AppModel, View, View, Boolean) -> Unit) : BaseAdapter<AppListAdapter.AppListViewHolder, AppModel>() {
    override fun onSetupViewHolder(holder: AppListViewHolder, data: AppModel) = holder updateData data
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AppListViewHolder(parent.context, parent, onClick)

    inner class AppListViewHolder(context: Context, parent: ViewGroup, click: (AppModel, View, View, Boolean) -> Unit) : BaseViewHolder<AppModel>(LayoutInflater.from(context).inflate(R.layout.app_item, parent, false)) {
        val iconView by lazy<ImageView> { itemView[R.id.icon] }
        val titleView by lazy<TextView> { itemView[R.id.title] }
        val packageNameView by lazy<TextView> { itemView[R.id.package_name] }
        var currentModel: AppModel? = null

        init {
            itemView.setOnClickListener { click(currentModel!!, iconView, titleView, false) }
            itemView.setOnLongClickListener { click(currentModel!!, iconView, titleView, true); true }
        }

        override fun updateData(data: AppModel) {
            this.currentModel = data
            iconView.setImageDrawable(data.icon)
            titleView.text = data.name
            packageNameView.apply { text = context.getString(R.string.package_name_text, data.packageName) }
        }
    }
}