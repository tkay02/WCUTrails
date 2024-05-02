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

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * The service that tracks the user's current location and get potentially record a trail to a
 * database.
 */

/** Extension for local database files */
const val DATABASE_EXTENSION = ".db"
/** Tag for success message for QR data stored in an intent */
const val QR_SUCCESS:String = "QRSuccess"
/** Tag for the result of a QR code stored in an intent */
const val QR_RESULT:String = "QRResult"

class ServiceGPS : Service() {

    /** Client used to get the user's location */
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    /** Callback used when a location is found by the client */
    private lateinit var locationCallback : LocationCallback
    /** Request object to set parameters to client */
    private lateinit var locationRequest: LocationRequest
    /** Local database class to store trail points */
    private var databaseHelper: PathDatabaseHelper? = null
    /** The name of the trail that is being recorded */
    private var trailName:String? = null
    /** The current id value of a trail point */
    private var cnt:Int = 0
    /** The last known user location */
    private lateinit var lastLatLng: LatLng
    /** Broadcast receiver used to receive messages regarding QR code data */
    private lateinit var qrBroadcastReceiver: BroadcastReceiver

    /**
     * Starts service by setting request, callback, and client fields.
     *
     * If a trail name was provided by the caller, makes the service to record trail data.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val bundle: Bundle? = intent!!.extras
        // Records trail data is trail name was provided by an intent
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

    /**
     * Creates the request object used to pass parameters to the client.
     */
    private fun createRequest() {
        // Sets request to have high priority, have a minimal time interval of 5 seconds, have a
        // minimal distance of 8 meters, sets the data being collected to the user's current enabled
        // permission, and waits for an accurate location
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TIME_INTERVAL).apply {
            setMinUpdateDistanceMeters(MIN_METERS)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
    }

    /**
     * Creates callback to be used when a successful location is found.
     */
    private fun createCallback() {
        // Implementing an anonymous class
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val lastLocation: Location? = locationResult.lastLocation
                if(lastLocation != null) {
                    // Insert location into database if helper is not null
                    databaseHelper?.insert(lastLocation, cnt)
                    cnt++
                    // Obtain the last unknown user location
                    lastLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                    // Update the user's location for the activity
                    updateLocation(lastLocation)
                }
            }
        }
    }

    /**
     * Sets up the client with the request and callback objects
     */
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

    /**
     * Extracts the latitude and longitude values from a location and sends data to be broadcasted.
     */
    private fun updateLocation(location:Location) {
        val lat:Double = location.latitude
        val long:Double = location.longitude
        val accuracy:Float = location.accuracy
        // Sets the lat value to be S if it was negative; N otherwise
        val latString = if(lat >= 0) {
            "Latitude: $lat N"
        } else {
            "Latitude: ${lat * -1} S"
        }
        // Sets the lat value to be W if it was negative; E otherwise
        val longString = if(long >= 0) {
            "Longitude: $long E"
        } else {
            "Longitude: ${long * -1} W"
        }
        // Used to inform that recorded location is 68% accurate within listed meters
        // i.e. Accuracy: 5.5 -> 68% accurate within 5.5 meters
        val accurString = "Accuracy: $accuracy"
        val coordinates:String = getString(R.string.location) + "\n" + latString + "\n" + longString + "\n" +
                accurString
        doBroadcast(lat, long, coordinates)
    }

    /**
     * Sends broadcast for data collected of user's location.
     *
     * @param lat Latitude value of user's location
     * @param long Longitude value of user's location
     * @param coordinates Message that contains the user's current location and accuracy
     */
    private fun doBroadcast(lat:Double, long:Double, coordinates:String) {
        val intent = Intent()
        intent.action = GPS
        intent.putExtra("LAT", lat)
        intent.putExtra("LONG", long)
        intent.putExtra("COORDINATES", coordinates)
        sendBroadcast(intent)
    }

    /**
     * Sets up broadcast receiver to receive messages from activities that carry data over from QR
     * codes.
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun setQRReceiver() {
        this.qrBroadcastReceiver = object:BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent != null) {
                    if(intent.action == QR) {
                        val bundle = intent.extras
                        if(bundle != null) {
                            // Checks if values of lat and long of QR code and if data is near to
                            // the user's current location, sends a success message via a broadcast
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
        // Adds intent filter to obtain messages from QR activity
        val intentFilter = IntentFilter()
        intentFilter.addAction(QR)
        registerReceiver(this.qrBroadcastReceiver, intentFilter)
    }

    /**
     * Records data collected from local database to Firebase.
     */
    private fun writeToFirebase() {
        val trailDatabaseHelper = TrailDatabaseHelper()
        val latlngList = databaseHelper?.getCoordinates()
        trailDatabaseHelper.addName(trailName!!)
        trailDatabaseHelper.addPoints(trailName!!, latlngList!!)
    }

    /**
     * Sets up broadcast receiver when service is created.
     */
    override fun onCreate() {
        super.onCreate()
        setQRReceiver()
    }

    /**
     * Unregisters broadcast receiver and removes location updates from client. If the trail name
     * was provided, records collected trail points to Firebase.
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(this.qrBroadcastReceiver)
        if(trailName != null) {
            writeToFirebase()
            Log.v("testing", "Wrote successfully to Firebase")
        }
        this.fusedLocationProviderClient.removeLocationUpdates(this.locationCallback)
    }

    /** Not implemented */
    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not implemented")
    }

    /**
     * Private constant to be used in the service
     */
    companion object {
        /** Tag for GPS data in an intent */
        const val GPS:String = "GPS"
    }
}