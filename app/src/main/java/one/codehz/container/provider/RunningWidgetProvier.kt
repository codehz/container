package one.codehz.container.provider

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.RemoteViews
import com.lody.virtual.helper.utils.VLog
import one.codehz.container.MainActivity
import one.codehz.container.R
import one.codehz.container.RunningWidgetService
import one.codehz.container.VLoadingActivity
import one.codehz.container.ext.killAllAppsEx
import one.codehz.container.ext.virtualCore

class RunningWidgetProvier : AppWidgetProvider() {
    companion object {
        val handler = Handler()
        fun forceUpdate(context: Context) {
            with(AppWidgetManager.getInstance(context)) {
                handler.postDelayed({ notifyAppWidgetViewDataChanged(getAppWidgetIds(ComponentName(context, RunningWidgetProvier::class.java)), R.id.main_list) }, 250)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach {
            onAppWidgetOptionsChanged(context, appWidgetManager, it, appWidgetManager.getAppWidgetOptions(it))
            appWidgetManager.notifyAppWidgetViewDataChanged(it, R.id.main_list)
            VLog.d("RWP", "update %d", it)
        }
        VLog.d("RWP", "update")
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val useLargeLayout = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) > 144
        val view = RemoteViews(context.packageName, if (useLargeLayout) R.layout.running_widget_large else R.layout.running_widget)
        view.setRemoteAdapter(R.id.main_list, Intent(context, RunningWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        })
        view.setEmptyView(R.id.main_list, R.id.empty_view)
        view.setPendingIntentTemplate(R.id.main_list,
                PendingIntent.getActivity(context, 0, Intent(context, VLoadingActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
        view.setOnClickPendingIntent(R.id.add_button,
                PendingIntent.getActivity(context, 1, Intent(context, MainActivity::class.java).apply { action = "one.codehz.container.TAB_INSTALL" }, PendingIntent.FLAG_UPDATE_CURRENT))
        view.setOnClickPendingIntent(R.id.clear,
                PendingIntent.getBroadcast(context, 2, Intent(context, RunningWidgetProvier::class.java).apply { action = "CLEAR_ALL" }, PendingIntent.FLAG_UPDATE_CURRENT))
        if (useLargeLayout)
            view.setOnClickPendingIntent(R.id.run_button,
                    PendingIntent.getActivity(context, 3, Intent(context, MainActivity::class.java).apply { action = "one.codehz.container.TAB_RUNNING" }, PendingIntent.FLAG_UPDATE_CURRENT))
        appWidgetManager.updateAppWidget(appWidgetId, view)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            "CLEAR_ALL" -> {
                virtualCore.killAllAppsEx()
            }
        }
    }
}