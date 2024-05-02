package edu.wcu.cs.thomas_kay.gpskotlin

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * Class used to record trail data into a local database.
 */

/**
 * Constructor.
 *
 * @param context The context of the activity/service that is being used in.
 * @param databaseName The name of the database file that is being recorded to; has the default
 * value of "pathrecorder.db".
 */
class PathDatabaseHelper(context: Context, databaseName: String = "pathrecorder.db") :
    SQLiteOpenHelper(context, databaseName, null, 1) {

    /**
     * Creates the table to be used for recording trail data.
     *
     * @param db The database to execute queries.
     */
    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE $TRAIL_TABLE ($TRAIL_POINT INTEGER PRIMARY KEY, "+
        "$TRAIL_LAT REAL, $TRAIL_LNG REAL);"
        db!!.execSQL(query)
    }

    /**
     * Drops the table when an update occurs.
     *
     * @param db The database to execute queries.
     * @param oldVersion The old version number of the database.
     * @param newVersion The new version number of the database.
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val query = "DROP TABLE IF EXISTS $TRAIL_TABLE;"
        db!!.execSQL(query)
        onCreate(db)
    }

    /**
     * Inserts new data into the database.
     *
     * @param location The location that is being added to the database.
     * @param cnt The primary key/id of the location.
     */
    fun insert(location: Location, cnt:Int):Boolean {
        val db:SQLiteDatabase = writableDatabase
        val cv = ContentValues()
        cv.put(TRAIL_POINT, cnt)
        // Puts location's latitude and longitude values into the contevt values
        cv.put(TRAIL_LAT, location.latitude)
        cv.put(TRAIL_LNG, location.longitude)
        val valid:Long = db.insert(TRAIL_TABLE, null, cv)
        if(valid.toInt() == -1) {
            return false
        }
        return true
    }

    /** Returns a list of coordinates recorded from the database */
    fun getCoordinates():ArrayList<LatLng> {
        val list = ArrayList<LatLng>()
        val query = "SELECT * FROM $TRAIL_TABLE"
        val db:SQLiteDatabase = readableDatabase
        val cursor = db.rawQuery(query, null)
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                var i = 0
                // Iterates through query data to add points to the list
                while(i < cursor.count) {
                    val lat = cursor.getDouble(1)
                    val long = cursor.getDouble(2)
                    list.add(LatLng(lat, long))
                    cursor.moveToNext()
                    i++
                }
            }
            // Closes cursor
            cursor.close()
        }
        // Closes database
        db.close()
        return list
    }

    /**
     * Private constants to be used for querying
     */
    companion object {
        /** The name of the table */
        const val TRAIL_TABLE:String = "TRAIL_TABLE"
        /** Attribute name of the primary key */
        const val TRAIL_POINT:String = "POINT"
        /** Attribute name of the latitude value */
        const val TRAIL_LAT:String = "LAT"
        /** Attribute name of the longitude value */
        const val TRAIL_LNG:String = "LNG"
    }
}