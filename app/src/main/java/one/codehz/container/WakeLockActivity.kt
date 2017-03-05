package one.codehz.container

import android.app.Activity
import android.os.Bundle
import android.support.v4.content.Loader
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import one.codehz.container.adapters.AutoAdapter
import one.codehz.container.base.BaseActivity
import one.codehz.container.ext.*
import one.codehz.container.models.AppModel
import one.codehz.container.provider.MainProvider

class WakeLockActivity : BaseActivity(R.layout.common_list_activity) {
    val contentList by lazy<RecyclerView> { this[R.id.content_list] }

    val contentAdapter by lazy {
        AutoAdapter<WakeLockDataModel>(R.layout.wakelock_item) {
            if (it.restricted)
                contentResolver delete MainProvider.WAKELOCK_URI where mapOf("package" to model.packageName, "pattern" to it.pattern)
            else
                contentResolver insert MainProvider.WAKELOCK_URI values mapOf("package" to model.packageName, "pattern" to it.pattern)

            supportLoaderManager.getLoader<Loader<*>>(0)?.forceLoad()
        }
    }

    val contentLoader by MakeLoaderCallbacks({ this }, { it() }) { _ ->
        contentResolver query MainProvider.WAKELOCK_LOG_VIEW_URI select arrayOf("pattern", "restricted") where mapOf("package" to model.packageName) exec {
            it.asSequence().map { WakeLockDataModel(it.getString(0), if (it.getInt(1) != 0) getString(R.string.restricted) else "", it.getInt(1) != 0) }.toList()
        } then {
            contentAdapter.updateModels(this)
        }
    }

    lateinit var model: AppModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pkgName = intent.getStringExtra("package")
        model = AppModel(this, virtualCore.findApp(pkgName))

        setResult(Activity.RESULT_CANCELED)

        title = getString(R.string.wake_lock_postfix, model.name)

        with(contentList) {
            adapter = contentAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(this@WakeLockActivity)
        }
        supportLoaderManager.restartLoader(0, null, contentLoader)
    }

    data class WakeLockDataModel(
            @property:targetView<TextView>(R.id.main, TextView::class)
            val pattern: String,
            @property:targetView<TextView>(R.id.status, TextView::class)
            val info: String,
            val restricted: Boolean
    ) : AutoAdapterModel<WakeLockDataModel>()
}