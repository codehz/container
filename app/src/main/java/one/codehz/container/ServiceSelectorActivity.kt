package one.codehz.container

import android.app.Activity
import android.content.ClipData
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.Loader
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.lody.virtual.helper.utils.VLog
import one.codehz.container.adapters.SimpleComponentListAdapter
import one.codehz.container.base.BaseActivity
import one.codehz.container.ext.MakeLoaderCallbacks
import one.codehz.container.ext.get
import one.codehz.container.ext.virtualCore
import one.codehz.container.models.AppModel
import one.codehz.container.models.SimpleComponentModel
import one.codehz.container.provider.MainProvider

class ServiceSelectorActivity : BaseActivity(R.layout.common_list_activity) {
    val contentList by lazy<RecyclerView> { this[R.id.content_list] }
    val packageInfo: PackageInfo? by lazy { packageManager.getPackageArchiveInfo(virtualCore.findApp(model.packageName).apkPath, PackageManager.GET_SERVICES) }
    val restrictedList: List<String> by lazy {
        contentResolver.query(MainProvider.COMPONENT_LOG_VIEW_URI, arrayOf("action"), "`package` = ? AND type = 'service' AND restricted <> 0", arrayOf(model.packageName), null).use {
            generateSequence { if (it.moveToNext()) it else null }.map { it.getString(0) }.toList()
        }
    }
    val contentAdapter by lazy {
        SimpleComponentListAdapter {
            enqueueDelete(it)
            supportLoaderManager.getLoader<Loader<*>>(0).forceLoad()
        }
    }
    val contentLoader by MakeLoaderCallbacks({ this }, { it() }) { _ ->
        packageInfo!!.services.map { it.name }.filter { it !in restrictedList }.map(::SimpleComponentModel).run {
            contentAdapter.updateModels(this)
        }
    }
    lateinit var model: AppModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pkgName = intent.getStringExtra("package")
        model = AppModel(this, virtualCore.findApp(pkgName))

        setResult(Activity.RESULT_CANCELED)

        title = model.name

        if (packageInfo == null || packageInfo?.services == null) {
            Toast.makeText(this, "Services list read failed", Toast.LENGTH_SHORT).show()
            return finish()
        }
        with(contentList) {
            adapter = contentAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(this@ServiceSelectorActivity)
        }
        supportLoaderManager.restartLoader(0, null, contentLoader)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.service_selector_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cancel -> {
                contentAdapter.clearDeleteQueue()
                supportLoaderManager.restartLoader(0, null, contentLoader)
            }
            R.id.check -> {
                VLog.d("X", contentAdapter.dumpDeleted().map { it.name }.toString())
                intent.putExtra("LIST", contentAdapter.dumpDeleted().map { it.name }.toTypedArray())
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        return true
    }
}