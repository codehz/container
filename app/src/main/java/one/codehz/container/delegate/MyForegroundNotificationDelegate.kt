package one.codehz.container.delegate

import android.app.Notification
import android.content.Context
import com.lody.virtual.client.hook.delegate.ForegroundNotificationDelegate
import one.codehz.container.R


class MyForegroundNotificationDelegate(val context: Context) : ForegroundNotificationDelegate {
    override fun isEnable() = true

    override fun isTryToHide() = false

    override fun getNotification()
            = Notification.Builder(context)
            .setContentTitle(context.getString(R.string.foreground_notification_title))
            .setPriority(Notification.PRIORITY_MIN)
            .setTicker(context.getString(R.string.container_has_started))
            .setSmallIcon(R.drawable.ic_stat_foreground)
            .setGroup("VA")
            .setGroupSummary(true)
            .build()!!

    override fun getGroup(orig: String?) = "VA"
}