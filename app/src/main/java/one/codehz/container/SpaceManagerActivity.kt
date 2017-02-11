package one.codehz.container

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import one.codehz.container.adapters.SpaceManagerAdapter
import one.codehz.container.base.BaseActivity
import one.codehz.container.ext.MakeLoaderCallbacks
import one.codehz.container.ext.get
import one.codehz.container.models.SpaceManagerModel
import org.apache.commons.io.FileUtils
import java.io.File

class SpaceManagerActivity : BaseActivity(R.layout.space_manager) {
    val contentList by lazy<RecyclerView> { this[R.id.content_list] }
    val contentAdapter by lazy { SpaceManagerAdapter() }
    val linearLayoutManager by lazy { LinearLayoutManager(this) }

    companion object {
        val APP_DATA = 0
        val APP_OPT = 1
    }

    val appDataLoader by MakeLoaderCallbacks({ this }, { dataList[APP_DATA].amount = it; syncList() }) {
        val data = filesDir.parent
        FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectoryAsBigInteger(File("$data/virtual/data")))
    }

    val appOptLoader by MakeLoaderCallbacks({ this }, { dataList[APP_OPT].amount = it; syncList() }) {
        val data = filesDir.parent
        FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectoryAsBigInteger(File("$data/virtual/opt")))
    }

    val dataList by lazy {
        mutableListOf(
                SpaceManagerModel(getString(R.string.space_application_in_container), ManageContainerApplicationStorageActivity::class),
                SpaceManagerModel(getString(R.string.container_application_optimization_data), ManageContainerApplicationStorageActivity::class)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(contentList) {
            adapter = contentAdapter
            layoutManager = linearLayoutManager
            itemAnimator = DefaultItemAnimator()
        }

        syncList()

        loaderManager.restartLoader(APP_DATA, null, appDataLoader)
        loaderManager.restartLoader(APP_OPT, null, appOptLoader)
    }

    val syncList: () -> Unit
        get() = contentAdapter.updateModels(dataList.map { it.clone() })
}