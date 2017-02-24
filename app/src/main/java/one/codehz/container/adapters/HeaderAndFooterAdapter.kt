package one.codehz.container.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import one.codehz.container.base.BaseAdapter
import one.codehz.container.base.BaseViewHolder
import one.codehz.container.base.SameAsAble
import one.codehz.container.models.HeaderAndFooterListModel
import kotlin.reflect.KClass

class HeaderAndFooterAdapter<T : SameAsAble<T>, InnerVH : BaseViewHolder<T>>
constructor(val headerRes: Int,
            val footerRes: Int,
            val headerInit: View.() -> Unit,
            val footerInit: View.() -> Unit,
            val onClick: (T) -> Unit,
            val clazzVH: KClass<InnerVH>)
    : BaseAdapter<HeaderAndFooterAdapter<T, InnerVH>.HeaderAndFooterViewHolder, HeaderAndFooterListModel>() {
    companion object {
        operator inline fun <T : SameAsAble<T>, reified InnerVH : BaseViewHolder<T>>
                invoke(headerRes: Int,
                       footerRes: Int,
                       noinline headerInit: View.() -> Unit,
                       noinline footerInit: View.() -> Unit,
                       noinline onClick: (T) -> Unit): HeaderAndFooterAdapter<T, InnerVH>
                = HeaderAndFooterAdapter(headerRes, footerRes, headerInit, footerInit, onClick, InnerVH::class)
    }

    override fun getItemViewType(position: Int) = when (position) {
        0 -> 1
        itemCount - 1 -> 2
        else -> 0
    }

    inner abstract class HeaderAndFooterViewHolder(view: View)
        : BaseViewHolder<HeaderAndFooterListModel>(view) {
        lateinit var inner: InnerVH

        constructor(inner: InnerVH) : this(inner.itemView) {
            this.inner = inner;
        }
    }

    inner class HeaderViewHolder(parent: ViewGroup)
        : HeaderAndFooterViewHolder(LayoutInflater.from(parent.context).inflate(headerRes, parent, false)) {
        override fun updateData(data: HeaderAndFooterListModel) = headerInit(itemView)
    }

    inner class FooterViewHolder(parent: ViewGroup)
        : HeaderAndFooterViewHolder(LayoutInflater.from(parent.context).inflate(footerRes, parent, false)) {
        override fun updateData(data: HeaderAndFooterListModel) = footerInit(itemView)
    }

    inner class ContentViewHolder(parent: ViewGroup)
        : HeaderAndFooterViewHolder(clazzVH.constructors.first().call(parent)) {
        lateinit var model: T

        init {
            itemView.setOnClickListener { onClick(model) }
        }

        @Suppress("UNCHECKED_CAST")
        override fun updateData(data: HeaderAndFooterListModel) {
            model = (data as HeaderAndFooterListModel.DataModel<T>).data
            inner.updateData(model)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        1 -> HeaderViewHolder(parent)
        2 -> FooterViewHolder(parent)
        else -> ContentViewHolder(parent)
    }

    override fun onSetupViewHolder(holder: HeaderAndFooterViewHolder, data: HeaderAndFooterListModel)
            = holder updateData data

    fun updateModelsEx(data: List<T>)
            = updateModels(listOf(HeaderAndFooterListModel.HeaderModel)
            + data.map { HeaderAndFooterListModel.DataModel(it) }
            + listOf(HeaderAndFooterListModel.FooterModel))
}