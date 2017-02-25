package one.codehz.container.fragment

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.Loader
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lody.virtual.helper.utils.VLog
import one.codehz.container.LoadingActivity
import one.codehz.container.MainActivity
import one.codehz.container.R
import one.codehz.container.adapters.RunningListAdapter
import one.codehz.container.ext.*
import one.codehz.container.interfaces.IFloatingActionTarget
import one.codehz.container.models.AppModel
import one.codehz.container.models.RunningAppModel

class RunningFragment : Fragment(), IFloatingActionTarget {
    companion object {
        val LIST_LOADER = 0
        val KILL_LOADER = 1
        val KILL_ALL_LOADER = 2
    }

    override val canBeFloatingActionTarget = true

    override fun onFloatingAction() {
        loaderManager.restartLoader(KILL_ALL_LOADER, null, killAllLoader)
        activityManager.appTasks.filter { it.taskInfo.baseIntent.action != Intent.ACTION_MAIN }.forEach {
            when (it.taskInfo.baseIntent.action) {
                Intent.ACTION_RUN ->
                    it.finishAndRemoveTask()
                else -> if (it.taskInfo.baseIntent.component.className.startsWith("com.lody.virtual.client.stub.StubActivity$")) {
                    it.finishAndRemoveTask()
                }
            }
        }
    }

    override fun getFloatingDrawable() = R.drawable.ic_clear_all

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(R.layout.content_main, container, false)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    val runningModelLoader by MakeLoaderCallbacks({ activity }, { it(); swipe_refresh_widget.isRefreshing = false }) { ctx ->
        contentAdapter.updateData(vUserManager.users.map { it.id }.map { id ->
            id to virtualCore.allApps.filter { virtualCore.isAppRunning(it.packageName, id) }.map { AppModel(ctx, it) }
        }.toMap().toSortedMap().toList().filter { it.second.isNotEmpty() })
    }

    val killedList = mutableListOf<Pair<Int, String>>()

    val killAppLoader by MakeLoaderCallbacks({ activity }, { loaderManager.getLoader<Loader<*>>(LIST_LOADER).forceLoad() }) {
        killedList.forEach {
            val (user, pkgName) = it
            virtualCore.killApp(pkgName, user)
            activityManager.appTasks
                    .filter { it.taskInfo.baseIntent.component.className.startsWith("com.lody.virtual.client.stub.StubActivity$") }
                    .filter { it.taskInfo.baseIntent.type.startsWith(pkgName + "/") }
                    .forEach(ActivityManager.AppTask::finishAndRemoveTask)
        }
        Thread.sleep(200)
    }

    val killAllLoader by MakeLoaderCallbacks({ activity }, { loaderManager.getLoader<Loader<*>>(LIST_LOADER).forceLoad() }) {
        killedList.clear()
        virtualCore.killAllApps()
        Thread.sleep(400)
    }

    val recycleView by lazy<RecyclerView> { view!![R.id.content_main] }
    val swipe_refresh_widget by lazy<SwipeRefreshLayout> { view!![R.id.swipe_refresh_widget] }
    val linearLayoutManager by lazy { LinearLayoutManager(activity) }
    val contentAdapter by lazy {
        RunningListAdapter({ runningModel: RunningAppModel, iconView: View, titleView: View ->
            LoadingActivity.launch(activity as Activity, runningModel.appModel, runningModel.userId, iconView, titleView)
        }, { uid ->
            killedList.addAll(virtualCore.allApps
                    .filter { virtualCore.isAppRunning(it.packageName, uid) }
                    .map { uid to it.packageName })
            loaderManager.restartLoader(KILL_LOADER, null, killAppLoader)
        })
    }
    var mSnackbar: Snackbar? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    override fun onResume() {
        super.onResume()

        contentAdapter.clear()
        loaderManager.restartLoader(LIST_LOADER, null, runningModelLoader)
        (activity as MainActivity).setTask(R.string.task_running)
    }

    fun initViews() {
        with(swipe_refresh_widget) {
            setOnRefreshListener {
                loaderManager.restartLoader(LIST_LOADER, null, runningModelLoader)
            }
            isRefreshing = true
        }

        with(recycleView) {
            adapter = contentAdapter
            layoutManager = linearLayoutManager
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
                override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?) = false

                override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                    return if (viewHolder is RunningListAdapter.ItemViewHolder) makeMovementFlags(0, ItemTouchHelper.END) else 0
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    contentAdapter.apply {
                        val itemViewHolder = viewHolder as RunningListAdapter.ItemViewHolder
                        val currentModel = itemViewHolder.currentModel!!
                        val deleteAction = enqueueDelete(currentModel)
                        var undo = false
                        mSnackbar = Snackbar.make(viewHolder.itemView, R.string.stopped, Snackbar.LENGTH_LONG)
                                .setBackground(ContextCompat.getColor(activity, R.color.colorPrimaryDark))
                                .setAction(R.string.undo) {
                                    undo = true
                                    deleteAction()
                                    loaderManager.restartLoader(LIST_LOADER, null, runningModelLoader)
                                }
                                .addCallback(object : Snackbar.Callback() {
                                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                        if (undo) return
                                        deleteAction()
                                        if (isAdded) {
                                            killedList += currentModel.userId to currentModel.appModel.packageName
                                            loaderManager.restartLoader(KILL_LOADER, null, killAppLoader)
                                        } else {
                                            virtualCore.killApp(currentModel.appModel.packageName, currentModel.userId)
                                        }
                                    }
                                })
                        mSnackbar!!.show()
                        loaderManager.restartLoader(LIST_LOADER, null, runningModelLoader)
                    }
                }

                override fun isLongPressDragEnabled() = false
            }).attachToRecyclerView(this)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mSnackbar?.dismiss()
    }

    override fun onPause() {
        super.onPause()
        mSnackbar?.dismiss()
    }
}