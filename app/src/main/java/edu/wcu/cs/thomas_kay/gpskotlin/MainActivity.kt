package edu.wcu.cs.thomas_kay.gpskotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
    private lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    private lateinit var locationRequest:LocationRequest
    private lateinit var locationCallback:LocationCallback
    private var prevMarker: Marker? = null
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var quit:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.v("Test", "step 1")
        locationTextView = this.findViewById(R.id.location_text)
        quit = this.findViewById(R.id.quitButton)
        quit.setOnClickListener{
            intent = Intent(this, ServiceGPS::class.java)
            stopService(intent)
            finish()
        }

        var databaseName:String? = null

        val bundle:Bundle? = intent.extras
        if(bundle != null) {
            databaseName = bundle.getString(DATABASE_NAME)
        }

        this.setLaunchers()
        //this.createRequest()

        /*
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val lastLocation:Location? = locationResult.lastLocation
                if(lastLocation != null) {
                    updateLocation(lastLocation)
                    Log.v("test","I hope it's working")
                }
            }

        }
         */

        updateFragment()
        setReceiver()
        //updateGPS()
        setUpService(databaseName)

    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(ServiceGPS.GPS)
        registerReceiver(broadcastReceiver, filter)


        /*
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
            null)
        } catch(error:SecurityException) {
            Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
        }

         */

    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(broadcastReceiver)
        } catch(error:Error) {
            Log.v("error", error.message ?: "Error")
        }
        //Delete
        //fusedLocationProviderClient.removeLocationUpdates(locationCallback)
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
        Log.v("Test", "step 3")
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
                    //updateGPS()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Toast.makeText(this, "Coarse permission granted", Toast.LENGTH_LONG).show()
                    startService(intent)
                    //updateGPS()
                }
                else -> {
                    Toast.makeText(this, "Need permissions to access location",
                        Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
        Log.v("Test", "step 2")
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

    private fun createRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,
                                                  TIME_INTERVAL).apply {
            setMinUpdateDistanceMeters(MIN_METERS)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
    }

    private fun updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener(this)
            { location:Location? ->
                if(location != null) {
                    updateLocation(location)
                    Log.v("test","hello")
                }
            }
        } else {
            locationPermissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun updateLocation(location: Location) {
        val latitude:Double = location.latitude
        val longitude:Double = location.longitude
        val coordinates:String = if(location.hasAltitude()) {
            getCoordinates(latitude, longitude, location.altitude)
        } else {
            getCoordinates(latitude, longitude)
        }
        locationTextView.text = coordinates
        if(this.prevMarker != null) {
            this.prevMarker!!.remove()
        }
        val current = LatLng(latitude, longitude)
        this.prevMarker = this.map.addMarker(MarkerOptions().position(current).title(
            "Current Location"))
        this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, ZOOM_IN))
    }

    private fun updateFragment() {
        val fragment: SupportMapFragment = this.supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment //Casting in Kotlin
        fragment.getMapAsync {map:GoogleMap -> this.map = map }
    }

    private fun getCoordinates(latitude: Double, longitude: Double): String {
        val latString:String = if (latitude >= 0) {
            "Latitude: $latitude N"
        } else {
            "Latitude: ${latitude * -1} S"
        }
        val longString:String = if (longitude >= 0) {
            "Latitude: $longitude E"
        } else {
            "Longitude: ${longitude * -1} W"
        }
        return getString(R.string.location) + "\n" + latString + "\n" + longString
    }

    private fun getCoordinates(latitude:Double, longitude:Double, altitude:Double) : String {
        return getCoordinates(latitude,longitude) + "\nAltitude: $altitude"
    }

    /**
     * Testing
     */
    private fun updateMap(lat:Double, long:Double, coordinates:String) {
        locationTextView.text = coordinates
        if(this.prevMarker != null) {
            this.prevMarker!!.remove()
        }
        val current = LatLng(lat, long)
        this.prevMarker = this.map.addMarker(MarkerOptions().position(current).title(
            "Current Location"))
        this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, ZOOM_IN))

    }

}