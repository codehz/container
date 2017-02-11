package one.codehz.container

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.util.Log
import com.lody.virtual.client.core.InstallStrategy
import one.codehz.container.ext.runAsync
import one.codehz.container.ext.streamTransfer
import one.codehz.container.ext.virtualCore
import one.codehz.container.models.AppModel
import java.io.File
import java.io.InputStream

class InstallerActivity : Activity() {
    companion object {
        var uninstallScheme = "package:"
        fun Context.installFromStream(params: Array<Pair<InputStream, Long>>, finish: () -> Unit) {
            runAsync<Pair<InputStream, Long>, Unit> { data ->
                val (stream, size) = data
                object {
                    val dialog: ProgressDialog by lazy {
                        ProgressDialog(this@installFromStream).apply {
                            setTitle(R.string.application_installing)
                            setMessage(getString(R.string.receive_apk_file))
                            isIndeterminate = false
                            max = size.toInt()
                            setCancelable(false)
                            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                            show()
                        }
                    }

                    fun start() {
                        stream.use { external ->
                            openFileOutput("temp.apk", Context.MODE_PRIVATE).use { internal ->
                                streamTransfer(external to internal, 8192) {
                                    ui {
                                        dialog.progress = it.toInt()
                                    }
                                }
                            }
                        }
                        ui {
                            dialog.isIndeterminate = true
                            dialog.setMessage(getString(R.string.parsing_application))
                        }

                        return virtualCore.installApp("$filesDir/temp.apk", InstallStrategy.UPDATE_IF_EXIST).run {
                            when {
                                isSuccess -> {
                                    AppModel(this@installFromStream, virtualCore.findApp(packageName)).apply {
                                        ui {
                                            dialog.setIcon(icon)
                                            dialog.setTitle(name)
                                            dialog.setMessage(getString(R.string.optimize_application))
                                        }
                                    }

                                    virtualCore.preOpt(packageName)

                                    ui {
                                        dialog.dismiss()
                                    }
                                }
                                else -> {
                                    ui {
                                        dialog.dismiss()
                                        AlertDialog.Builder(this@installFromStream)
                                                .setIcon(R.drawable.ic_alert)
                                                .setTitle(R.string.install_failed)
                                                .setMessage(error)
                                                .show()
                                    }
                                }
                            }
                        }
                    }
                }.start()
            }.then { unit ->
                File("$filesDir/temp.apk").delete()
                finish()
            }.execute(*params)

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == null || intent.data == null) finish()

        when (intent.action) {
            "android.intent.action.VIRTUAL_INSTALL_PACKAGE", "android.intent.action.INSTALL_PACKAGE" -> {
                Log.d("IA", intent.dataString)
                installFromStream(arrayOf(contentResolver.openFileDescriptor(intent.data, "r").let { ParcelFileDescriptor.AutoCloseInputStream(it) to it.statSize })) {
                    finish()
                }
            }

            "android.intent.action.VIRTUAL_UNINSTALL_PACKAGE" -> {
                virtualCore.uninstallApp(intent.dataString.substring(uninstallScheme.length))
                AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_alert)
                        .setTitle(getString(R.string.advance_virtual_package_installer))
                        .setMessage(getString(R.string.application_uninstalled))
                        .setOnDismissListener {
                            finish()
                        }
                        .show()
            }
        }
    }


}