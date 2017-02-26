package one.codehz.container.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PreferenceDb(context: Context) : SQLiteOpenHelper(context, "preference", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE pre_package(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "package TEXT NOT NULL," +
                "key TEXT NOT NULL," +
                "value INTEGER NOT NULL," +
                "UNIQUE (`package`, `key`) ON CONFLICT REPLACE);")
        db.execSQL("CREATE TABLE pre_user(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "user INTEGER NOT NULL," +
                "key TEXT NOT NULL," +
                "value INTEGER NOT NULL," +
                "UNIQUE (`user`, `key`) ON CONFLICT REPLACE);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}