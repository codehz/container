package one.codehz.container.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import com.lody.virtual.helper.utils.VLog
import one.codehz.container.db.MainDb

class MainProvider : ContentProvider() {
    companion object {
        val TYPE_LIST = 0
        val TYPE_ITEM = 1

        val AUTHORITY = "one.codehz.container.provider.main"
        val LOG_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("log").build()!!
        val COMPONENT_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("component").build()!!
        val COMPONENT_LOG_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("clog").build()!!
        val COMPONENT_LOG_VIEW_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("clog_view").build()!!

        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "*", TYPE_LIST)
            addURI(AUTHORITY, "*/#", TYPE_ITEM)
        }
    }

    private lateinit var dbHelper:MainDb

    override fun onCreate(): Boolean {
        dbHelper = MainDb(this.context)
        return true
    }

    private fun getMime(isDir: Boolean, name: String) = "vnd.android.cursor.${if (isDir) "dir" else "item"}/vnd.one.codehz.container.$name"

    override fun getType(uri: Uri) = when (uriMatcher.match(uri)) {
        TYPE_LIST -> getMime(true, uri.pathSegments.first())
        TYPE_ITEM -> getMime(false, uri.pathSegments.first())
        else -> throw IllegalArgumentException("Unsupported URI: $uri")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val name = when (uriMatcher.match(uri)) {
            TYPE_LIST -> uri.pathSegments.first()
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
        val id = dbHelper.writableDatabase.insert(name, null, values)
        context.contentResolver.notifyChange(uri, null)
        return uri.buildUpon().appendPath(id.toString()).build()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val (name, newSelection) = when (uriMatcher.match(uri)) {
            TYPE_LIST -> uri.pathSegments.first() to selection
            TYPE_ITEM -> uri.pathSegments.first() to "_id=${uri.pathSegments.last()}" + if(selection.isNullOrEmpty()) "" else " AND ($selection)"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
        val res = dbHelper.writableDatabase.delete(name, newSelection, selectionArgs)
        context.contentResolver.notifyChange(uri, null)
        return res
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val (name, newSelection) = when (uriMatcher.match(uri)) {
            TYPE_LIST -> uri.pathSegments.first() to selection
            TYPE_ITEM -> uri.pathSegments.first() to "_id=${uri.pathSegments.last()}" + if(selection.isNullOrEmpty()) "" else " AND ($selection)"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
        val res = dbHelper.writableDatabase.update(name, values, newSelection, selectionArgs)
        context.contentResolver.notifyChange(uri, null)
        return res
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val (name, targetId) = when (uriMatcher.match(uri)) {
            TYPE_LIST -> uri.pathSegments.first() to null
            TYPE_ITEM -> uri.pathSegments.first() to uri.lastPathSegment
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
        return dbHelper.readableDatabase.let {
            SQLiteQueryBuilder().apply {
                tables = name
                targetId?.apply { appendWhere("_id=$this") }
            }.query(it, projection, selection, selectionArgs, uri.getQueryParameter("groupBy"), uri.getQueryParameter("having"), sortOrder)
        }
    }
}