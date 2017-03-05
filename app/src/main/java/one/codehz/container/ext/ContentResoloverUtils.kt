package one.codehz.container.ext

import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

infix fun ContentResolver.query(uri: Uri) = object : PendingQuery {
    override infix fun select(projection: Array<String>) = object : PendingSelect {
        var whereStr: String? = null
        var whereArgs: Array<String>? = null
        var sortOption: String? = null
        private val selectSelf = this
        override infix fun where(string: String) = object : PendingWhere<PendingSelect> {
            override infix fun args(args: Array<Any>?) = selectSelf.apply {
                whereStr = string
                whereArgs = args?.map(Any::toString)?.toTypedArray()
            }
        }

        override fun where(map: Map<String, *>) = apply {
            whereStr = map.map { it.key }.fold("1 = 1") { r, t -> "$r And `$t` = ?" }
            whereArgs = map.map { it.value }.map(Any?::toString).toTypedArray()
        }

        override infix fun sort(option: String) = apply {
            sortOption = option
        }

        override infix fun <R> exec(fn: (Cursor) -> R) = query(uri, projection, whereStr, whereArgs, sortOption).use(fn)
    }
}

infix fun ContentResolver.delete(uri: Uri) = object : PendingDelete {
    private val deleteSelf = this

    override fun where(string: String) = object : PendingWhere<PendingDelete> {
        override infix fun args(args: Array<Any>?) = deleteSelf.apply {
            val whereStr = string
            val whereArgs = args?.map(Any::toString)?.toTypedArray()

            delete(uri, whereStr, whereArgs)
        }
    }

    override fun where(map: Map<String, *>) {
        val whereStr = map.map { it.key }.fold("1 = 1") { r, t -> "$r And `$t` = ?" }
        val whereArgs = map.map { it.value }.map(Any?::toString).toTypedArray()

        delete(uri, whereStr, whereArgs)
    }
}

infix fun ContentResolver.insert(uri: Uri) = object : PendingInsert {
    override fun values(map: Map<String, *>) {
        insert(uri, ContentValues().also {
            map.forEach { k, v ->
                it.put(k, v.toString())
            }
        })
    }
}

interface PendingInsert {
    infix fun values(map: Map<String, *>)
}

interface PendingDelete {
    infix fun where(string: String): PendingWhere<PendingDelete>

    infix fun where(map: Map<String, *>)
}

interface PendingWhere<T> {
    infix fun args(args: Array<Any>?): T
}

interface PendingSelect {
    infix fun where(string: String): PendingWhere<PendingSelect>

    infix fun where(map: Map<String, *>): PendingSelect

    infix fun sort(option: String): PendingSelect

    infix fun <R> exec(fn: (Cursor) -> R): R
}

interface PendingQuery {
    infix fun select(projection: Array<String>): PendingSelect
}
