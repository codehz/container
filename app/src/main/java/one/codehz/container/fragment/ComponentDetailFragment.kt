package one.codehz.container.fragment

import android.content.ContentValues
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.Loader
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import one.codehz.container.R
import one.codehz.container.adapters.ComponentListAdapter
import one.codehz.container.ext.MakeLoaderCallbacks
import one.codehz.container.ext.get
import one.codehz.container.ext.virtualCore
import one.codehz.container.models.AppModel
import one.codehz.container.models.ComponentInfoModel
import one.codehz.container.provider.MainProvider

class ComponentDetailFragment(val model: AppModel, onSnack: (Snackbar) -> Unit) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(R.layout.component_detail, container, false)!!

    val clearButton by lazy<Button> { view!![R.id.clear_history] }
    val emptyList by lazy<RecyclerView> { view!![R.id.empty_list] }
    val staticComponentList by lazy<RecyclerView> { view!![R.id.manifest_component_list] }
    val historyList by lazy<RecyclerView> { view!![R.id.history_list] }

    val staticComponentListAdapter by lazy {
        ComponentListAdapter(true) { id, type, value ->
            if (id != 0L) {
                context.contentResolver.delete(MainProvider.COMPONENT_URI.buildUpon().appendPath(id.toString()).build(), null, null)
                loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
                loaderManager.getLoader<Loader<*>>(1)?.forceLoad()
            } else {
                context.contentResolver.insert(MainProvider.COMPONENT_URI, ContentValues().apply {
                    put("package", model.packageName)
                    put("type", type)
                    put("action", value)
                })
                loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
                loaderManager.getLoader<Loader<*>>(1)?.forceLoad()
            }
        }
    }
    val historyListAdapter by lazy {
        ComponentListAdapter(true) { id, type, value ->
            context.contentResolver.query(MainProvider.COMPONENT_URI, arrayOf("_id"), "`package` = ? AND `type` = ? AND `action` = ?", arrayOf(model.packageName, type, value), null).use {
                if (it.moveToNext()) {
                    context.contentResolver.delete(MainProvider.COMPONENT_URI.buildUpon().appendPath(it.getLong(0).toString()).build(), null, null)
                    loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
                    loaderManager.getLoader<Loader<*>>(1)?.forceLoad()
                } else {
                    context.contentResolver.insert(MainProvider.COMPONENT_URI, ContentValues().apply {
                        put("package", model.packageName)
                        put("type", type)
                        put("action", value)
                    })
                    loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
                    loaderManager.getLoader<Loader<*>>(1)?.forceLoad()
                }
            }
        }
    }

    val packageInfo: PackageInfo? by lazy { context.packageManager.getPackageArchiveInfo(virtualCore.findApp(model.packageName).apkPath, PackageManager.GET_SERVICES) }

    val staticComponentListLoader by MakeLoaderCallbacks({ context }, { it?.invoke() }) { ctx ->
        val map = ctx.contentResolver.query(MainProvider.COMPONENT_LOG_VIEW_URI, arrayOf("action", "_id"), "`package` = ? AND type = 'service' AND restricted <> 0", arrayOf(model.packageName), null).use {
            generateSequence { if (it.moveToNext()) it else null }.map { it.getString(0) to it.getLong(1) }.toMap()
        }
        packageInfo?.services?.map { ComponentInfoModel(map.getOrElse(it.name) { 0L }, it.name, "service", if (it.name in map) getString(R.string.restricted) else "") }?.run {
            staticComponentListAdapter.updateModels(this)
        }
    }
    val historyListLoader by MakeLoaderCallbacks({ context }, { it() }) { ctx ->
        ctx.contentResolver.query(
                MainProvider.COMPONENT_LOG_VIEW_URI,
                arrayOf("_id", "type", "action", "result", "count", "restricted"),
                "`package` = ?",
                arrayOf(model.packageName),
                "type ASC").use {
            generateSequence { if (it.moveToNext()) it else null }.map {
                ComponentInfoModel(it.getLong(0), it.getString(2), it.getString(1), "${if (it.getInt(5) != 0) "${getString(R.string.restricted)} " else ""}${it.getString(3)}/${it.getString(4)}")
            }.toList()
        }.run {
            historyListAdapter.updateModels(this)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //fix layout bug
        with(emptyList) {
            adapter = ComponentListAdapter(true) { a, b, c -> }
            layoutManager = LinearLayoutManager(context)
        }
        with(staticComponentList) {
            adapter = staticComponentListAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
        }
        with(historyList) {
            adapter = historyListAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
        }
        clearButton.setOnClickListener {
            context.contentResolver.delete(MainProvider.COMPONENT_LOG_URI, "`package` = ?", arrayOf(model.packageName))
            loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
            loaderManager.getLoader<Loader<*>>(1)?.forceLoad()
        }
        loaderManager.restartLoader(0, null, historyListLoader)
        loaderManager.restartLoader(1, null, staticComponentListLoader)
    }

    override fun onResume() {
        super.onResume()
        loaderManager.restartLoader(0, null, historyListLoader)
    }
}