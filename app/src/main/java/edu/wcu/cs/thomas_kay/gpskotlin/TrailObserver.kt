package edu.wcu.cs.thomas_kay.gpskotlin

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class TrailObserver : AppCompatActivity(),OnTouchListener {

    private lateinit var trailName:String
    private lateinit var map:GoogleMap
    private lateinit var trail:Trail
    private var currentMarker:Marker? = null
    private lateinit var trailArray:Array<String>
    private lateinit var prevButton:Button
    private lateinit var nextButton:Button

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trail_observer)
        trailArray = resources.getStringArray(R.array.name_of_trails)
        updateFragment()
        this.prevButton = this.findViewById(R.id.prev_button)
        prevButton.setOnTouchListener(this)
        this.nextButton = this.findViewById(R.id.next_button)
        nextButton.setOnTouchListener(this)
        val bundle = intent.extras
        if(bundle != null) {
            this.trailName = bundle.getString(TRAIL_NAME)!!
            val tv:TextView = this.findViewById(R.id.trail_name)
            tv.text = this.trailName
        }
    }

    //Move to onCreate()?
    //Change this to be more efficient
    override fun onStart() {
        super.onStart()
        val reference = FirebaseDatabase.getInstance().getReference(FIREDATABASE_NAME)
        reference.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.v("num of children", "${snapshot.childrenCount}") //debugging
                val trailList = ArrayList<Trail>()
                for((cnt, i) in snapshot.children.withIndex()) {
                    Log.v("num of children2", "${i.childrenCount}") //debugging
                    val trail = Trail(trailArray[cnt])
                    for(j in i.children) {
                        val trailPathPoint = j.getValue(TrailPathPoint::class.java)
                        trail.add(trailPathPoint?.lat!!, trailPathPoint.lng!!)
                    }
                    trailList.add(trail)
                }
                trail = trailList[trailArray.indexOf(trailName)]
                Log.v("trail success", "Trail was successfully created")
                recordPoints()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.v("error", "UH OH!")
            }

        })
    }

    private fun recordPoints() {
        val latlngList = trail.iterate()
        if(latlngList.size != 0) {
            val origin: LatLng = latlngList[0]
            val destination: LatLng = latlngList[latlngList.size - 1]
            val path = PolylineOptions()
            path.addAll(latlngList)
            path.width(WIDTH)
            path.color(ContextCompat.getColor(this, R.color.gps_color))
            path.geodesic(true)
            map.addPolyline(path)
            map.addMarker(MarkerOptions().position(origin).title(getString(
                R.string.start_of_trail)))
            map.addMarker(MarkerOptions().position(destination).title(getString(
                R.string.end_of_trail)))
            val bounds = LatLngBounds.builder()
                .include(origin)
                .include(destination)
                .build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, PADDING))
        }
    }

    private fun updateFragment() {
        val fragment: SupportMapFragment = this.supportFragmentManager.findFragmentById(R.id.map2)
                as SupportMapFragment
        fragment.getMapAsync {this.map = it}
    }

    private fun createCurrentMarker(lat:Double, lng:Double) {
        val options = MarkerOptions().position(LatLng(lat,lng)).title(getString(
            R.string.current_point))
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        this.currentMarker = this.map.addMarker(options)
    }

    private fun getPoint(isPrev:Boolean) {
        if(this::trail.isInitialized) {
            if (this.currentMarker != null) {
                this.currentMarker!!.remove()
            }
            val currentPoint: Trail.TrailPoint = if (isPrev) {
                this.trail.getPrevPoint()!!
            } else {
                this.trail.getNextPoint()!!
            }
            createCurrentMarker(currentPoint.lat, currentPoint.lng)
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v) {
            this.prevButton -> {
                if(event?.action == MotionEvent.ACTION_BUTTON_PRESS) {
                    getPoint(true)
                }
            }
            this.nextButton -> {
                if(event?.action == MotionEvent.ACTION_BUTTON_PRESS) {
                    getPoint(false)
                }
            }
            else -> {}
        }
        return true
    }


}