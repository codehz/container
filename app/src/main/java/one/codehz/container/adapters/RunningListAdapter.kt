package one.codehz.container.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import one.codehz.container.R
import one.codehz.container.base.BaseAdapter
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.ext.get
import one.codehz.container.ext.onEach
import one.codehz.container.ext.vUserManager
import one.codehz.container.models.AppModel
import one.codehz.container.models.RunningAppBaseModel
import one.codehz.container.models.RunningAppHeaderModel
import one.codehz.container.models.RunningAppModel
import java.security.InvalidParameterException

class RunningListAdapter(val onClick: (RunningAppModel, View, View) -> Unit, val onCloseUserAllProcess: (Int) -> Unit) : BaseAdapter<BaseViewHolder<RunningAppBaseModel>, RunningAppBaseModel>() {
    override fun onSetupViewHolder(holder: BaseViewHolder<RunningAppBaseModel>, data: RunningAppBaseModel) = holder updateData data

    companion object {
        private val HEADER = 0
        private val ITEM = 1
    }

    inner class HeaderViewHolder(parent: ViewGroup) : BaseViewHolder<RunningAppHeaderModel>(LayoutInflater.from(parent.context).inflate(R.layout.running_app_header, parent, false)) {
        val text by lazy<TextView> { itemView[R.id.text] }
        val closeButton by lazy<View> { itemView[R.id.close_button] }
        var uid = 0

        init {
            closeButton.setOnClickListener {
                onCloseUserAllProcess(uid)
            }
        }

        override fun updateData(data: RunningAppHeaderModel) {
            uid = data.uid
            text.text = data.name
        }
    }

    inner class ItemViewHolder(parent: ViewGroup) : BaseViewHolder<RunningAppModel>(LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)) {
        val iconView by lazy<ImageView> { itemView[R.id.icon] }
        val titleView by lazy<TextView> { itemView[R.id.title] }
        val packageNameView by lazy<TextView> { itemView[R.id.package_name] }
        var currentModel: RunningAppModel? = null

        init {
            itemView.setOnClickListener {
                onClick(currentModel!!, iconView, titleView)
            }
        }

        override fun updateData(data: RunningAppModel) {
            currentModel = data
            iconView.setImageDrawable(data.appModel.icon)
            titleView.text = data.appModel.name
            packageNameView.apply { text = context.getString(R.string.package_name_text, data.appModel.packageName) }
        }
    }

    override fun getItemViewType(position: Int) = if (models[position].isHeader) HEADER else ITEM

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        HEADER -> HeaderViewHolder(parent)
        ITEM -> ItemViewHolder(parent)
        else -> throw InvalidParameterException()
    } as BaseViewHolder<RunningAppBaseModel>

    fun updateData(params: List<Pair<Int, List<AppModel>>>) = updateModels(params.fold(mutableListOf<RunningAppBaseModel>()) { list, item ->
        val (uid, models) = item
        list.apply {
            add(RunningAppHeaderModel(uid, vUserManager.getUserInfo(uid).name))
            models.forEach {
                add(RunningAppModel(it, uid))
            }
        }
    })

}