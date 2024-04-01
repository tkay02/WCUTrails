package edu.wcu.cs.thomas_kay.gpskotlin

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.google.android.gms.maps.model.LatLng
import edu.wcu.cs.thomas_kay.gpskotlin.EntryScreen.Companion.DATABASE_NAME
import java.lang.UnsupportedOperationException


const val DATABASE_EXTENSION = ".db"
const val QR_SUCCESS:String = "QRSuccess"
const val QR_RESULT:String = "QRResult"

class ServiceGPS : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback : LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var databaseHelper: PathDatabaseHelper? = null
    private var trailName:String? = null
    private var cnt:Int = 0
    private lateinit var lastLatLng: LatLng
    private lateinit var qrBroadcastReceiver: BroadcastReceiver

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val bundle: Bundle? = intent!!.extras
        if(bundle != null) {
            trailName = bundle.getString(DATABASE_NAME)
            val trailFile = trailName + DATABASE_EXTENSION
            databaseHelper = PathDatabaseHelper(this, trailFile)
            Log.v("testing", "Database is recording")
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
                    lastLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
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
                    lastLatLng = LatLng(it.latitude, it.longitude)
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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun setQRReceiver() {
        this.qrBroadcastReceiver = object:BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent != null) {
                    if(intent.action == QR) {
                        val bundle = intent.extras
                        if(bundle != null) {
                            val qrLatLng = LatLng(bundle.getDouble(QRLAT), bundle.getDouble(QRLNG))
                            val isInRadius = isNearPoint(lastLatLng, qrLatLng, RADIUS)
                            val returningIntent = Intent()
                            returningIntent.action = QR_SUCCESS
                            returningIntent.putExtra(QR_RESULT, isInRadius)
                            sendBroadcast(returningIntent)
                        }
                    }
                }
            }

        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(QR)
        registerReceiver(this.qrBroadcastReceiver, intentFilter)
    }

    //I only use this method to write my data onto Firebase automatically
    //ONLY USE THIS METHOD TO AUTOMATE WRITING PROCESS TO FIREBASE DATABASE!!!
    private fun writeToFirebase() {
        val trailDatabaseHelper = TrailDatabaseHelper()
        val latlngList = databaseHelper?.getCoordinates()
        trailDatabaseHelper.addName(trailName!!)
        trailDatabaseHelper.addPoints(trailName!!, latlngList!!)
    }

    override fun onCreate() {
        super.onCreate()
        setQRReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(this.qrBroadcastReceiver)
        if(trailName != null) {
            writeToFirebase()
            Log.v("testing", "Wrote successfully to Firebase")
        }
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not implemented")
    }

    //public constants - another way for using constants
    companion object {
        const val GPS:String = "GPS"
    }
}