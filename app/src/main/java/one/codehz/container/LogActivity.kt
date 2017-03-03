package one.codehz.container

import android.content.ClipData
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import one.codehz.container.adapters.LogListAdapter
import one.codehz.container.base.BaseActivity
import one.codehz.container.ext.*
import one.codehz.container.models.AppModel
import one.codehz.container.models.LogModel
import one.codehz.container.provider.MainProvider

class LogActivity : BaseActivity(R.layout.activity_log) {
    val logList by lazy<RecyclerView> { this[R.id.content_list] }
    val logListAdapter by lazy {
        LogListAdapter { _, value ->
            clipboardManager.primaryClip = ClipData.newPlainText("log", value)
            Snackbar.make(logList, getString(R.string.log_copied), Snackbar.LENGTH_SHORT).setBackground(ContextCompat.getColor(this, R.color.colorPrimaryDark)).show()
        }
    }
    val logLoader by MakeLoaderCallbacks({ this }, { it() }) { ctx ->
        ctx.contentResolver.query(MainProvider.LOG_URI, arrayOf("time", "data"), "`package` = \"${model.packageName}\"", null, "time ASC").use {
            generateSequence { if (it.moveToNext()) it else null }.map { LogModel(it.getString(0), it.getString(1)) }.toList()
        }.run {
            logListAdapter.updateModels(this)
        }
    }

    lateinit var model: AppModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = AppModel(this, virtualCore.findApp(intent.data.path.substring(1)))

        with(logList) {
            adapter = logListAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(this@LogActivity)
        }

        supportLoaderManager.restartLoader(0, null, logLoader)
    }
}