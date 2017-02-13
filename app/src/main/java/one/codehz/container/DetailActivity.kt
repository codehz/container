package one.codehz.container

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.graphics.Palette
import android.support.v7.widget.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.lody.virtual.os.VUserHandle
import one.codehz.container.adapters.PropertyListAdapter
import one.codehz.container.base.BaseActivity
import one.codehz.container.ext.*
import one.codehz.container.models.AppModel
import one.codehz.container.models.AppPropertyModel

class DetailActivity : BaseActivity(R.layout.application_detail) {
    companion object {
        val RESULT_DELETE_APK = 1
        val REQUEST_USER = 0
        val REQUEST_USER_FOR_SHORTCUT = 1

        fun launch(context: Activity, appModel: AppModel, iconView: View, startFn: (Intent, Bundle) -> Unit) {
            startFn(Intent(context, DetailActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.Builder().scheme("container").authority("detail").appendPath(appModel.packageName).build()
                Log.d("DA", dataString)
            }, ActivityOptions.makeSceneTransitionAnimation(context, iconView, "app_icon").toBundle())
        }
    }

    val package_name: String by lazy { intent.data.path.substring(1) }
    val model by lazy { AppModel(this, virtualCore.findApp(package_name)) }

    val listLoader by MakeLoaderCallbacks({ this }, { it() }) { ctx ->
        contentAdapter.updateModels(AppPropertyModel(model).getItems().onEach { Log.d("DA", it.key) })
    }

    val iconView by lazy<ImageView> { this[R.id.icon] }
    val contentList by lazy<RecyclerView> { this[R.id.content_list] }
    val collapsingToolbar by lazy<CollapsingToolbarLayout> { this[R.id.collapsing_toolbar] }
    var bgcolor = 0

    val contentAdapter by lazy {
        PropertyListAdapter<AppPropertyModel>()
    }
    val linearLayoutManager by lazy { LinearLayoutManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        postponeEnterTransition()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        loaderManager.initLoader(0, null, listLoader).forceLoad()

        iconView.setImageDrawable(model.icon)
        Palette.from(model.icon.bitmap).apply { maximumColorCount(1) }.generate { palette ->
            try {
                val (main_color) = palette.swatches
                val dark_color = Color.HSVToColor(floatArrayOf(0f, 0f, 0f).apply { Color.colorToHSV(main_color.rgb, this) }.apply { this[2] *= 0.8f })

                window.statusBarColor = dark_color
                window.navigationBarColor = dark_color
                bgcolor = dark_color
                collapsingToolbar.background = ColorDrawable(main_color.rgb)

                setTaskDescription(ActivityManager.TaskDescription(getString(R.string.task_detail_prefix, model.name), model.icon.bitmap, dark_color))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                startPostponedEnterTransition()
            }
        }

        with(contentList) {
            adapter = contentAdapter
            layoutManager = linearLayoutManager
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(DividerItemDecoration(this@DetailActivity, OrientationHelper.HORIZONTAL))
            setHasFixedSize(false)
        }

        collapsingToolbar.title = model.name
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                Log.d("DA", "HOME")
                finishAfterTransition()
                true
            }
            R.id.run -> {
                LoadingActivity.launch(this, model, VUserHandle.USER_OWNER, iconView)
                true
            }
            R.id.run_as -> {
                startActivityForResult(Intent(this, UserSelectorActivity::class.java), REQUEST_USER)
                true
            }
            R.id.uninstall -> {
                AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_alert)
                        .setTitle("Do you really want to delete this application?")
                        .setPositiveButton("Uninstall") { dialog, id ->
                            setResult(RESULT_DELETE_APK, intent)
                            finishAfterTransition()
                        }
                        .setNegativeButton("Cancel") { dialog, id -> }
                        .setOnDismissListener {
                            Snackbar.make(iconView, "Uninstalling is canceled.", Snackbar.LENGTH_SHORT).setBackground(bgcolor).show()
                        }
                        .show()
                true
            }
            R.id.send_to_desktop -> {
                startActivityForResult(Intent(this, UserSelectorActivity::class.java), REQUEST_USER_FOR_SHORTCUT)
                true
            }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_USER -> if (resultCode == Activity.RESULT_OK) {
                data!!
                LoadingActivity.launch(this, model, data.getIntExtra(UserSelectorActivity.KEY_USER_ID, 0), iconView)
            }
            REQUEST_USER_FOR_SHORTCUT -> if (resultCode == Activity.RESULT_OK) {
                data!!
                Log.d("DA", data.toString())
                sendBroadcast(Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent(this@DetailActivity, VLoadingActivity::class.java).apply {
                        this.data = Uri.Builder().scheme("container").authority("launch").appendPath(model.packageName).fragment(data.getIntExtra(UserSelectorActivity.KEY_USER_ID, 0).toString()).build()
                    })
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, model.name)
                    putExtra(Intent.EXTRA_SHORTCUT_ICON, model.icon.bitmap)
                    putExtra("duplicate", false)
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loaderManager.restartLoader(0, null, listLoader)
    }
}