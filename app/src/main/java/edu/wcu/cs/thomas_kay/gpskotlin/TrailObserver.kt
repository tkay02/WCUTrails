package edu.wcu.cs.thomas_kay.gpskotlin

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * The activity that displays the trail that the user can observe. Additionally, the user can
 * navigate through each point of the trail.
 */

/** Determines how fast the countdown is */
const val COUNTDOWN_INTERVAL:Long = 500

class TrailObserver : AppCompatActivity() {

    /** The name of the trail */
    private lateinit var trailName:String
    /** The map that displays the trail */
    private lateinit var map:GoogleMap
    /** The trail object contains the list of trail points */
    private lateinit var trail:Trail
    /** Marker used to display the current point on the trail */
    private var currentMarker:Marker? = null
    /** List that contains the list of trail names */
    private lateinit var trailArray:ArrayList<String>
    /** Button that goes to the previous point in the trail */
    private lateinit var prevButton:Button
    /** Button that goes to the next point in the trail */
    private lateinit var nextButton:Button
    /** Reference to the application class */
    private lateinit var app: TrailApplication
    /** Countdown timer used to hold down previous points */
    private lateinit var countDownPrev:CountDownTimer
    /** Countdown timer used to hold down next points */
    private lateinit var countDownNext:CountDownTimer

    /**
     * Sets up the activity by setting up the fragments, the buttons, and the trail used.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trail_observer)
        val application = application as TrailApplication
        trailArray = application.getTrailNames()
        val bundle = intent.extras
        if(bundle != null) {
            // Uses trail name to locate specific location of the trail
            this.trailName = bundle.getString(TRAIL_NAME)!!
            val tv:TextView = this.findViewById(R.id.trail_name)
            tv.text = this.trailName
            app = application
            trail = app.getTrailList()[trailArray.indexOf(trailName)]
            updateFragment()
            setButtons()
        }
    }

    /**
     * Sets up fragment used to display trail data and records the trail onto the map.
     */
    private fun updateFragment() {
        val fragment: SupportMapFragment = this.supportFragmentManager.findFragmentById(R.id.map2)
                as SupportMapFragment
        fragment.getMapAsync {
            this.map = it
            // Draws trail on the map
            this.app.recordPoints(trail, map)
        }
    }

    /**
     * Creates the current position of the trail that the user is navigating through.
     *
     * @param lat The latitude value of the current trail point.
     * @param lng The longitude value of the current trail point.
     */
    private fun createCurrentMarker(lat:Double, lng:Double) {
        val options = MarkerOptions().position(LatLng(lat,lng)).title(getString(
            R.string.current_point))
        // Sets color for icon to be purple to uniquely identify current point
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        this.currentMarker = this.map.addMarker(options)
    }

    /**
     * Gets the next point of the trail. Determines if the user wants to go forwards or backwards to
     * the next point on the trail.
     *
     * Updates current marker to reflect new point on the trail.
     *
     * @param isPrev True if the user wants to move the current point to the previous location;
     * otherwise, the user wants to move the current point to the next location.
     */
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

    /**
     * Sets up the buttons to use to iterate/navigate throughout the trail. Uses count down timers
     * and onTouchListeners so that the user can just can press down on the button to iterate rather
     * than constantly spamming the button.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setButtons() {
        setTimers()
        this.prevButton = findViewById(R.id.prev_button)
        prevButton.setOnTouchListener { _, event ->
            when (event.action) {
                // Starts timer when user presses down
                MotionEvent.ACTION_DOWN -> countDownPrev.start()
                // Cancels timer when user stops pressing
                MotionEvent.ACTION_UP -> countDownPrev.cancel()
                else -> {}
            }
            true
        }
        this.nextButton = findViewById(R.id.next_button)
        nextButton.setOnTouchListener { _, event ->
            when (event.action) {
                // Starts timer when user presses down
                MotionEvent.ACTION_DOWN -> countDownNext.start()
                // Starts timer when user presses down
                MotionEvent.ACTION_UP -> countDownNext.cancel()
                else -> {}
            }
            true
        }
    }

    /**
     * Sets timers to use for each button.
     */
    private fun setTimers() {
        this.countDownPrev = object:CountDownTimer(Long.MAX_VALUE, COUNTDOWN_INTERVAL) {
            // When timer is ticking, iterates the trail by its previous points
            override fun onTick(millisUntilFinished: Long) {
                getPoint(true)
            }

            override fun onFinish() {}
        }
        this.countDownNext = object:CountDownTimer(Long.MAX_VALUE, COUNTDOWN_INTERVAL) {
            // When timer is ticking, iterates the trail by its next points
            override fun onTick(millisUntilFinished: Long) {
                getPoint(false)
            }

            override fun onFinish() {}
        }
    }


}