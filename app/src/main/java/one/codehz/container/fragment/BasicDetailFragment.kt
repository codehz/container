package one.codehz.container.fragment

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.lody.virtual.helper.utils.VLog
import one.codehz.container.LogActivity
import one.codehz.container.R
import one.codehz.container.adapters.LogListAdapter
import one.codehz.container.adapters.PropertyListAdapter
import one.codehz.container.ext.MakeLoaderCallbacks
import one.codehz.container.ext.clipboardManager
import one.codehz.container.ext.get
import one.codehz.container.ext.virtualCore
import one.codehz.container.models.AppModel
import one.codehz.container.models.AppPropertyModel
import one.codehz.container.models.LogModel
import one.codehz.container.provider.MainProvider

class BasicDetailFragment(val model: AppModel, onSnack: (Snackbar) -> Unit) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(R.layout.application_basic_detail, container, false)!!

    val contentList by lazy<RecyclerView> { view!![R.id.content_list] }
    val logList by lazy<RecyclerView> { view!![R.id.log_list] }
    val logManagerButton by lazy<TextView> { view!![R.id.log_manager] }
    val clearLogButton by lazy<TextView> { view!![R.id.clear_log] }

    val propertyLoader by MakeLoaderCallbacks({ context }, { it() }) { ctx ->
        contentAdapter.updateModels(AppPropertyModel(model).getItems())
    }
    val logLoader by MakeLoaderCallbacks({ context }, { it() }) { ctx ->
        ctx.contentResolver.query(MainProvider.LOG_URI, arrayOf("time", "data"), "`package` = \"${model.packageName}\"", null, "time ASC limit 5").use {
            generateSequence { if (it.moveToNext()) it else null }.map { LogModel(it.getString(0), it.getString(1)) }.toList()
        }.run {
            logListAdapter.updateModels(this)
        }
    }
    val contentAdapter by lazy {
        PropertyListAdapter<AppPropertyModel> { key, value ->
            clipboardManager.primaryClip = ClipData.newPlainText(key, value)
            onSnack(Snackbar.make(contentList, virtualCore.context.getString(R.string.value_copied, key), Snackbar.LENGTH_SHORT))
        }
    }
    val logListAdapter by lazy {
        LogListAdapter { time, value ->
            clipboardManager.primaryClip = ClipData.newPlainText("log", value)
            onSnack(Snackbar.make(contentList, getString(R.string.log_copied), Snackbar.LENGTH_SHORT))
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    fun initView() {
        with(contentList) {
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context)
        }
        with(logList) {
            adapter = logListAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
        }

        logManagerButton.setOnClickListener {
            startActivity(Intent(context, LogActivity::class.java).apply {
                data = Uri.Builder().scheme("log").authority("container").path(model.packageName).build()
            })
        }

        clearLogButton.setOnClickListener {
            context.contentResolver.delete(MainProvider.LOG_URI, "`package` = \"${model.packageName}\"", null)
            loaderManager.restartLoader(1, null, logLoader)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        VLog.d("BDF", "vis")
        if (isVisibleToUser && isAdded) {
            loaderManager.restartLoader(0, null, propertyLoader)
            loaderManager.restartLoader(1, null, logLoader)
        }
    }

    override fun onResume() {
        super.onResume()
        VLog.d("BDF", "res")
        loaderManager.restartLoader(0, null, propertyLoader)
        loaderManager.restartLoader(1, null, logLoader)
    }
}