package one.codehz.container.adapters

import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.TextView
import one.codehz.container.R
import one.codehz.container.base.BaseAdapter
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.ext.get
import one.codehz.container.models.LogModel


class LogListAdapter(val onClick: (String, String) -> Unit) : BaseAdapter<LogListAdapter.ViewHolder, LogModel>() {
    override fun onSetupViewHolder(holder: ViewHolder, data: LogModel) = holder updateData data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    inner class ViewHolder(parent: ViewGroup) : BaseViewHolder<LogModel>(LayoutInflater.from(parent.context).inflate(R.layout.log_line, parent, false)) {
        val timeText by lazy<TextView> { itemView[R.id.time_text] }
        val infoParent by lazy<HorizontalScrollView> { itemView[R.id.horizontal_scroll_view] }
        val infoText by lazy<TextView> { itemView[R.id.info] }

        inner class LongPressListener : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent?) {
                super.onLongPress(e)
                onClick(timeText.text.toString(), infoText.text.toString())
            }
        }

        val longPressDetector = GestureDetector(itemView.context, LongPressListener())

        init {
            infoParent.setOnTouchListener { _, e ->
                longPressDetector.onTouchEvent(e)
            }
        }

        override fun updateData(data: LogModel) {
            timeText.text = data.time
            infoText.text = data.data
        }
    }
}