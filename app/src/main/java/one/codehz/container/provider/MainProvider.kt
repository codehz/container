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
        val TYPE_LOG_ALL = 0
        val TYPE_LOG_SINGLE = 1
        val TYPE_COMPONENT = 2
        val TYPE_COMPONENT_ITEM = 3
        val TYPE_COMPONENT_LOG = 4
        val TYPE_COMPONENT_LOG_ITEM = 5

        val AUTHORITY = "one.codehz.container.provider.main"
        val LOG_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("log").build()!!
        val COMPONENT_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("component").build()!!
        val COMPONENT_LOG_URI = Uri.Builder().scheme("content").authority(AUTHORITY).appendPath("clog").build()!!

        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "log", TYPE_LOG_ALL)
            addURI(AUTHORITY, "log/#", TYPE_LOG_SINGLE)
            addURI(AUTHORITY, "component", TYPE_COMPONENT)
            addURI(AUTHORITY, "component/#", TYPE_COMPONENT_ITEM)
            addURI(AUTHORITY, "clog", TYPE_COMPONENT_LOG)
            addURI(AUTHORITY, "clog/#", TYPE_COMPONENT_LOG_ITEM)
        }
    }

    private lateinit var dbHelper:MainDb

    override fun onCreate(): Boolean {
        dbHelper = MainDb(this.context)
        return true
    }

    private fun getMime(isDir: Boolean, name: String) = "vnd.android.cursor.${if (isDir) "dir" else "item"}/vnd.one.codehz.container.$name"

    override fun getType(uri: Uri?) = when (uriMatcher.match(uri)) {
        TYPE_LOG_ALL -> getMime(true, "log")
        TYPE_LOG_SINGLE -> getMime(false, "log")
        TYPE_COMPONENT -> getMime(true, "component")
        TYPE_COMPONENT_ITEM -> getMime(false, "component")
        TYPE_COMPONENT_LOG -> getMime(true, "clog")
        TYPE_COMPONENT_LOG_ITEM -> getMime(false, "clog")
        else -> throw IllegalArgumentException("Unsupported URI: $uri")
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        val name = when (uriMatcher.match(uri)) {
            TYPE_LOG_ALL -> "log"
            TYPE_COMPONENT -> "component"
            TYPE_COMPONENT_LOG -> "clog"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
        val id = dbHelper.writableDatabase.insert(name, null, values)
        context.contentResolver.notifyChange(uri, null)
        return uri!!.buildUpon().appendPath(id.toString()).build()
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        val (name, newSelection) = when (uriMatcher.match(uri)) {
            TYPE_LOG_ALL -> "log" to selection
            TYPE_LOG_SINGLE -> "log" to "_id=${uri!!.pathSegments.last()}" + if(selection.isNullOrEmpty()) "" else " AND ($selection)"
            TYPE_COMPONENT -> "component" to selection
            TYPE_COMPONENT_ITEM -> "component" to "_id=${uri!!.pathSegments.last()}" + if(selection.isNullOrEmpty()) "" else " AND ($selection)"
            TYPE_COMPONENT_LOG -> "clog" to selection
            TYPE_COMPONENT_LOG_ITEM -> "clog" to "_id=${uri!!.pathSegments.last()}" + if(selection.isNullOrEmpty()) "" else " AND ($selection)"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
        val res = dbHelper.writableDatabase.delete(name, newSelection, selectionArgs)
        context.contentResolver.notifyChange(uri, null)
        return res
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val (name, newSelection) = when (uriMatcher.match(uri)) {
            TYPE_LOG_ALL -> "log" to selection
            TYPE_LOG_SINGLE -> "log" to "_id=${uri!!.pathSegments.last()}" + if(selection.isNullOrEmpty()) "" else " AND ($selection)"
            TYPE_COMPONENT -> "component" to selection
            TYPE_COMPONENT_ITEM -> "component" to "_id=${uri!!.pathSegments.last()}" + if(selection.isNullOrEmpty()) "" else " AND ($selection)"
            TYPE_COMPONENT_LOG -> "clog" to selection
            TYPE_COMPONENT_LOG_ITEM -> "clog" to "_id=${uri!!.pathSegments.last()}" + if(selection.isNullOrEmpty()) "" else " AND ($selection)"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
        val res = dbHelper.writableDatabase.update(name, values, newSelection, selectionArgs)
        context.contentResolver.notifyChange(uri, null)
        return res
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val (name, targetId) = when (uriMatcher.match(uri)) {
            TYPE_LOG_ALL -> "log" to null
            TYPE_LOG_SINGLE -> "log" to uri.lastPathSegment
            TYPE_COMPONENT -> "component" to null
            TYPE_COMPONENT_ITEM -> "component" to uri.lastPathSegment
            TYPE_COMPONENT_LOG -> "clog" to null
            TYPE_COMPONENT_LOG_ITEM -> "clog" to uri.lastPathSegment
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