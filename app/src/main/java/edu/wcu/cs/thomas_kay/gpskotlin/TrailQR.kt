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
import java.io.File
import java.io.FileOutputStream

const val FIREBASE_QR = "QR"

class TrailQR : AppCompatActivity() {

    private lateinit var trailName:String
    private lateinit var map: GoogleMap
    private lateinit var trail:Trail
    private lateinit var currentPoint:Trail.TrailPoint
    private var currentMarker: Marker? = null
    private lateinit var trailArray:ArrayList<String>
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private lateinit var createQRButton: Button
    private lateinit var app: TrailApplication
    private lateinit var countDownPrev: CountDownTimer
    private lateinit var countDownNext: CountDownTimer
    private var qrCount:Int = 0
    private val reference = FirebaseDatabase.getInstance().getReference(FIREBASE_QR)

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trail_qr)
        app = application as TrailApplication
        trailArray = app.getTrailNames()
        val bundle = intent.extras
        if(bundle != null) {
            this.trailName = bundle.getString(TRAIL_NAME)!!
            val tv: TextView = findViewById(R.id.trail_name)
            tv.text = this.trailName
            trail = app.getTrailList()[trailArray.indexOf(this.trailName)]
            updateFragment()
            setReference()
            setButtons()
        }
    }

    private fun updateFragment() {
        val fragment: SupportMapFragment = this.supportFragmentManager.findFragmentById(R.id.map3)
                as SupportMapFragment
        fragment.getMapAsync {
            this.map = it
            this.app.recordPoints(trail, map)
        }
    }

    private fun setReference() {
        reference.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                qrCount = 0
                Log.v("debugging", "Num of children: ${snapshot.childrenCount}")
                for(i in snapshot.children) {
                    val nameOfTrail = i.getValue(Trail.TrailName::class.java)!!
                    if(nameOfTrail.trailName == trailName) {
                        Log.v("debugging", "Trail name: ${nameOfTrail.trailName}")
                        Log.v("debugging", "Num of children: ${i.childrenCount}")
                        for(j in i.children) {
                            qrCount++
                        }
                        Log.v("debugging", "QR Count: $qrCount")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.v("error", error.message)
            }

        })
    }

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

    private fun putQrElementInDatabase() {
        val qrElement = QREntry(qrCount, currentPoint.lat, currentPoint.lng)
        val qrKey = reference.child(trailName).push().key
        reference.child(trailName).child(qrKey!!).setValue(qrElement)
        if(qrCount == 0) {
            val trailNameData = Trail.TrailName(trailName)
            reference.child(trailName).setValue(trailNameData)
        }
    }

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
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100, outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(this, "Qr Entry has been successfully recorded",
                Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Need external write permissions to create QR code entry",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun createCurrentMarker(lat:Double, lng:Double) {
        val options = MarkerOptions().position(LatLng(lat,lng)).title(getString(
            R.string.current_point))
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        this.currentMarker = this.map.addMarker(options)
    }

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

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("ClickableViewAccessibility")
    private fun setButtons() {
        setTimers()
        this.prevButton = findViewById(R.id.prev_button)
        prevButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> countDownPrev.start()
                MotionEvent.ACTION_UP -> countDownPrev.cancel()
                else -> {}
            }
            true
        }
        this.nextButton = findViewById(R.id.next_button)
        nextButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> countDownNext.start()
                MotionEvent.ACTION_UP -> countDownNext.cancel()
                else -> {}
            }
            true
        }
        this.createQRButton = findViewById(R.id.trail_qr)
        createQRButton.setOnClickListener { createQRCode() }
    }

    private fun setTimers() {
        this.countDownPrev = object:CountDownTimer(Long.MAX_VALUE, 500) {
            override fun onTick(millisUntilFinished: Long) {
                getPoint(true)
            }

            override fun onFinish() {}
        }
        this.countDownNext = object:CountDownTimer(Long.MAX_VALUE, 500) {
            override fun onTick(millisUntilFinished: Long) {
                getPoint(false)
            }

            override fun onFinish() {}
        }
    }

    private fun isExternalWritable():Boolean {
        val state = Environment.getExternalStorageState()!!
        if(Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

}

data class QREntry(val id:Int = 0, val lat: Double = 0.0, val lng: Double = 0.0)