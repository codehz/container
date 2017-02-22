package one.codehz.container.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import one.codehz.container.R
import one.codehz.container.base.BaseAdapter
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.ext.get
import one.codehz.container.models.LogModel


class LogListAdapter : BaseAdapter<LogListAdapter.ViewHolder, LogModel>() {
    override fun onSetupViewHolder(holder: ViewHolder, data: LogModel) = holder updateData data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    inner class ViewHolder(parent: ViewGroup) : BaseViewHolder<LogModel>(LayoutInflater.from(parent.context).inflate(R.layout.log_line, parent, false)) {
        val timeText by lazy<TextView> { itemView[R.id.time_text] }
        val infoText by lazy<TextView> { itemView[R.id.info] }

        override fun updateData(data: LogModel) {
            timeText.text = data.time
            infoText.text = data.data
        }
    }
}