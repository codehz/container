package one.codehz.container

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.lody.virtual.os.VUserInfo
import one.codehz.container.ext.vUserManager
import one.codehz.container.ext.virtualCore
import one.codehz.container.models.AppModel

class RunningWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WidgetFactory(applicationContext, intent)
    }

    override fun onCreate() {
        super.onCreate()
    }

    class WidgetFactory(val context: Context, val intent: Intent) : RemoteViewsFactory {
        lateinit var data: List<Pair<AppModel, VUserInfo>>

        override fun getLoadingView() = null

        override fun getViewAt(position: Int)
                = RemoteViews(context.packageName, R.layout.runing_widget_item).apply {
            val (model, user) = data[position]
            val density = context.resources.displayMetrics.density
            setImageViewBitmap(R.id.app_icon, Bitmap.createScaledBitmap(model.icon.bitmap, (48 * density).toInt(), (48 * density).toInt(), false))
            setOnClickFillInIntent(R.id.app_icon, Intent().apply { data = Uri.Builder().scheme("container").authority("launch").appendPath(model.packageName).fragment(user.id.toString()).build() })
            setTextViewText(R.id.app_name, model.name)
            setTextViewText(R.id.user_name, user.name)
        }

        override fun getViewTypeCount() = 1

        override fun onCreate() {
            onDataSetChanged()
        }

        override fun getItemId(position: Int) = position.toLong()

        override fun onDataSetChanged() {
            data = virtualCore.allApps
                    .map { it to vUserManager.users.filter { user -> virtualCore.isAppRunning(it.packageName, user.id) } }
                    .filter { it.second.isNotEmpty() }
                    .map { AppModel(context, it.first) to it.second }
                    .fold(mutableListOf<Pair<AppModel, VUserInfo>>()) { list, el ->
                        list.addAll(el.second.map { el.first to it })
                        list
                    }
        }

        override fun hasStableIds() = true

        override fun getCount() = data.size

        override fun onDestroy() {

        }

    }
}