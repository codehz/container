package one.codehz.container.fragment

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import android.widget.ImageButton
import android.widget.TextView
import one.codehz.container.DetailActivity
import one.codehz.container.R
import one.codehz.container.ServiceSelectorActivity
import one.codehz.container.adapters.ComponentListAdapter
import one.codehz.container.ext.MakeLoaderCallbacks
import one.codehz.container.ext.get
import one.codehz.container.models.AppModel
import one.codehz.container.models.ComponentInfoModel
import one.codehz.container.provider.MainProvider

class ComponentDetailFragment(val model: AppModel) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(R.layout.component_detail, container, false)!!

    val hackList by lazy<RecyclerView> { view!![R.id.hack] }
    val historyList by lazy<RecyclerView> { view!![R.id.history_list] }
    val addButton by lazy<ImageButton> { view!![R.id.add_button] }
    val clearButton by lazy<TextView> { view!![R.id.clear_history] }
    val historyListAdapter by lazy {
        ComponentListAdapter(true) { item ->
            val (id, value, type) = item
            context.contentResolver.query(MainProvider.COMPONENT_URI, arrayOf("_id"), "`package` = ? AND `type` = ? AND `action` = ?", arrayOf(model.packageName, type, value), null).use {
                if (it.moveToNext()) {
                    context.contentResolver.delete(MainProvider.COMPONENT_URI.buildUpon().appendPath(it.getLong(0).toString()).build(), null, null)
                    loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
                } else {
                    context.contentResolver.insert(MainProvider.COMPONENT_URI, ContentValues().apply {
                        put("package", model.packageName)
                        put("type", type)
                        put("action", value)
                    })
                    loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
                }
            }
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

        with(historyList) {
            adapter = historyListAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
        }
        with(hackList) {
            adapter = ComponentListAdapter(true) {}
            layoutManager = LinearLayoutManager(context)
        }
        clearButton.setOnClickListener {
            context.contentResolver.delete(MainProvider.COMPONENT_LOG_URI, "package = ?", arrayOf(model.packageName))
            loaderManager.restartLoader(0, null, historyListLoader)
        }
        addButton.setOnClickListener {
            activity.startActivityForResult(Intent(context, ServiceSelectorActivity::class.java).apply {
                putExtra("package", model.packageName)
            }, DetailActivity.SELECT_SERVICES)
        }
    }

    val handler = Handler()

    override fun onResume() {
        super.onResume()
        loaderManager.restartLoader(0, null, historyListLoader)
    }
}