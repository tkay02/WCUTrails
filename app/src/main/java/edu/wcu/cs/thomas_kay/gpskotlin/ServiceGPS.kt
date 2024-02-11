package edu.wcu.cs.thomas_kay.gpskotlin

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.lang.UnsupportedOperationException

class ServiceGPS : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback : LocationCallback
    private lateinit var locationRequest: LocationRequest

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
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
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, null)
        } catch(error:SecurityException) {
            Log.v("Error", error.message ?: "Something else went wrong")
        }
    }

    /**
     * Testing
     */
    private fun updateLocation(location:Location) {
        val lat:Double = location.latitude
        val long:Double = location.longitude
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
        val coordinates:String = getString(R.string.location) + "\n" + latString + "\n" + longString
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

    //public constants
    companion object {
        const val GPS:String = "GPS"
    }
}