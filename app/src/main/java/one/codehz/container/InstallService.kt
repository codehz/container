package one.codehz.container

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.view.WindowManager
import com.lody.virtual.client.core.InstallStrategy
import com.lody.virtual.os.VUserHandle
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

    override fun onCreate() {
        super.onCreate()
        handler
    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            var handling = false
            when (intent.action) {
                INSTALL -> AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_install_request)
                        .setTitle(R.string.install_request)
                        .setMessage(getString(R.string.install_request_content, virtualCore.processName, intent.dataString))
                        .setPositiveButton(R.string.install) { dialogInterface: DialogInterface, i: Int ->
                            handler.sendMessage(handler.obtainMessage(0, startId, 0, intent.data))
                            handling = true
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        .setOnDismissListener {
                            if (!handling)
                                stopSelf()
                        }
                        .create()
                UNINSTALL -> AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_alert)
                        .setTitle(getString(R.string.uninstall_request))
                        .setMessage(getString(R.string.uninstall_request_content, intent.getStringExtra(KEY_PACKAGE_NAME)))
                        .setPositiveButton(android.R.string.yes) { dialogInterface: DialogInterface, i: Int ->
                            handler.sendMessage(handler.obtainMessage(0, startId, 1, intent.getStringExtra(KEY_PACKAGE_NAME)))
                            handling = true
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        .setOnDismissListener {
                            if (!handling)
                                stopSelf()
                        }
                        .create()
                else -> throw IllegalArgumentException()
            }.apply { window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT) }.show()
        } catch (ignored: Exception) {
        }
        return START_NOT_STICKY
    }

    private fun startUninstall(packageName: String) {
        virtualCore.uninstallApp(packageName)
        systemService<NotificationManager>(Context.NOTIFICATION_SERVICE).notify(1, Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_install_request)
                .setContentTitle(getString(R.string.application_uninstalled))
                .setTicker(getString(R.string.application_uninstalled))
                .build())
    }

    private fun startInstall(path: Uri) {
        val notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_install_request)
                .setContentTitle(getString(R.string.installing))
                .setTicker(getString(R.string.installing))
                .setProgress(0, 0, true)

        systemService<NotificationManager>(Context.NOTIFICATION_SERVICE).notify(0, notification.build())

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
                        systemService<NotificationManager>(Context.NOTIFICATION_SERVICE).notify(0, notification.build())

                        virtualCore.preOpt(packageName)

                        notification
                                .setContentTitle(getString(R.string.install_finished_with_name, name))
                                .setTicker(getString(R.string.install_finished_with_name, name))
                                .addAction(Notification.Action(0, name,
                                        PendingIntent.getActivity(this@InstallService, 0,
                                                virtualCore.getLaunchIntent(packageName, VUserHandle.USER_OWNER),
                                                PendingIntent.FLAG_CANCEL_CURRENT)))
                                .setProgress(0, 0, false)
                        systemService<NotificationManager>(Context.NOTIFICATION_SERVICE).notify(0, notification.build())
                    }
                }
                else -> {
                    notification
                            .setContentTitle(getString(R.string.install_failed))
                            .setTicker(getString(R.string.install_failed))
                            .setContentText(error)
                            .setProgress(0, 0, false)
                    systemService<NotificationManager>(Context.NOTIFICATION_SERVICE).notify(0, notification.build())
                }
            }
        }
    }

    companion object {
        val INSTALL = "one.codehz.container.INSTALL"
        val UNINSTALL = "one.codehz.container.UNINSTALL"
        val KEY_PACKAGE_NAME by staticName
    }
}