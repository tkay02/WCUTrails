package edu.wcu.cs.thomas_kay.gpskotlin

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
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


class TrailObserver : AppCompatActivity() {

    private lateinit var trailName:String
    private lateinit var map:GoogleMap
    private lateinit var trail:Trail
    private var currentMarker:Marker? = null
    private lateinit var trailArray:ArrayList<String>
    private lateinit var prevButton:Button
    private lateinit var nextButton:Button
    private lateinit var app: TrailApplication
    private lateinit var countDownPrev:CountDownTimer
    private lateinit var countDownNext:CountDownTimer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trail_observer)
        val application = application as TrailApplication
        trailArray = application.getTrailNames()
        val bundle = intent.extras
        if(bundle != null) {
            this.trailName = bundle.getString(TRAIL_NAME)!!
            val tv:TextView = this.findViewById(R.id.trail_name)
            tv.text = this.trailName
            app = application as TrailApplication
            trail = app.getTrailList()[trailArray.indexOf(trailName)]
            updateFragment()
            setButtons()
        }
    }

    private fun updateFragment() {
        val fragment: SupportMapFragment = this.supportFragmentManager.findFragmentById(R.id.map2)
                as SupportMapFragment
        fragment.getMapAsync {
            this.map = it
            this.app.recordPoints(trail, map)
        }
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtons() {
        setTimers()
        this.prevButton = findViewById(R.id.prev_button)
        prevButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> countDownPrev.start()
                MotionEvent.ACTION_UP -> countDownPrev.cancel()
                else -> {}
            }
            true
        }
        this.nextButton = findViewById(R.id.next_button)
        nextButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> countDownNext.start()
                MotionEvent.ACTION_UP -> countDownNext.cancel()
                else -> {}
            }
            true
        }
    }

    private fun setTimers() {
        this.countDownPrev = object:CountDownTimer(Long.MAX_VALUE, 500) {
            override fun onTick(millisUntilFinished: Long) {
                getPoint(true)
            }

            override fun onFinish() {}
        }
        this.countDownNext = object:CountDownTimer(Long.MAX_VALUE, 500) {
            override fun onTick(millisUntilFinished: Long) {
                getPoint(false)
            }

            override fun onFinish() {}
        }
    }


}