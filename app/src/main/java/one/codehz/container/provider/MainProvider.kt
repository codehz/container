package one.codehz.container.provider

import android.net.Uri
import one.codehz.container.base.BaseDatabaseProvider
import one.codehz.container.db.MainDb

class MainProvider : BaseDatabaseProvider() {
    companion object {
        val AUTHORITY = "one.codehz.container.provider.main"
        val LOG_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("log").build()!!
        val COMPONENT_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("component").build()!!
        val COMPONENT_LOG_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("clog").build()!!
        val COMPONENT_LOG_VIEW_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("clog_view").build()!!
    }

    override fun onCreateDatabase() = MainDb(context)

    override val AUTHORITY = "one.codehz.container.provider.main"
}