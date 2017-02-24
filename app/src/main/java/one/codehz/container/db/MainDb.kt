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
                "`action` TEXT NOT NULL," +
                "UNIQUE (`package`, `type`, `action`) ON CONFLICT REPLACE);")
        db.execSQL("CREATE TABLE IF NOT EXISTS clog(" +
                "`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`package` TEXT NOT NULL," +
                "`type` TEXT NOT NULL," +
                "`result` INTEGER NOT NULL," +
                "`action` TEXT NOT NULL);")
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("DROP VIEW IF EXISTS clog_view;")
        db.execSQL("CREATE TEMP VIEW IF NOT EXISTS clog_view AS " +
                "SELECT `_id`, `package`, `type`, `action`, sum(r) AS `result`, sum(x) AS `count`, sum(v) AS `restricted` FROM " +
                "(SELECT component._id AS `_id`, component.package AS `package`, component.type AS `type`, component.action AS `action`, clog.action AS `r_action`, 0 AS `r`, 0 AS `x`, component._id AS `v` " +
                "FROM component LEFT JOIN clog USING(package, type, action) " +
                "UNION ALL SELECT clog._id AS `_id`, clog.package AS `package`, clog.type AS `type`, clog.action AS `action`, component.action AS `r_action`, clog.result AS `r`, 1 AS `x`, 0 AS `v` " +
                "FROM clog LEFT JOIN component USING(package, type, action)) " +
                "GROUP BY package, action;")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //nothing to do
    }
}