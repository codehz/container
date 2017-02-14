package one.codehz.container

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import one.codehz.container.adapters.UserListAdapter
import one.codehz.container.base.BaseActivity
import one.codehz.container.ext.MakeLoaderCallbacks
import one.codehz.container.ext.get
import one.codehz.container.ext.staticName
import one.codehz.container.ext.vUserManager
import one.codehz.container.models.UserModel

class UserSelectorActivity : BaseActivity(R.layout.user_manager_activity) {
    companion object {
        val USER_LIST = 0
        val KEY_USER_ID by staticName
    }

    val contentList by lazy<RecyclerView> { this[R.id.content_list] }
    val fab by lazy<FloatingActionButton> { this[R.id.fab] }
    val linearLayoutManager by lazy { LinearLayoutManager(this) }

    val contentAdapter by lazy {
        UserListAdapter { model ->
            intent.putExtra(KEY_USER_ID, model.id)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
    val userListLoader by MakeLoaderCallbacks({ this }, { it() }) { contentAdapter.updateModels(vUserManager.users.map(::UserModel)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        initViews()

        supportLoaderManager.restartLoader(USER_LIST, null, userListLoader)
    }

    fun initViews() {
        fab.hide()

        with(contentList) {
            adapter = contentAdapter
            layoutManager = linearLayoutManager
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> false
    }
}