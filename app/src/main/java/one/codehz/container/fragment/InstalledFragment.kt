package one.codehz.container.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.Loader
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import com.lody.virtual.client.core.InstallStrategy
import com.lody.virtual.os.VUserHandle
import one.codehz.container.DetailActivity
import one.codehz.container.LoadingActivity
import one.codehz.container.MainActivity
import one.codehz.container.R
import one.codehz.container.adapters.AppListAdapter
import one.codehz.container.ext.*
import one.codehz.container.interfaces.IFloatingActionTarget
import one.codehz.container.models.AppModel
import java.io.File
import java.io.InputStream

class InstalledFragment : Fragment(), IFloatingActionTarget {
    val modelLoader by MakeLoaderCallbacks({ activity }, { it(); swipe_refresh_widget.isRefreshing = false }) { ctx ->
        contentAdapter.updateModels(virtualCore.allApps.filterNotNull().filter { it.packageName != "android" }.map { AppModel(ctx, it) })
    }

    val uninstallLoader by MakeLoaderCallbacks({ activity }, { loaderManager.getLoader<Loader<*>>(AppListLoaderId).forceLoad() }) { ctx ->
        uninstallPendingList.forEach {
            Thread.sleep(200)
            virtualCore.uninstallApp(it)
        }
    }

    companion object {
        val AppListLoaderId = 0
        val PkgUninstallLoaderId = 1
        val OPEN_APK_REQUEST: Int = 0
        val DETAIL_REQUEST: Int = 1
    }

    override val canBeFloatingActionTarget = true

    val uninstallPendingList = mutableListOf<String>()
    val installedList by lazy<RecyclerView> { view!![R.id.content_main] }
    val swipe_refresh_widget by lazy<SwipeRefreshLayout> { view!![R.id.swipe_refresh_widget] }
    val linearLayoutManager by lazy { LinearLayoutManager(activity) }
    val contentAdapter by lazy {
        AppListAdapter { appModel: AppModel, iconView: View, titleView: View, isLongPress: Boolean ->
            iconView.playSoundEffect(SoundEffectConstants.CLICK)
            if (isLongPress)
                iconView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            else
                iconView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (isLongPress xor sharedPreferences.getBoolean("switch_click_function", false))
                DetailActivity.launch(activity, appModel, iconView) { intent, bundle ->
                    startActivityForResult(intent, DETAIL_REQUEST, bundle)
                }
            else
                LoadingActivity.launch(activity as Activity, appModel, VUserHandle.USER_OWNER, iconView, titleView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) = inflater!!.inflate(R.layout.content_main, container, false)!!

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    override fun onResume() {
        super.onResume()

        loaderManager.restartLoader(AppListLoaderId, null, modelLoader)
        (activity as MainActivity).setTask(R.string.task_installed)
    }

    override fun onFloatingAction() {
        startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).run {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            Intent.createChooser(this, getString(R.string.select_apk))
        }, OPEN_APK_REQUEST)
    }

    override fun getFloatingDrawable() = R.drawable.ic_add

    fun initViews() {
        with(swipe_refresh_widget) {
            setOnRefreshListener {
                loaderManager.restartLoader(AppListLoaderId, null, modelLoader)
            }
            isRefreshing = true
        }

        with(installedList) {
            adapter = contentAdapter
            layoutManager = linearLayoutManager
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
                override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    viewHolder as AppListAdapter.AppListViewHolder
                    preUninstallApp(viewHolder.currentModel!!)
                }

                override fun isLongPressDragEnabled() = false
            }).attachToRecyclerView(this)
        }
    }

    fun preUninstallApp(currentModel: AppModel) {
        val deleteAction = contentAdapter.enqueueDelete(currentModel)
        var undo = false
        Snackbar.make(installedList, R.string.deleted, Snackbar.LENGTH_SHORT)
                .setBackground(ContextCompat.getColor(activity, R.color.colorPrimaryDark))
                .setAction(R.string.undo) {
                    undo = true
                    deleteAction()
                    loaderManager.restartLoader(AppListLoaderId, null, modelLoader)
                }
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        if (undo) return
                        deleteAction()
                        if (isAdded) {
                            uninstallPendingList.add(currentModel.packageName)
                            loaderManager.restartLoader(PkgUninstallLoaderId, null, uninstallLoader)
                        } else {
                            virtualCore.uninstallApp(currentModel.packageName)
                        }
                    }
                }).show()
        loaderManager.restartLoader(AppListLoaderId, null, modelLoader)
    }

    fun installFromStream(vararg params: Pair<InputStream, Long>) {
        with(activity) {
            runAsync<Pair<InputStream, Long>, Unit> { data ->
                val (stream, size) = data
                object {
                    val dialog: ProgressDialog by lazy {
                        ProgressDialog(activity).apply {
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
                                    AppModel(this@with, virtualCore.findApp(packageName)).apply {
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
                                        AlertDialog.Builder(this@with)
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

                Snackbar.make(installedList, getString(R.string.install_finished), Snackbar.LENGTH_SHORT).setBackground(ContextCompat.getColor(activity, R.color.colorPrimaryDark)).show()
                this@InstalledFragment.loaderManager.restartLoader(AppListLoaderId, null, modelLoader)
            }.execute(*params)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            OPEN_APK_REQUEST -> when (resultCode) {
                Activity.RESULT_OK -> intent?.apply {
                    installFromStream(*when {
                        data != null -> listOf(data)
                        clipData != null -> clipData.run { (0..itemCount - 1).map { getItemAt(it).uri } }
                        else -> listOf()
                    }.map { activity.contentResolver.openFileDescriptor(it, "r") }.map { ParcelFileDescriptor.AutoCloseInputStream(it) to it.statSize }.toTypedArray())
                }
                else -> Snackbar.make(installedList, getString(R.string.invalid_apk), Snackbar.LENGTH_SHORT).setBackground(ContextCompat.getColor(activity, R.color.colorPrimaryDark)).show()
            }
            DETAIL_REQUEST -> when (resultCode) {
                DetailActivity.RESULT_DELETE_APK -> {
                    intent?.apply {
                        preUninstallApp(AppModel(activity, virtualCore.findApp(data.path.substring(1))))
                    }
                }
            }
        }
    }
}