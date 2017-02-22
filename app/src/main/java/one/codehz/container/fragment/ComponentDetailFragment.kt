package one.codehz.container.fragment

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
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

    val resictedList by lazy<RecyclerView> { view!![R.id.restricted_list] }
    val historyList by lazy<RecyclerView> { view!![R.id.history_list] }

    val resictedListAdapter by lazy {
        ComponentListAdapter(false)
    }
    val historyListAdapter by lazy {
        ComponentListAdapter(true)
    }

    val resictedListLoader by MakeLoaderCallbacks({ context }, { it() }) { ctx ->
        ctx.contentResolver.query(MainProvider.COMPONENT_URI, arrayOf("type", "regex"), "`package` = \"${model.packageName}\"", null, "type ASC").use {
            generateSequence { if (it.moveToNext()) it else null }.map { ComponentInfoModel(it.getString(1), it.getString(0), "") }.toList()
        }.run {
            resictedListAdapter.updateModels(this)
        }
    }
    val historyListLoader by MakeLoaderCallbacks({ context }, { it() }) { ctx ->
        ctx.contentResolver.query(
                MainProvider.COMPONENT_LOG_URI.buildUpon().appendQueryParameter("groupBy", "action").build(),
                arrayOf("type", "action", "count(result = 1)", "count(*)"),
                "`package` = \"${model.packageName}\"",
                null,
                "type ASC").use {
            generateSequence { if (it.moveToNext()) it else null }.map {
                ComponentInfoModel(it.getString(1), it.getString(0), "${it.getString(2)}/${it.getString(3)}")
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