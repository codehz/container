package one.codehz.container.provider

import android.net.Uri
import one.codehz.container.base.BaseDatabaseProvider
import one.codehz.container.db.PreferenceDb

class PreferenceProvider : BaseDatabaseProvider() {
    companion object {
        val AUTHORITY = "one.codehz.container.provider.preference"
        val PRE_PACKAGE_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("pre_package").build()!!
        val PRE_USER_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("pre_user").build()!!
    }

    override val AUTHORITY = "one.codehz.container.provider.preference"

    override fun onCreateDatabase() = PreferenceDb(context)
}