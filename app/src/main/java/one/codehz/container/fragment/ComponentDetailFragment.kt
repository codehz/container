package one.codehz.container.fragment

import android.content.ContentValues
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import one.codehz.container.R
import one.codehz.container.adapters.ComponentListAdapter
import one.codehz.container.ext.MakeLoaderCallbacks
import one.codehz.container.ext.get
import one.codehz.container.models.AppModel
import one.codehz.container.models.ComponentInfoModel
import one.codehz.container.provider.MainProvider

class ComponentDetailFragment(val model: AppModel, onSnack: (Snackbar) -> Unit) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(R.layout.component_detail, container, false)!!

    val restrictedList by lazy<RecyclerView> { view!![R.id.restricted_list] }
    val historyList by lazy<RecyclerView> { view!![R.id.history_list] }

    val restrictedListAdapter by lazy {
        ComponentListAdapter(false) { id, type, value ->
            AlertDialog.Builder(context)
                    .setTitle(getString(R.string.prompt_delete_item))
                    .setPositiveButton(android.R.string.yes) { dialog, w ->
                        context.contentResolver.delete(MainProvider.COMPONENT_URI.buildUpon().appendPath(id.toString()).build(), null, null)
                        loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
                        loaderManager.getLoader<Loader<*>>(1)?.forceLoad()
                    }
                    .setNegativeButton(android.R.string.no) { dialog, w -> }
                    .show()
        }
    }
    val historyListAdapter by lazy {
        ComponentListAdapter(true) { id, type, value ->
            context.contentResolver.query(MainProvider.COMPONENT_LOG_URI.buildUpon().appendPath(id.toString()).build(), arrayOf("restricted", "componentId"), null, null, null).use {
                if (!it.moveToNext()) return@ComponentListAdapter
                if (it.getInt(0) != 0) {
                    val componentId = it.getLong(1)
                    AlertDialog.Builder(context)
                            .setTitle(getString(R.string.prompt_not_restricted))
                            .setPositiveButton(android.R.string.yes) { dialog, w ->
                                context.contentResolver.delete(MainProvider.COMPONENT_URI.buildUpon().appendPath(componentId.toString()).build(), null, null)
                                loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
                                loaderManager.getLoader<Loader<*>>(1)?.forceLoad()
                            }
                            .setNegativeButton(android.R.string.no) { dialog, w -> }
                            .show()
                } else
                    AlertDialog.Builder(context)
                            .setTitle(getString(R.string.prompt_restricted))
                            .setPositiveButton(android.R.string.ok) { dialog, w ->
                                context.contentResolver.insert(MainProvider.COMPONENT_URI, ContentValues().apply {
                                    put("package", model.packageName)
                                    put("type", type)
                                    put("action", value)
                                })
                                loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
                                loaderManager.getLoader<Loader<*>>(1)?.forceLoad()
                            }
                            .setNegativeButton(android.R.string.cancel) { dialog, w -> }
                            .show()
            }

        }
    }

    val restrictedListLoader by MakeLoaderCallbacks({ context }, { it() }) { ctx ->
        ctx.contentResolver.query(MainProvider.COMPONENT_URI, arrayOf("_id", "type", "action"), "`package` = ?", arrayOf(model.packageName), "type ASC").use {
            generateSequence { if (it.moveToNext()) it else null }.map { ComponentInfoModel(it.getLong(0), it.getString(2), it.getString(1), "") }.toList()
        }.run {
            restrictedListAdapter.updateModels(this)
        }
    }
    val historyListLoader by MakeLoaderCallbacks({ context }, { it() }) { ctx ->
        ctx.contentResolver.query(
                MainProvider.COMPONENT_LOG_URI,
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

        with(restrictedList) {
            adapter = restrictedListAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
        }

        with(historyList) {
            adapter = historyListAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
        }

        loaderManager.restartLoader(0, null, restrictedListLoader)
        loaderManager.restartLoader(1, null, historyListLoader)
    }

    override fun onResume() {
        super.onResume()

        loaderManager.restartLoader(0, null, restrictedListLoader)
        loaderManager.restartLoader(1, null, historyListLoader)
    }
}