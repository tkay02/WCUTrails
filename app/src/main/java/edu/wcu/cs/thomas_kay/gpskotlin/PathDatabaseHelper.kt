package edu.wcu.cs.thomas_kay.gpskotlin

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * Can possibly use this as a trail tracker for a user's personal use.
 */
class PathDatabaseHelper(context: Context, databaseName: String = "pathrecorder.db") :
    SQLiteOpenHelper(context, databaseName, null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE $TRAIL_TABLE ($TRAIL_POINT INTEGER PRIMARY KEY, "+
        "$TRAIL_LAT REAL, $TRAIL_LNG REAL);"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val query = "DROP TABLE IF EXISTS $TRAIL_TABLE;"
        db!!.execSQL(query)
        onCreate(db)
    }

    fun insert(location: Location, cnt:Int):Boolean {
        val db:SQLiteDatabase = writableDatabase
        val cv = ContentValues()
        cv.put(TRAIL_POINT, cnt)
        cv.put(TRAIL_LAT, location.latitude)
        cv.put(TRAIL_LNG, location.longitude)
        val valid:Long = db.insert(TRAIL_TABLE, null, cv)
        if(valid.toInt() == -1) {
            return false
        }
        return true
    }

    fun getCoordinates():ArrayList<LatLng> {
        val list = ArrayList<LatLng>()
        val query = "SELECT * FROM $TRAIL_TABLE" //Change this to SELECT LAT, LNG
        val db:SQLiteDatabase = readableDatabase
        val cursor = db.rawQuery(query, null)
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                var i = 0
                while(i < cursor.count) {
                    val lat = cursor.getDouble(1)
                    val long = cursor.getDouble(2)
                    list.add(LatLng(lat, long))
                    cursor.moveToNext()
                    i++
                }
            }
            cursor.close()
        }
        db.close()
        return list
    }

    //Public Constants
    companion object {
        const val TRAIL_TABLE:String = "TRAIL_TABLE"
        const val TRAIL_POINT:String = "POINT"
        const val TRAIL_LAT:String = "LAT"
        const val TRAIL_LNG:String = "LNG"
    }
}