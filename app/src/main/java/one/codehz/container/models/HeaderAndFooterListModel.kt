package one.codehz.container.models

import android.view.View
import one.codehz.container.base.SameAsAble

sealed class HeaderAndFooterListModel : SameAsAble<HeaderAndFooterListModel> {
    object HeaderModel : HeaderAndFooterListModel()
    object FooterModel : HeaderAndFooterListModel()
    class DataModel<out T>(val data: T) : HeaderAndFooterListModel() {
        override fun equals(other: Any?): Boolean {
            return data?.equals((other as? DataModel<*>)?.data) ?: false
        }

        override fun hashCode(): Int {
            return data?.hashCode() ?: 0
        }
    }
}