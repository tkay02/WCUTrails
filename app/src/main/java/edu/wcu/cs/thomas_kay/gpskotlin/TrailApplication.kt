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


const val DELAY:Long = 5000;
class TrailApplication : Application() {

    /** Contains references to the list of trails to be read in */
    private lateinit var trailList: ArrayList<Trail>
    private lateinit var trailNames: ArrayList<String>
    var isStarting = true
    private lateinit var settings: SharedPreferences
    var currentUser:String? = null

    //Use this to store trail data types that can be accessed from
    //And load in the data from Firebase during the splashscreen

    /**
     * Used to read in and fill in data from the Firebase database. Use this function/method as a
     * background process.
     */
    fun init():Boolean {
        val reference = FirebaseDatabase.getInstance().getReference(FIREDATABASE_NAME)
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.v("num of children", "${snapshot.childrenCount}") //debugging
                trailList = ArrayList()
                trailNames = ArrayList()
                for((cnt, i) in snapshot.children.withIndex()) {
                    Log.v("num of children2", "${i.childrenCount}") //debugging
                    val trailName = i.getValue(Trail.TrailName::class.java)
                    trailNames.add(trailName?.trailName!!)
                    Log.v("Trail name testing", trailNames[cnt])
                    val trail = Trail(trailNames[cnt])
                    for(j in i.children) {
                        try {
                            val trailPathPoint = j.getValue(TrailPathPoint::class.java)
                            trail.add(trailPathPoint?.lat!!, trailPathPoint.lng!!)
                        }catch (e:DatabaseException) {
                            Log.v("Error", "Not a valid datatype")
                        }
                    }
                    trailList.add(trail)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.v("error", "Error: ${error.message}")
            }

        })
        return true
    }

    fun getTrailList():ArrayList<Trail> {
        return this.trailList
    }

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
            val origin = latlngList[0]
            val destination = latlngList[latlngList.size - 1]
            val path = PolylineOptions()
            path.addAll(latlngList)
            path.width(WIDTH)
            path.color(ContextCompat.getColor(this, R.color.gps_color))
            path.geodesic(true)
            map.addPolyline(path)
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