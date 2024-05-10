package edu.wcu.cs.thomas_kay.gpskotlin

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * The activity that displays the trail that the admin can observe. Additionally, the admin can
 * navigate through each point of the trail and create/save a QR code at the admin's current point.
 */

/** Reference to the QR database */
const val FIREBASE_QR = "QR"
/** The quality of the QR code entries */
const val QUALITY = 75

class TrailQR : AppCompatActivity() {

    /** The name of the trail */
    private lateinit var trailName:String
    /** The map that displays the trail */
    private lateinit var map: GoogleMap
    /** The trail object contains the list of trail points */
    private lateinit var trail:Trail
    /** Reference to the current point in the trail */
    private lateinit var currentPoint:Trail.TrailPoint
    /** Marker used to display the current point on the trail */
    private var currentMarker: Marker? = null
    /** List that contains the list of trail names */
    private lateinit var trailArray:ArrayList<String>
    /** Button that goes to the previous point in the trail */
    private lateinit var prevButton: Button
    /** Button that goes to the next point in the trail */
    private lateinit var nextButton: Button
    /** Button that creates and saves the QR code at the current point in the trail */
    private lateinit var createQRButton: Button
    /** Reference to the application class */
    private lateinit var app: TrailApplication
    /** Countdown timer used to hold down previous points */
    private lateinit var countDownPrev: CountDownTimer
    /** Countdown timer used to hold down next points */
    private lateinit var countDownNext: CountDownTimer
    /** Id used to generate QR codes */
    private var qrCount:Int = 0
    /** Reference to the QR database */
    private val reference = FirebaseDatabase.getInstance().getReference(FIREBASE_QR)

    /**
     * Sets up the activity by setting up the fragments, the buttons, the trail used, and the
     * reference to the QR database.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trail_qr)
        app = application as TrailApplication
        trailArray = app.getTrailNames()
        val bundle = intent.extras
        if(bundle != null) {
            // Uses trail name to locate specific location of the trail
            this.trailName = bundle.getString(TRAIL_NAME)!!
            val tv: TextView = findViewById(R.id.trail_name)
            tv.text = this.trailName
            trail = app.getTrailList()[trailArray.indexOf(this.trailName)]
            updateFragment()
            setReference()
            setButtons()
        }
    }

    /**
     * Sets up fragment used to display trail data and records the trail onto the map.
     */
    private fun updateFragment() {
        val fragment: SupportMapFragment = this.supportFragmentManager.findFragmentById(R.id.map3)
                as SupportMapFragment
        fragment.getMapAsync {
            this.map = it
            this.app.recordPoints(trail, map)
        }
    }

    /**
     * Sets up reference for QR database and for the specific trail that the admin chose.
     */
    private fun setReference() {
        reference.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Sets count to 0 at start of analyzing data
                qrCount = 0
                Log.v("debugging", "Num of children: ${snapshot.childrenCount}")
                for(i in snapshot.children) {
                    val nameOfTrail = i.getValue(Trail.TrailName::class.java)!!
                    // Only counts entries related to the selected trail
                    if(nameOfTrail.trailName == trailName) {
                        Log.v("debugging", "Trail name: ${nameOfTrail.trailName}")
                        Log.v("debugging", "Num of children: ${i.childrenCount}")
                        for(j in i.children) {
                            // Increases id value for each collected
                            qrCount++
                        }
                        qrCount-- //Removes the name of the trail as part of the count
                        Log.v("debugging", "QR Count: $qrCount")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.v("error", error.message)
            }

        })
    }

    /**
     * Creates a QR entry in the database and downloads the QR code into the admin's device.
     *
     * Checks if the current point has been initialized before attempting to create and download a
     * QR entry.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun createQRCode() {
        if(!this::currentPoint.isInitialized) {
            Toast.makeText(this, "Need a current point to create QR data",
                Toast.LENGTH_LONG).show()
        } else {
            putQrElementInDatabase()
            saveQrCode()
        }
    }

    /**
     * Writes qr code entry in the database.
     */
    private fun putQrElementInDatabase() {
        // If the qr count is zero, creates new trail placement in QR database
        if(qrCount == 0) {
            val trailNameData = Trail.TrailName(trailName)
            reference.child(trailName).setValue(trailNameData)
        }
        val qrElement = QREntry(qrCount, currentPoint.lat, currentPoint.lng)
        // Creates new entry for the database
        val qrKey = reference.child(trailName).push().key
        reference.child(trailName).child(qrKey!!).setValue(qrElement)
    }

    /**
     * Saves the QR code entry into the admin's device. The data in the QR code is encrypted and the
     * data is saved in the path for the admin's picture directory. Only saves if the admin has
     * granted permission to writable external data.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun saveQrCode() {
        if(isExternalWritable()) {
            val data = "$TITLE ${currentPoint.lat} ${currentPoint.lng}"
            val window = getSystemService(WINDOW_SERVICE) as WindowManager
            val dimen = window.currentWindowMetrics.bounds
            var size = dimen.width()
            if (dimen.width() >= dimen.height()) {
                size = dimen.height()
            }
            size = size * 3 / 4
            val encryptedData = encodeQR(data)
            val encoder = QRGEncoder(encryptedData, QRGContents.Type.TEXT, size)
            val bitmap = encoder.bitmap!!
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val fileName = "QRCode$trailName$qrCount.jpeg"
            val file = File(path, fileName)
            // Change this to a buffered output stream
            val outputStream = BufferedOutputStream(FileOutputStream(file))
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(this, "Qr Entry has been successfully recorded",
                Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Need external write permissions to create QR code entry",
                Toast.LENGTH_LONG).show()
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
            currentPoint = if (isPrev) {
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
     * than constantly spamming the button. Sets an additional button to create QR code entry in the
     * database and downloads the QR code in the admin's device.
     */
    @RequiresApi(Build.VERSION_CODES.R)
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
                // Cancels timer when user stops pressing
                MotionEvent.ACTION_UP -> countDownNext.cancel()
                else -> {}
            }
            true
        }
        this.createQRButton = findViewById(R.id.trail_qr)
        createQRButton.setOnClickListener { createQRCode() }
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

    /** Checks if the user has granted permission for external writable storage. */
    private fun isExternalWritable():Boolean {
        val state = Environment.getExternalStorageState()!!
        if(Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

}

/**
 * Class that represents the schema of a QR entry in the database.
 *
 * @property id The unique identifier of the QR code within the trail.
 * @property lat The latitude value of the current point.
 * @property lng The longitude value of the current point.
 */
data class QREntry(val id:Int = 0, val lat: Double = 0.0, val lng: Double = 0.0)