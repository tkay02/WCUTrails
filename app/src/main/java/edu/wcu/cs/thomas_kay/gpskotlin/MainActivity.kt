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

//private constants
const val TIME_INTERVAL:Long = 5000
const val MIN_METERS:Float = 8.0f
const val ZOOM_IN = 18.0f
const val QR = "QRCode"
const val QRLAT = "QRLat"
const val QRLNG = "QRLng"
const val SCORE = "Score"

class MainActivity : AppCompatActivity() {

    private lateinit var locationTextView:TextView
    private lateinit var map:GoogleMap
    private lateinit var timer:TimerFragment
    private var timerHasStarted = false
    private lateinit var locationPermissions:ActivityResultLauncher<Array<String>>
    private lateinit var qrLauncher:ActivityResultLauncher<Intent>
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var prevMarker: Marker? = null
    private val trailQueue:ArrayDeque<LatLng> = ArrayDeque()
    private var origin:LatLng? = null
    private val polylineList = ArrayList<LatLng>()
    private var polyline:Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationTextView = this.findViewById(R.id.location_text)
        var databaseName:String? = null
        var trailName:String? = null
        val bundle:Bundle? = intent.extras
        if(bundle != null) {
            databaseName = bundle.getString(DATABASE_NAME)
            trailName = bundle.getString(TRAIL_NAME)
        }
        val qrButton: Button = findViewById(R.id.qr_button)
        qrButton.setOnClickListener { goToQRActivity() }
        this.setLaunchers()
        updateFragment(trailName)
        setReceiver()
        setUpService(databaseName)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(ServiceGPS.GPS)
        filter.addAction(QR_SUCCESS)
        registerReceiver(broadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(broadcastReceiver)
        } catch(error:Error) {
            Log.v("error", error.message ?: "Error")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, ServiceGPS::class.java)
        stopService(intent)
        if(timerHasStarted) {
            this.timer.stopCount()
        }
    }

    private fun setReceiver() {
        this.broadcastReceiver = object:BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent != null) {
                    val bundle:Bundle? = intent.extras
                    when (intent.action) {
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

    private fun setLaunchers() {
        locationPermissions = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val intent = Intent(this,ServiceGPS::class.java)
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
                    try {
                        val lat = coordinates[0].toDouble()
                        val lng = coordinates[1].toDouble()
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

    private fun setUpService(databaseName:String?) {
        Log.v("Test", "step 4")
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, ServiceGPS::class.java)
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

    private fun updateFragment(trailName:String?) {
        val fragment: SupportMapFragment = this.supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment //Casting in Kotlin
        fragment.getMapAsync {
            this.map = it
            if(trailName != null) {
                val application = application as TrailApplication
                val trailArray = resources.getStringArray(R.array.name_of_trails);
                val trail = application.getTrailList()[trailArray.indexOf(trailName)]
                trailQueue.addAll(trail.iterate())
                origin = application.recordPoints(trail, this.map)
            }
        }
        this.timer = this.supportFragmentManager.findFragmentById(R.id.timer) as TimerFragment
    }

    /** Example of Kotlin docs */
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
        //Displays user's current location on the map
        if(origin != null && !isNearPoint(current, origin!!, RADIUS) &&
            trailQueue.contains(origin)) {
                val bounds = LatLngBounds.builder()
                    .include(current)
                    .include(origin!!)
                    .build()
                this.map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, PADDING))
        } else {
            this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, ZOOM_IN))
            if(!timerHasStarted && origin != null) {
                this.timer.setTimer()
                timerHasStarted = true
            }
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
            if(trailQueue.isEmpty() && origin != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(SCORE, this.timer.getSeconds())
                setResult(RESULT_OK, intent)
                finish()
            }
        }

    }

    private fun goToQRActivity() {
        val intent = Intent(this, QRScanner::class.java)
        this.qrLauncher.launch(intent)
    }

    private fun doQRBroadcast(lat: Double, lng: Double) {
        val intent = Intent()
        intent.action = QR
        intent.putExtra(QRLAT, lat)
        intent.putExtra(QRLNG, lng)
        sendBroadcast(intent)
    }

}