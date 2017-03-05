package one.codehz.container

import android.app.Activity
import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.widget.AppCompatSpinner
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.lody.virtual.helper.utils.VLog
import one.codehz.container.base.BaseActivity
import one.codehz.container.ext.*
import one.codehz.container.models.AppModel
import java.io.File

class ShortcutMakerActivity : BaseActivity(R.layout.shortcut_maker) {
    companion object {
        val EXTRA_PACKAGE by staticName
        val OPEN_IMAGE_REQUEST = 0
        val EDIT_IMAGE_REQUEST = 1
        val OPEN_EXPORT_DIRECTORY = 2
    }

    val pkgName: String by lazy { intent.getStringExtra(EXTRA_PACKAGE) }
    val model: AppModel by lazy { AppModel(this, virtualCore.findApp(pkgName)) }
    val iconPreview by lazy<ImageView> { this[R.id.icon_preview] }
    val titleEdit by lazy<EditText> { this[R.id.title_edit] }
    val userSpinner by lazy<AppCompatSpinner> { this[R.id.user_spinner] }
    val imageSelector by lazy<Button> { this[R.id.icon_selector] }
    val imageExporter by lazy<Button> { this[R.id.icon_export] }

    var userId = 0
    var cacheBitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
        set(value) {
            if (field != model.icon.bitmap)
                field.recycle()
            field = value
            iconPreview.setImageDrawable(BitmapDrawable(resources, field))
        }
        get() = field

    val userSpinnerAdapter by lazy {
        ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, vUserManager.users.map { "${it.id}-${it.name}" })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(R.string.shortcut_maker_postfix, model.name)

        saveImage(model.icon.bitmap)
        titleEdit.setText(model.name, TextView.BufferType.EDITABLE)
        userSpinner.adapter = userSpinnerAdapter
        userSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) = Unit
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val s = parent.selectedItem as String
                userId = s.split('-').first().toInt()
            }
        }
        imageSelector.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).run {
                type = "image/*"
                Intent.createChooser(this, getString(R.string.select_image))
            }, OPEN_IMAGE_REQUEST)
        }
        imageExporter.setOnClickListener {
            val file = saveToCache(cacheBitmap)
            val uri = getUriFromFile(file)
            Snackbar.make(titleEdit, getString(R.string.icon_exported, file.absolutePath), Snackbar.LENGTH_LONG).setAction(R.string.open) {
                startActivityForResult(Intent(Intent.ACTION_VIEW).run {
                    setDataAndType(uri, contentResolver.getType(uri))
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                    Intent.createChooser(this, getString(R.string.select_image))
                }, EDIT_IMAGE_REQUEST)
            }.show()

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.shortcut_maker, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.check -> {
                sendBroadcast(Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent(this@ShortcutMakerActivity, VLoadingActivity::class.java).apply {
                        this.data = Uri.Builder().scheme("container").authority("launch").appendPath(model.packageName).fragment(userId.toString()).build()
                    })
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, titleEdit.text.toString())
                    putExtra(Intent.EXTRA_SHORTCUT_ICON, cacheBitmap)
                    putExtra("duplicate", true)
                })
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultIntent: Intent?) {
        when (requestCode) {
            OPEN_IMAGE_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = resultIntent?.data ?: throw IllegalStateException()
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    if (bitmap.width == bitmap.height) {
                        saveImage(bitmap)
                    } else
                        startActivityForResult(Intent(Intent.ACTION_EDIT).run {
                            type = "image/*"
                            data = uri
                            type = contentResolver.getType(uri)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            Intent.createChooser(this, getString(R.string.edit_image))
                        }, EDIT_IMAGE_REQUEST)
                }
            }
            EDIT_IMAGE_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = resultIntent?.data ?: throw IllegalStateException()
                    saveImage(MediaStore.Images.Media.getBitmap(contentResolver, uri))
                }
            }
        }
    }

    private fun saveToCache(bitmap: Bitmap): File {
        val file = externalCacheDir.resolve("${model.packageName}.png")
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return file
    }

    private fun saveImage(bitmap: Bitmap) {
        val density = resources.displayMetrics.density
        val size = systemService<ActivityManager>(Context.ACTIVITY_SERVICE).launcherLargeIconSize
        val width = (size * density + 0.5).toInt()
        cacheBitmap = Bitmap.createScaledBitmap(bitmap, width, width, false)
    }
}