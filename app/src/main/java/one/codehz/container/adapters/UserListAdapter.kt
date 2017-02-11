package one.codehz.container.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import one.codehz.container.R
import one.codehz.container.base.BaseAdapter
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.ext.get
import one.codehz.container.models.UserModel

class UserListAdapter(private val click: (UserModel) -> Unit) : BaseAdapter<UserListAdapter.ViewHolder, UserModel>() {
    override fun onSetupViewHolder(holder: ViewHolder, data: UserModel) = holder updateData data
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    inner class ViewHolder(parent: ViewGroup) : BaseViewHolder<UserModel>(LayoutInflater.from(parent.context).inflate(R.layout.user_list_item, parent, false)) {
        val nameView by lazy<TextView> { itemView[R.id.name] }
        val idView by lazy<TextView> { itemView[R.id.user_id] }
        var currentModel: UserModel? = null

        init {
            itemView.setOnClickListener { currentModel?.apply { click(this) } }
        }

        override fun updateData(data: UserModel) {
            nameView.text = data.name
            idView.text = data.id.toString()
            currentModel = data
        }
    }
}