package edu.wcu.cs.thomas_kay.gpskotlin

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import edu.wcu.cs.thomas_kay.gpskotlin.EntryScreen.Companion.DATABASE_NAME
import java.lang.UnsupportedOperationException


const val DATABASE_EXTENSION = ".db"
class ServiceGPS : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback : LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var databaseHelper: PathDatabaseHelper? = null  //Change this to record to remote database
    private var cnt:Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val bundle: Bundle? = intent!!.extras
        if(bundle != null) {
            val databaseName = bundle.getString(DATABASE_NAME) + DATABASE_EXTENSION
            databaseHelper = PathDatabaseHelper(this, databaseName)
        }
        createRequest()
        createCallback()
        setGPS()
        return START_NOT_STICKY
    }

    private fun createRequest() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TIME_INTERVAL).apply {
            setMinUpdateDistanceMeters(MIN_METERS)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
    }

    private fun createCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val lastLocation: Location? = locationResult.lastLocation
                if(lastLocation != null) {
                    //Insert location into database if helper is not null
                    databaseHelper?.insert(lastLocation, cnt)
                    cnt++
                    updateLocation(lastLocation)
                }
            }
        }
    }

    private fun setGPS() {
        try {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    updateLocation(it)
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                null)
        } catch(error:SecurityException) {
            Log.v("Error", error.message ?: "Something else went wrong")
        }
    }

    private fun updateLocation(location:Location) {
        val lat:Double = location.latitude
        val long:Double = location.longitude
        val accuracy:Float = location.accuracy
        val latString = if(lat >= 0) {
            "Latitude: $lat N"
        } else {
            "Latitude: ${lat * -1} S"
        }
        val longString = if(long >= 0) {
            "Longitude: $long E"
        } else {
            "Longitude: ${long * -1} W"
        }
        //Used to inform that recorded location is 68% accurate within listed meters
        //i.e. Accuracy: 5.5 -> 68% accurate within 5.5 meters
        val accurString = "Accuracy: $accuracy"
        val coordinates:String = getString(R.string.location) + "\n" + latString + "\n" + longString + "\n" +
                accurString
        doBroadcast(lat, long, coordinates)
    }

    private fun doBroadcast(lat:Double, long:Double, coordinates:String) {
        val intent = Intent()
        intent.action = GPS
        intent.putExtra("LAT", lat)
        intent.putExtra("LONG", long)
        intent.putExtra("COORDINATES", coordinates)
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not implemented")
    }

    //public constants - another way for using constants
    companion object {
        const val GPS:String = "GPS"
    }
}