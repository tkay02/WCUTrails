package edu.wcu.cs.thomas_kay.gpskotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import edu.wcu.cs.thomas_kay.gpskotlin.EntryScreen.Companion.DATABASE_NAME
import java.io.File
import java.io.OutputStream

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * The activity that displays a user's current location and the path of a trail.
 */


/** The time interval for an update to the user's location */
const val TIME_INTERVAL:Long = 5000
/** The minimal amount of meters for an update to the user's location */
const val MIN_METERS:Float = 8.0f
/** Value used to zoom into a user's current location on a map */
const val ZOOM_IN = 18.0f
/** Name for QR code data for an intent */
const val QR = "QRCode"
/** Name for QR latitude data for an intent */
const val QRLAT = "QRLat"
/** Name for QR longitude data for an intent */
const val QRLNG = "QRLng"
/** Name for the score data for an intent */
const val SCORE = "Score"
/** Width used to set the width of the trail within the map */
const val WIDTH:Float = 15f
/** Padding used to animate camera to display user's current location */
const val PADDING:Int = 115


class MainActivity : AppCompatActivity() {

    /** Textview used to inform the user of their current latitude and longitude coordinates */
    private lateinit var locationTextView:TextView
    /** Map that displays trail and current user */
    private lateinit var map:GoogleMap
    /** Timer used to count down the user's time */
    private lateinit var timer:TimerFragment
    /** Condition that checks if the timer has been started or not */
    private var timerHasStarted = false
    /** Launcher to ask user to enable coarse/fine location permission */
    private lateinit var locationPermissions:ActivityResultLauncher<Array<String>>
    /** Launcher to go to QR Code Scanner activity */
    private lateinit var qrLauncher:ActivityResultLauncher<Intent>
    /** Broadcast receiver used to receive messages from broadcasts */
    private lateinit var broadcastReceiver: BroadcastReceiver
    /** Marker used to display the current user */
    private var prevMarker: Marker? = null
    /** Queue that contains the list of trail nodes */
    private val trailQueue:ArrayDeque<LatLng> = ArrayDeque()
    /** The start/entry point of a trail */
    private var origin:LatLng? = null
    /** List of trail nodes that the user already travelled through */
    private val polylineList = ArrayList<LatLng>()
    /** Polyline to display a user's current progress throughout a trail */
    private var polyline:Polyline? = null

    /**
     * Creates activity, sends map to track user's progress and location, and starts up GPS service
     * to record current location
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationTextView = this.findViewById(R.id.location_text)
        var databaseName:String? = null
        var trailName:String? = null
        val bundle:Bundle? = intent.extras
        // If bundle is not null, gets the database name for the service and the name of the trail
        if(bundle != null) {
            databaseName = bundle.getString(DATABASE_NAME)
            trailName = bundle.getString(TRAIL_NAME)
        }
        val qrButton: Button = findViewById(R.id.qr_button)
        qrButton.setOnClickListener { goToQRActivity() }
        setLaunchers()
        updateFragment(trailName)
        setReceiver()
        setUpService(databaseName)
    }

    /**
     * Registers the filters for the broadcast receiver to receive messages from broadcasts when
     * the activity is in the foreground.
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        // Adds tags for what intents that the receiver should receive
        filter.addAction(ServiceGPS.GPS)
        filter.addAction(QR_SUCCESS)
        registerReceiver(broadcastReceiver, filter)
    }

    /**
     * Unregisters the broadcast receiver when the activity is not in the foreground.
     */
    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(broadcastReceiver)
        } catch(error:Error) {
            Log.v("error", error.message ?: "Error")
        }
    }

    /**
     * Stops GPS service and timer when activity is being destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, ServiceGPS::class.java)
        stopService(intent)
        if(timerHasStarted) {
            this.timer.stopCount()
        }
    }

    /**
     * Sets up the broadcast receiver to receive broadcasts that relate to the current GPS status
     * from the GPS service and QR location data.
     */
    private fun setReceiver() {
        this.broadcastReceiver = object:BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent != null) {
                    val bundle:Bundle? = intent.extras
                    when (intent.action) {
                        // If intent contains info about user's location, call updateMap
                        ServiceGPS.GPS -> {
                            if(bundle != null) {
                                val lat = bundle.getDouble("LAT")
                                val long = bundle.getDouble("LONG")
                                val coordinates = bundle.getString(
                                    "COORDINATES",
                                    "Location cannot be found"
                                )
                                updateMap(lat, long, coordinates)
                            }
                        }
                        // If intent contains info about QR code's location data, display success
                        // message
                        QR_SUCCESS -> {
                            if(bundle != null) {
                                val success = bundle.getBoolean(QR_RESULT)
                                val message = if(success) {
                                    "User is within $RADIUS meters from QR location"
                                } else {
                                    "User is not within $RADIUS meters from QR location (CHEATER!)"
                                }
                                Toast.makeText(this@MainActivity, message,
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                        else -> {}
                    }
                }
            }

        }
    }

    /**
     * Sets up launchers to be used in the activity.
     *
     * Sets the location permissions launcher to ask the user permission to use their location. If
     * the user gives permission, starts service. If not, finish activity. Uses the QRLauncher uses
     * the intent returned by the QR activity to determine if data scanned is valid or not.
     */
    private fun setLaunchers() {
        locationPermissions = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val intent = Intent(this,ServiceGPS::class.java)
            // If user grants permission for fine or coarse location, starts service
            // Otherwise, quits out of activity
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Toast.makeText(this, "Fine permission granted", Toast.LENGTH_LONG).show()
                    startService(intent)
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Toast.makeText(this, "Coarse permission granted", Toast.LENGTH_LONG).show()
                    startService(intent)
                }
                else -> {
                    Toast.makeText(this, "Need permissions to access location",
                        Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
        qrLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) {
                val intent = it.data
                if(intent != null) {
                    val coordinates = intent.getStringExtra(QRCODE)?.split(" ")!!
                    // If QR results are valid, send latitude and longitude data for service
                    try {
                        val lat = coordinates[1].toDouble()
                        val lng = coordinates[2].toDouble()
                        doQRBroadcast(lat, lng)
                    } catch (e:NumberFormatException) {
                        Toast.makeText(this, "Incorrect type of QR code was used",
                            Toast.LENGTH_LONG).show()
                    } catch (e:ArrayIndexOutOfBoundsException) {
                        Toast.makeText(this, "Incorrect type of QR code was used",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    /**
     * Sets up the GPS service to be used for the activity.
     *
     * If a database name was entered as a parameter, the service is used to record points onto a
     * local database. Checks if an user granted their fine or coarse permission is enabled to start
     * service. Otherwise, launches location permissions launcher to ask user for said permissions.
     *
     * @param databaseName The name of the database for the service to be used to record points.
     */
    private fun setUpService(databaseName:String?) {
        Log.v("Test", "step 4")
        // Checks permissions
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, ServiceGPS::class.java)
            // Puts database name into intent if name was provided
            if(databaseName != null) {
                intent.putExtra(DATABASE_NAME, databaseName)
            }
            this.startService(intent)
            Log.v("Test", "service started")
        } else {
            locationPermissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    /**
     * Sets up the map and timer fragments used in the application.
     *
     * If the name of the trail is provided, prints the trail of the trail name to display on the
     * map.
     *
     * @param trailName The name of the trail to display the trail path.
     */
    private fun updateFragment(trailName:String?) {
        val fragment: SupportMapFragment = this.supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment //Casting in Kotlin
        fragment.getMapAsync {
            this.map = it
            if(trailName != null) {
                val application = application as TrailApplication
                val trailArray = application.getTrailNames()
                val trail = application.getTrailList()[trailArray.indexOf(trailName)]
                trailQueue.addAll(trail.iterate())
                // Calls application's record points to populate map with trail points and to
                // receive the start of the trail
                origin = application.recordPoints(trail, this.map)
            }
        }
        this.timer = this.supportFragmentManager.findFragmentById(R.id.timer) as TimerFragment
    }

    /**
     * Updates the map and text with the user's current location. The marker is also updated when
     * the user's current location is found.
     *
     * @param lat The latitude value used to display the marker on the map.
     * @param long The longitude value used to display the marker on the map.
     * @param coordinates String that informs the user where they are located.
     */
    private fun updateMap(lat:Double, long:Double, coordinates:String) {
        //Updates the TextView with the user's message
        locationTextView.text = coordinates
        //Removes the previous marker if the marker exists
        if(this.prevMarker != null) {
            this.prevMarker!!.remove()
        }
        //LatLng object used for map to display current location
        val current = LatLng(lat, long)
        this.prevMarker = this.map.addMarker(MarkerOptions().position(current).title(
            "Current Location"))
        // If the origin is not null, the current point is not near the origin, and the trail queue
        // contains the origin, displays distance between the start of the trail and the user
        if(origin != null && !isNearPoint(current, origin!!, RADIUS) &&
            trailQueue.contains(origin)) {
                val bounds = LatLngBounds.builder()
                    .include(current)
                    .include(origin!!)
                    .build()
                this.map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, PADDING))
        } else {
            this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, ZOOM_IN))
            // If origin has been collected, start the timer fragment
            if(!timerHasStarted && origin != null) {
                this.timer.setTimer()
                timerHasStarted = true
            }
            // If trail queue is not empty and the current point is near the head of the trail
            // queue, display updated path to user and poll the trail queue
            if(!trailQueue.isEmpty() && isNearPoint(current, trailQueue.first(), RADIUS)) {
                polylineList.add(trailQueue.removeFirst())
                val completedPath = PolylineOptions()
                completedPath.addAll(polylineList)
                        .width(WIDTH)
                        .color(ContextCompat.getColor(this,R.color.followed_trail_color))
                        .geodesic(true)
                if(polyline != null) {
                    polyline!!.remove()
                }
                polyline = this.map.addPolyline(completedPath)
            }
            // If the trail queue is empty and an origin exists, exits out of the activity with the
            // score of the timer fragment being passed into an intent
            if(trailQueue.isEmpty() && origin != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(SCORE, this.timer.getSeconds())
                setResult(RESULT_OK, intent)
                finish()
            }
        }

    }

    /**
     * Starts the QR Code Scanner activity.
     */
    private fun goToQRActivity() {
        val intent = Intent(this, QRScanner::class.java)
        this.qrLauncher.launch(intent)
    }

    /**
     * Sends a broadcast to the service by sending the latitude and longitude found in a QR code.
     *
     * @param lat Latitude value of QR code.
     * @param lng Longitude value of QR code.
     */
    private fun doQRBroadcast(lat: Double, lng: Double) {
        val intent = Intent()
        intent.action = QR
        intent.putExtra(QRLAT, lat)
        intent.putExtra(QRLNG, lng)
        sendBroadcast(intent)
    }

}