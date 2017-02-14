package one.codehz.container

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.support.v4.app.NotificationCompat
import com.lody.virtual.client.core.InstallStrategy
import one.codehz.container.ext.staticName
import one.codehz.container.ext.systemService
import one.codehz.container.ext.virtualCore
import one.codehz.container.models.AppModel

class InstallService : Service() {
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.arg2 == 0)
                startInstall(msg.obj as Uri)
            else
                startUninstall(msg.obj as String)
            stopSelf(msg.arg1)
        }
    }

    private val thread by lazy { HandlerThread("InstallServiceThread", Process.THREAD_PRIORITY_BACKGROUND).apply(Thread::start) }
    private val handler by lazy { ServiceHandler(thread.looper) }
    private val notificationManager by lazy { systemService<NotificationManager>(Context.NOTIFICATION_SERVICE) }

    override fun onCreate() {
        super.onCreate()
        handler
    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun makePendingService(id: Int, target: String, fn: (Intent.() -> Unit)? = null)
            = PendingIntent.getService(this, id, Intent(this, InstallService::class.java).apply { action = target; fn?.invoke(this) }, PendingIntent.FLAG_CANCEL_CURRENT)

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val cancelPendingIntent = makePendingService(PENDING_CANCEL, CANCEL)
        when (intent.action) {
            REQUEST_INSTALL -> NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.install_request))
                    .setContentText(getString(R.string.install_request_content, intent.data.path))
                    .addAction(NotificationCompat.Action(0, getString(R.string.install), makePendingService(PENDING_INSTALL, INSTALL) { data = intent.data }))
                    .addAction(NotificationCompat.Action(0, getString(android.R.string.cancel), cancelPendingIntent))
            REQUEST_UNINSTALL -> NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.uninstall_request))
                    .setContentText(getString(R.string.uninstall_request_content, intent.getStringExtra(KEY_PACKAGE_NAME)))
                    .addAction(NotificationCompat.Action(0, getString(R.string.uninstall), makePendingService(PENDING_UNINSTALL, UNINSTALL) { putExtra(KEY_PACKAGE_NAME, intent.getStringExtra(KEY_PACKAGE_NAME)) }))
                    .addAction(NotificationCompat.Action(0, getString(android.R.string.cancel), cancelPendingIntent))
            INSTALL -> {
                notificationManager.cancel(NOTIFICATION_ID_REQUEST)
                handler.sendMessage(handler.obtainMessage(0, startId, 0, intent.data))
                return START_NOT_STICKY
            }
            UNINSTALL -> {
                notificationManager.cancel(NOTIFICATION_ID_REQUEST)
                handler.sendMessage(handler.obtainMessage(0, startId, 1, intent.getStringExtra(KEY_PACKAGE_NAME)))
                return START_NOT_STICKY
            }
            CANCEL -> {
                notificationManager.cancel(NOTIFICATION_ID_REQUEST)
                return START_NOT_STICKY
            }
            OPEN -> {
                notificationManager.cancel(NOTIFICATION_ID_INSTALL)
                startActivity(Intent(this, VLoadingActivity::class.java).apply {
                    data = intent.data
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                return START_NOT_STICKY
            }
            else -> {
                stopSelf(startId)
                return START_NOT_STICKY
            }
        }.apply {
            setSmallIcon(R.drawable.ic_install_request)
            setVibrate(longArrayOf(1))
            priority = Notification.PRIORITY_HIGH
            notificationManager.notify(NOTIFICATION_ID_REQUEST, build())
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }

    private fun startUninstall(packageName: String) {
        virtualCore.uninstallApp(packageName)
        notificationManager.notify(NOTIFICATION_ID_UNINSTALL, Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_install_request)
                .setContentTitle(getString(R.string.application_uninstalled))
                .setTicker(getString(R.string.application_uninstalled))
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(longArrayOf(1))
                .setAutoCancel(true)
                .build())
    }

    private fun startInstall(path: Uri) {
        val notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_install_request)
                .setContentTitle(getString(R.string.installing))
                .setTicker(getString(R.string.installing))
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(longArrayOf(1))
                .setProgress(0, 0, true)

        notificationManager.notify(NOTIFICATION_ID_INSTALL, notification.build())

        val width = resources.getDimension(android.R.dimen.notification_large_icon_width).toInt()
        val height = resources.getDimension(android.R.dimen.notification_large_icon_height).toInt()

        virtualCore.installApp(path.path, InstallStrategy.UPDATE_IF_EXIST).run {
            when {
                isSuccess -> {
                    AppModel(this@InstallService, virtualCore.findApp(packageName)).apply {
                        notification
                                .setLargeIcon(Bitmap.createScaledBitmap(icon.bitmap, width, height, false))
                                .setContentTitle(getString(R.string.optimize_application_with_name, name))
                                .setTicker(getString(R.string.optimize_application_with_name, name))
                        notificationManager.notify(NOTIFICATION_ID_INSTALL, notification.build())

                        virtualCore.preOpt(packageName)

                        notification
                                .setContentTitle(getString(R.string.install_finished_with_name, name))
                                .setTicker(getString(R.string.install_finished_with_name, name))
                                .addAction(NotificationCompat.Action(0, name,
                                        makePendingService(PENDING_OPEN, OPEN) {
                                            data = Uri.Builder().scheme("container").authority("launch").appendPath(packageName).build()
                                        }))
                                .setProgress(0, 0, false)
                        notificationManager.notify(NOTIFICATION_ID_INSTALL, notification.build())
                    }
                }
                else -> {
                    notification
                            .setContentTitle(getString(R.string.install_failed))
                            .setTicker(getString(R.string.install_failed))
                            .setContentText(error)
                            .setProgress(0, 0, false)
                    notificationManager.notify(NOTIFICATION_ID_INSTALL, notification.build())
                }
            }
        }
    }

    companion object {
        val REQUEST_INSTALL = "one.codehz.container.REQUEST_INSTALL"
        val REQUEST_UNINSTALL = "one.codehz.container.REQUEST_UNINSTALL"
        val INSTALL = "one.codehz.container.INSTALL"
        val UNINSTALL = "one.codehz.container.UNINSTALL"
        val CANCEL = "one.codehz.container.CANCEL"
        val OPEN = "one.codehz.container.OPEN"
        val KEY_PACKAGE_NAME by staticName
        val PENDING_INSTALL = 0
        val PENDING_UNINSTALL = 1
        val PENDING_CANCEL = 2
        val PENDING_OPEN = 3
        val NOTIFICATION_ID_REQUEST = 10
        val NOTIFICATION_ID_INSTALL = 11
        val NOTIFICATION_ID_UNINSTALL = 12
    }
}