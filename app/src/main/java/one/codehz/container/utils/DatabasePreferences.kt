package one.codehz.container.utils

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

class DatabasePreferences(val context: Context, val uri: Uri, val keyName: String, val valueName: String, val staticField: Map<String, String>) : SharedPreferences {
    val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        if (listener != null)
            listeners += listener
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        if (listener != null)
            listeners -= listener
    }

    inline private fun <R> query(key: String?, fn: (Cursor) -> R)
            = context.contentResolver.query(
            uri,
            arrayOf(valueName),
            staticField.keys.fold("$keyName = ?") { s, k -> "$s AND $k = ?" },
            arrayOf(key, *staticField.map { it.value }.toTypedArray()),
            null
    ).use(fn)

    inline private fun <R> queryAll(fn: (Cursor) -> R)
            = context.contentResolver.query(
            uri,
            arrayOf(keyName, valueName),
            staticField.keys.fold("1 = 1") { s, k -> "$s AND $k = ?" },
            staticField.map { it.value }.toTypedArray(),
            null
    ).use(fn)

    private fun deleteAll() {
        context.contentResolver.delete(
                uri,
                staticField.keys.fold("1 = 1") { s, k -> "$s AND $k = ?" },
                staticField.map { it.value }.toTypedArray()
        )
    }

    private fun delete(key: String?) {
        context.contentResolver.delete(
                uri,
                staticField.keys.fold("$keyName = ?") { s, k -> "$s AND $k = ?" },
                arrayOf(key, *staticField.map { it.value }.toTypedArray())
        )
    }

    inline private fun insert(key: String?, fn: ContentValues.() -> Unit) {
        context.contentResolver.insert(uri, ContentValues().apply {
            put(keyName, key)
            staticField.forEach { put(it.key, it.value) }
            fn()
        })
    }

    override fun contains(key: String?) = query(key, Cursor::moveToNext)

    override fun getBoolean(key: String?, defValue: Boolean) = query(key) { if (it.moveToNext()) it.getInt(0) != 0 else defValue }

    override fun getInt(key: String?, defValue: Int) = query(key) { if (it.moveToNext()) it.getInt(0) else defValue }

    override fun getLong(key: String?, defValue: Long) = query(key) { if (it.moveToNext()) it.getLong(0) else defValue }

    override fun getFloat(key: String?, defValue: Float) = query(key) { if (it.moveToNext()) it.getFloat(0) else defValue }

    override fun getString(key: String?, defValue: String?) = query(key) { if (it.moveToNext()) it.getString(0) else defValue }

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?) = query(key) {
        if (it.moveToNext()) ObjectInputStream(it.getBlob(0).inputStream()).use(ObjectInputStream::readObject) as MutableSet<String> else defValues
    }

    override fun getAll() = queryAll {
        generateSequence { if (it.moveToNext()) it else null }
                .map { it.getString(0) to it.getString(1) }
                .toMap()
    }

    override fun edit() = Editor()

    inner class Editor : SharedPreferences.Editor {
        inline private fun runSelf(fn: () -> Unit): Editor {
            fn()
            return this
        }

        override fun clear() = runSelf { deleteAll() }

        override fun remove(key: String?) = runSelf { delete(key) }

        override fun putInt(key: String?, value: Int) = runSelf { insert(key) { put(valueName, value) } }

        override fun putLong(key: String?, value: Long) = runSelf { insert(key) { put(valueName, value) } }

        override fun putFloat(key: String?, value: Float) = runSelf { insert(key) { put(valueName, value) } }

        override fun putBoolean(key: String?, value: Boolean) = runSelf { insert(key) { put(valueName, if (value) 1 else 0) } }

        override fun putString(key: String?, value: String?) = runSelf { insert(key) { put(valueName, value) } }

        override fun putStringSet(key: String?, values: MutableSet<String>?) = runSelf {
            insert(key) {
                put(keyName, ByteArrayOutputStream()
                        .apply { ObjectOutputStream(this).use { it.writeObject(HashSet(values)) } }.toByteArray())
            }
        }

        override fun commit() = true

        override fun apply() = Unit
    }
}