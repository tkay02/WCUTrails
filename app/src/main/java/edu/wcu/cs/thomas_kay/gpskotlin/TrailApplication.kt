package edu.wcu.cs.thomas_kay.gpskotlin

import android.app.Application
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Deferred
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


class TrailApplication() : Application() {

    /** Contains references to the list of trails to be read in */
    private lateinit var trailList: ArrayList<Trail>

    //Use this to store trail data types that can be accessed from
    //And load in the data from Firebase during the splashscreen

    /**
     * Used to read in and fill in data from the Firebase database. Use this function/method as a
     * background process.
     */
    fun init():Boolean {
        val trailArray = resources.getStringArray(R.array.name_of_trails)
        val reference = FirebaseDatabase.getInstance().getReference(FIREDATABASE_NAME)
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.v("num of children", "${snapshot.childrenCount}") //debugging
                trailList = ArrayList()
                for((cnt, i) in snapshot.children.withIndex()) {
                    Log.v("num of children2", "${i.childrenCount}") //debugging
                    val trail = Trail(trailArray[cnt])
                    for(j in i.children) {
                        val trailPathPoint = j.getValue(TrailPathPoint::class.java)
                        trail.add(trailPathPoint?.lat!!, trailPathPoint.lng!!)
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

    //Move function to a different class
    /**
     * Calculates the distance between two latitude/longitude coordinate points.
     *
     * @param coord1 Coordinate point one (the user's location).
     * @param coord2 Coordinate point two (static distance location).
     * @return The distance between two coordinate points in meters.
     */
    fun calculateDistance(coord1: LatLng, coord2: LatLng):Double {
        //Make 180 and 6378100 as constants
        //Convert to radians
        val lat1 = coord1.latitude * (PI/180)
        val lng1 = coord1.longitude * (PI/180)
        val lat2 = coord2.latitude * (PI/180)
        val lng2 = coord2.longitude * (PI/180)
        return acos(sin(lat1)*sin(lat2)+cos(lat1)*cos(lat2)*cos(lng2-lng1)) * 6371000
    }

    //Move function to different class
    /**
     * Checks if the first coordinate point is within a radius of the second coordinate point.
     *
     * @param coord1 Coordinate point one (the user's location).
     * @param coord2 Coordinate point two (static distance location).
     * @param radius The radius of the second coordinate point in meters.
     * @return True if the distance of the two coordinate points are within the radius; false
     * otherwise. The distance is within the radius if the distance is less than or equal to the
     * radius.
     */
    fun isNearPoint(coord1: LatLng, coord2: LatLng, radius: Double):Boolean {
        val distanceBtwPoints = calculateDistance(coord1, coord2)
        return distanceBtwPoints <= radius
    }



}