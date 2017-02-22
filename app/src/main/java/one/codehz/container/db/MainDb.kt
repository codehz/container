package one.codehz.container.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MainDb(context: Context) : SQLiteOpenHelper(context, "main", null, 0) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS log(" +
                "`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`package` TEXT NOT NULL," +
                "`data` TEXT NOT NULL);")
        db.execSQL("CREATE TABLE IF NOT EXISTS component(" +
                "`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`type` TEXT NOT NULL," +
                "`regex` TEXT NOT NULL);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //nothing to do
    }
}