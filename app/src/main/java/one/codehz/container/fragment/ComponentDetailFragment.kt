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
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import one.codehz.container.R
import one.codehz.container.adapters.ComponentListAdapter
import one.codehz.container.ext.MakeLoaderCallbacks
import one.codehz.container.ext.get
import one.codehz.container.models.AppModel
import one.codehz.container.models.ComponentInfoModel
import one.codehz.container.provider.MainProvider

class ComponentDetailFragment(val model: AppModel, onSnack: (Snackbar) -> Unit) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(R.layout.component_detail, container, false)!!

    val resictedList by lazy<RecyclerView> { view!![R.id.restricted_list] }
    val historyList by lazy<RecyclerView> { view!![R.id.history_list] }

    val resictedListAdapter by lazy {
        ComponentListAdapter(false) { id, type, value ->
            AlertDialog.Builder(context)
                    .setView(R.layout.dialog_input)
                    .setTitle(getString(R.string.edit_regex))
                    .setPositiveButton(android.R.string.ok) { dialog, w ->
                        dialog as AlertDialog
                        val input = dialog.findViewById(R.id.input) as EditText
                        if (input.text.isEmpty())
                            context.contentResolver.delete(MainProvider.COMPONENT_URI.buildUpon().appendPath(id.toString()).build(), null, null)
                        else
                            context.contentResolver.update(MainProvider.COMPONENT_URI.buildUpon().appendPath(id.toString()).build(), ContentValues().apply {
                                put("action", input.text.toString())
                            }, null, null)
                        loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, w -> }
                    .show().apply {
                val input = findViewById(R.id.input) as EditText
                input.text = Editable.Factory.getInstance().newEditable(value)
            }
        }
    }
    val historyListAdapter by lazy {
        ComponentListAdapter(true) { id, type, value ->
            AlertDialog.Builder(context)
                    .setTitle(getString(R.string.prompt_resicted))
                    .setPositiveButton(android.R.string.ok) { dialog, w ->
                        context.contentResolver.insert(MainProvider.COMPONENT_URI, ContentValues().apply {
                            put("package", model.packageName)
                            put("type", type)
                            put("action", value)
                        })
                        loaderManager.getLoader<Loader<*>>(0)?.forceLoad()
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, w -> }
                    .show()
        }
    }

    val resictedListLoader by MakeLoaderCallbacks({ context }, { it() }) { ctx ->
        ctx.contentResolver.query(MainProvider.COMPONENT_URI, arrayOf("_id", "type", "action"), "`package` = ?", arrayOf(model.packageName), "type ASC").use {
            generateSequence { if (it.moveToNext()) it else null }.map { ComponentInfoModel(it.getLong(0), it.getString(2), it.getString(1), "") }.toList()
        }.run {
            resictedListAdapter.updateModels(this)
        }
    }
    val historyListLoader by MakeLoaderCallbacks({ context }, { it() }) { ctx ->
        ctx.contentResolver.query(
                MainProvider.COMPONENT_LOG_URI.buildUpon().appendQueryParameter("groupBy", "action").build(),
                arrayOf("_id", "type", "action", "sum(result)", "count(*)"),
                "`package` = ?",
                arrayOf(model.packageName),
                "type ASC").use {
            generateSequence { if (it.moveToNext()) it else null }.map {
                ComponentInfoModel(it.getLong(0), it.getString(2), it.getString(1), "${it.getString(3)}/${it.getString(4)}")
            }.toList()
        }.run {
            Log.d("CDF", this.toString())
            historyListAdapter.updateModels(this)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(resictedList) {
            adapter = resictedListAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
        }

        with(historyList) {
            adapter = historyListAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
        }

        loaderManager.restartLoader(0, null, resictedListLoader)
        loaderManager.restartLoader(1, null, historyListLoader)
    }

    override fun onResume() {
        super.onResume()

        loaderManager.restartLoader(0, null, resictedListLoader)
        loaderManager.restartLoader(1, null, historyListLoader)
    }
}