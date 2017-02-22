package one.codehz.container.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MainDb(context: Context) : SQLiteOpenHelper(context, "main", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS log(" +
                "`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`package` TEXT NOT NULL," +
                "`data` TEXT NOT NULL);")
        db.execSQL("CREATE TABLE IF NOT EXISTS component(" +
                "`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`package` TEXT NOT NULL," +
                "`type` TEXT NOT NULL," +
                "`action` TEXT NOT NULL);")
        db.execSQL("CREATE TABLE IF NOT EXISTS clog(" +
                "`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`package` TEXT NOT NULL," +
                "`type` TEXT NOT NULL," +
                "`result` INTEGER NOT NULL," +
                "`action` TEXT NOT NULL);")
        db.execSQL("CREATE VIEW IF NOT EXISTS clog_view AS " +
                "SELECT clog._id AS `_id`, clog.package AS `package`, clog.type AS `type`, clog.action AS `action`, sum(clog.result) AS `result`, count(*) AS `count`, (CASE component.action WHEN clog.action THEN 1 ELSE 0 END) AS `restricted`, component._id AS `componentId` FROM clog " +
                "LEFT JOIN component ON " +
                "clog.package = component.package AND clog.type = component.type AND clog.action = component.action " +
                "GROUP BY clog.package, clog.action;")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //nothing to do
    }
}