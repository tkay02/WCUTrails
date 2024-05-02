package edu.wcu.cs.thomas_kay.gpskotlin

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * Application class to store trail data from Firebase so that other activities can use its data.
 */

/** The amount of time it takes to run splashscreen when starting application. */
const val DELAY:Long = 5000;
class TrailApplication : Application() {

    /** Contains references to the list of trails to be read in */
    private lateinit var trailList: ArrayList<Trail>
    /** Contains references to the name of the trails found in the database */
    private lateinit var trailNames: ArrayList<String>
    /** Boolean that determines if the application is starting */
    var isStarting = true
    /** To save settings -- not implemented yet */
    private lateinit var settings: SharedPreferences
    /** The name of an user that has logged in */
    var currentUser:String? = null

    /**
     * Used to read in and fill in data from the Firebase database. Use this function/method as a
     * background process.
     */
    fun init():Boolean {
        val reference = FirebaseDatabase.getInstance().getReference(FIREDATABASE_NAME)
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.v("num of children", "${snapshot.childrenCount}") //debugging
                // Sets up lists to store trails and their trail names
                trailList = ArrayList()
                trailNames = ArrayList()
                for((cnt, i) in snapshot.children.withIndex()) {
                    Log.v("num of children2", "${i.childrenCount}") //debugging
                    // Adds trail name to list
                    val trailName = i.getValue(Trail.TrailName::class.java)
                    trailNames.add(trailName?.trailName!!)
                    Log.v("Trail name testing", trailNames[cnt])
                    val trail = Trail(trailNames[cnt])
                    for(j in i.children) {
                        // Add trail point to trail
                        try {
                            val trailPathPoint = j.getValue(TrailPathPoint::class.java)
                            trail.add(trailPathPoint?.lat!!, trailPathPoint.lng!!)
                        }catch (e:DatabaseException) {
                            Log.v("Error", "Not a valid datatype")
                        }
                    }
                    // Add trail to trail ist
                    trailList.add(trail)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.v("error", "Error: ${error.message}")
            }

        })
        return true
    }

    /** Returns the list of trails */
    fun getTrailList():ArrayList<Trail> {
        return this.trailList
    }

    /** Returns the list of trail names */
    fun getTrailNames():ArrayList<String> {
        return this.trailNames
    }

    //Maybe move this to another class similar to DistanceCalc.kt
    /**
     * Sketches a trail onto a map. A modular (reusable) function that could be applied to
     * activities that contain a map.
     */
    fun recordPoints(trail:Trail, map:GoogleMap):LatLng? {
        val latlngList = trail.iterate()
        if(latlngList.size != 0) {
            // Extracts origin and end of the trail
            val origin = latlngList[0]
            val destination = latlngList[latlngList.size - 1]
            val path = PolylineOptions()
            path.addAll(latlngList)
            path.width(WIDTH)
            path.color(ContextCompat.getColor(this, R.color.gps_color))
            path.geodesic(true)
            // Display trail path onto the map
            map.addPolyline(path)
            // Add markers for the head and end of a trail
            map.addMarker(
                MarkerOptions().position(origin).title(getString(
                R.string.start_of_trail)))
            map.addMarker(
                MarkerOptions().position(destination).title(getString(
                R.string.end_of_trail)))
            val bounds = LatLngBounds.builder()
                .include(origin)
                .include(destination)
                .build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, PADDING))
            return origin
        }
        return null
    }

}