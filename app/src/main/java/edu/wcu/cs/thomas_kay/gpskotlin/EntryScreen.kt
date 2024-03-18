package edu.wcu.cs.thomas_kay.gpskotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

const val QRCODE:String = "QRCODE"

class EntryScreen : AppCompatActivity() {

    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var qrcodeLauncher: ActivityResultLauncher<Intent>
    private lateinit var recordLauncher: ActivityResultLauncher<Intent>
    private lateinit var observeLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_screen)
        val application = this.application as TrailApplication
        application.init() //Do this in splashscreen
        button1 = findViewById(R.id.locate_button)
        button1.setOnClickListener {goToLocationActivity()}
        button2 = findViewById(R.id.demo_button)
        button2.setOnClickListener {goToDemoActivity()}
        button3 = findViewById(R.id.qr_code_button)
        setUpLaunchers()
        button3.setOnClickListener {goToQRActivity()}
        button4 = findViewById(R.id.record_trail)
        button4.setOnClickListener {goToRecordActivity()}
    }

    private fun goToLocationActivity() {
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToDemoActivity() {
        intent = Intent(this, TrailSelector::class.java)
        this.observeLauncher.launch(intent)
    }

    private fun goToQRActivity() {
        val intent = Intent(this, QRScanner::class.java)
        this.qrcodeLauncher.launch(intent)
    }

    private fun goToRecordActivity() {
        val intent = Intent(this, RecordTrail::class.java)
        this.recordLauncher.launch(intent)
    }

    private fun setUpLaunchers() {
        this.qrcodeLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                val intent = it.data
                if(intent != null) {
                    val coordinates = intent.getStringExtra(QRCODE)
                    Toast.makeText(this, "Coordinates: $coordinates", Toast.LENGTH_LONG).show()
                }

            }
        }
        this.recordLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if(it.resultCode == RESULT_OK) {
                val intent = it.data
                if(intent != null) {
                    val trailName = intent.getStringExtra(NAME)
                    val intent2 = Intent(this, MainActivity::class.java)
                    intent2.putExtra(DATABASE_NAME, trailName)
                    startActivity(intent2)
                }
            }
        }
        this.observeLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if(it.resultCode == RESULT_OK) {
                val intent = it.data
                if(intent != null) {
                    val trailName = intent.getStringExtra(TRAIL_NAME)
                    val newIntent = Intent(this, TrailObserver::class.java)
                    newIntent.putExtra(TRAIL_NAME, trailName)
                    startActivity(newIntent)
                }
            }
        }
    }


    companion object {
        const val DATABASE_NAME = "DatabaseName"
    }
}