package one.codehz.container.delegate

import android.annotation.TargetApi
import android.app.Notification
import android.content.Context
import android.os.Build
import com.lody.virtual.client.hook.delegate.ForegroundNotificationDelegate
import one.codehz.container.R

@TargetApi(Build.VERSION_CODES.N)
class MyForegroundNotificationDelegate(val context: Context) : ForegroundNotificationDelegate {
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