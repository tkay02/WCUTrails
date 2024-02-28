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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.wcu.cs.thomas_kay.gpskotlin.EntryScreen.Companion.DATABASE_NAME

//private constants
const val TIME_INTERVAL:Long = 5000
const val MIN_METERS:Float = 8.0f
const val ZOOM_IN = 18.0f

class MainActivity : AppCompatActivity() {

    private lateinit var locationTextView:TextView
    private lateinit var map:GoogleMap
    private lateinit var locationPermissions:ActivityResultLauncher<Array<String>>
    private var prevMarker: Marker? = null
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationTextView = this.findViewById(R.id.location_text)
        var databaseName:String? = null
        val bundle:Bundle? = intent.extras
        if(bundle != null) {
            databaseName = bundle.getString(DATABASE_NAME)
        }
        this.setLaunchers()
        updateFragment()
        setReceiver()
        setUpService(databaseName)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(ServiceGPS.GPS)
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
    }

    private fun setReceiver() {
        this.broadcastReceiver = object:BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent != null) {
                    if(intent.action.equals(ServiceGPS.GPS)) {
                        val bundle:Bundle? = intent.extras
                        if(bundle != null) {
                            val lat = bundle.getDouble("LAT")
                            val long = bundle.getDouble("LONG")
                            val coordinates = bundle.getString("COORDINATES",
                                                         "Location cannot be found")
                            updateMap(lat, long, coordinates)
                        }
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

    private fun updateFragment() {
        val fragment: SupportMapFragment = this.supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment //Casting in Kotlin
        fragment.getMapAsync {this.map = it}
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
        this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, ZOOM_IN))

    }
}