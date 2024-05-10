package edu.wcu.cs.thomas_kay.gpskotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * Menu screen.
 */

/** Code to store data for QR code information within an intent */
const val QRCODE:String = "QRCODE"

class EntryScreen : AppCompatActivity() {

    /** Launcher used to launch RecordTrail activity */
    private lateinit var recordLauncher: ActivityResultLauncher<Intent>
    /** Launcher used to launch ObserveTrail activity */
    private lateinit var observeLauncher: ActivityResultLauncher<Intent>
    /** Launcher used to start tracking a user's progress */
    private lateinit var startTrailLauncher: ActivityResultLauncher<Intent>
    /** Launcher used to test score activity */
    private lateinit var scoreLauncher: ActivityResultLauncher<Intent>

    /**
     * Sets up name of the current user, buttons, and launchers.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_screen)
        val application = application as TrailApplication
        val title:TextView = findViewById(R.id.title)
        // Changes textview to display the user's name
        title.text = "Welcome, \n${application.currentUser}"
        val button1:Button = findViewById(R.id.locate_button)
        button1.setOnClickListener {goToLocationActivity()}
        val button2:Button = findViewById(R.id.demo_button)
        button2.setOnClickListener {goToDemoActivity()}
        val button3:Button = findViewById(R.id.qr_code_button)
        setUpLaunchers()
        button3.setOnClickListener {goToQRActivity()}
        val button4:Button = findViewById(R.id.record_trail)
        button4.setOnClickListener {goToRecordActivity()}
        val button5:Button = findViewById(R.id.go_to_path_button)
        button5.setOnClickListener {goToTrail()}
    }

    /** Starts basic loaction activity */
    private fun goToLocationActivity() {
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    /** Starts trail selector activity to view trails */
    private fun goToDemoActivity() {
        intent = Intent(this, TrailSelector::class.java)
        startActivity(intent)
    }

    /** Starts activity to QR generator activity */
    private fun goToQRActivity() {
        val intent = Intent(this, QRGenerator::class.java)
        startActivity(intent)
    }

    /** Launches Record trail activity */
    private fun goToRecordActivity() {
        val intent = Intent(this, RecordTrail::class.java)
        this.recordLauncher.launch(intent)
    }

    /** Launches activity to start a single player game */
    private fun goToTrail() {
        intent = Intent(this, TrailSelector::class.java)
        intent.putExtra(RESULT_ACTIVITY, true)
        this.startTrailLauncher.launch(intent)
    }

    /**
     * Sets up launchers to be used by the activity.
     */
    private fun setUpLaunchers() {
        this.recordLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // If the result is okay, launches user to activity to record a new trail
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
            // If the result is okay, launches user to Trail Observer activity
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
        this.startTrailLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // If the result is ok, starts a game of single player
            if(it.resultCode == RESULT_OK) {
                val intent = it.data
                if(intent != null) {
                    val trailName = intent.getStringExtra(TRAIL_NAME)
                    val newIntent = Intent(this, MainActivity::class.java)
                    newIntent.putExtra(TRAIL_NAME, trailName)
                    // Calls the score launcher
                    this.scoreLauncher.launch(newIntent)
                }
            }
        }
        this.scoreLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // If the result is ok, goes to the score activity
            if(it.resultCode == RESULT_OK) {
                val intent = it.data
                if(intent != null) {
                    val score = intent.getIntArrayExtra(SCORE)
                    val newIntent = Intent(this, Score::class.java)
                    newIntent.putExtra(SCORE, score)
                    startActivity(newIntent)
                }
            }
        }
    }


    /**
     * Private constants
     */
    companion object {
        /** Example constant for a database name */
        const val DATABASE_NAME = "DatabaseName"
    }
}