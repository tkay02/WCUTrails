package edu.wcu.cs.thomas_kay.gpskotlin

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


const val FIREDATABASE_NAME:String = "Trails"

class TrailDatabaseHelper() {

    val databaseReference:DatabaseReference = FirebaseDatabase.getInstance().getReference(
        FIREDATABASE_NAME)
    var trailPointCounter:Int = 1

    fun setCountToOne() {
        this.trailPointCounter = 1
    }

    fun addPoint(trailName:String, lat:Double, lng:Double) {
        val trailPoint = TrailPathPoint(this.trailPointCounter, lat, lng)
        val trailPointKey = databaseReference.child(trailName).push().key
        this.databaseReference.child(trailName).child(trailPointKey!!).setValue(trailPoint)
        this.trailPointCounter++
    }

    fun addPoints(trailName:String, pointCollection:List<LatLng>) {
        for(i in pointCollection) {
            this.addPoint(trailName, i.latitude, i.longitude)
        }
        this.setCountToOne()
    }

    companion object {
        //Read-only array that contains names of the trails
        val LIST_OF_TRAILS:Array<String> = arrayOf("Cullowhee-HHS Connector")
        init {
            LIST_OF_TRAILS.sort()
        }
    }


}

data class TrailPathPoint(val idNum:Int = 0, val lat:Double? = null, val lng:Double? = null)