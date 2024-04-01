package edu.wcu.cs.thomas_kay.gpskotlin

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/** Identifies the section of the database that contains information for the trails */
const val FIREDATABASE_NAME:String = "Trails"

/**
 * Helper class that is used to write to the Firebase database for information about trails.
 */
class TrailDatabaseHelper() {

    /** Reference that connects to the Firebase database to write information for trails */
    val databaseReference:DatabaseReference = FirebaseDatabase.getInstance().getReference(
        FIREDATABASE_NAME)
    /** Counter used to give ids to the trail nodes */
    private var trailPointCounter:Int = 1

    /**
     * Initializes the counter back to one.
     */
    fun setCountToOne() {
        this.trailPointCounter = 1
    }

    /**
     * Writes a trail point to the database.
     *
     * @param trailName The name of the trail that the point belongs to.
     * @param lat The latitude coordinate.
     * @param lng The longitude coordinate.
     */
    fun addPoint(trailName:String, lat:Double, lng:Double) {
        val trailPoint = TrailPathPoint(this.trailPointCounter, lat, lng)
        val trailPointKey = databaseReference.child(trailName).push().key
        this.databaseReference.child(trailName).child(trailPointKey!!).setValue(trailPoint)
        this.trailPointCounter++
    }

    fun addName(trailName:String) {
        val trailNameData = Trail.TrailName(trailName)
        this.databaseReference.child(trailName).setValue(trailNameData)
    }

    /**
     * Writes a collection of trail points to the database. Used to automate the writing of points
     * collected from the app.
     *
     * @param trailName The name of the trail that the collection of points belongs to.
     * @param pointCollection The list of coordinate points.
     */
    fun addPoints(trailName:String, pointCollection:List<LatLng>) {
        for(i in pointCollection) {
            this.addPoint(trailName, i.latitude, i.longitude)
        }
        this.setCountToOne()
    }


}

/**
 * Data object used to collect trail point read from the database. Has an empty constructor so that
 * it can be read from the database with no issues.
 *
 * @param idNum The id of the trail point in regard to the order of the trail (starting from 1 to
 * the size of the trail).
 * @param lat The latitude coordinate.
 * @param lng The longitude coordinate.
 */
data class TrailPathPoint(val idNum:Int = 0, val lat:Double? = null, val lng:Double? = null)