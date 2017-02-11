package one.codehz.container.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import one.codehz.container.R

class RecycleViewWithEmptySupport @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyle: Int = 0)
    : RecyclerView(context, attributeSet, defStyle) {
    var emptyDrawable: Drawable = ColorDrawable(Color.RED)
    private var isEmpty = false

    init {
        attributeSet?.let {
            context.obtainStyledAttributes(it, R.styleable.RecycleViewWithEmptySupport).apply {
                this.getResourceId(R.styleable.RecycleViewWithEmptySupport_emptyDrawable, 0).apply {
                    if (this != 0)
                        emptyDrawable = context.getDrawable(this)
                }
            }.recycle()
        }
        addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            emptyDrawable.setBounds(0, 0, right - left, bottom - top)
        }
    }

    private val emptyObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            isEmpty = adapter.itemCount == 0
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            isEmpty = false
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            isEmpty = adapter.itemCount == 0
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)

        adapter?.apply {
            try {
                registerAdapterDataObserver(emptyObserver)
            } catch (ignored: Exception) {
            }
            emptyObserver.onChanged()
        }
    }

    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if (isEmpty) {
            emptyDrawable.draw(c)
        }
    }
}