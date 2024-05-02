package edu.wcu.cs.thomas_kay.gpskotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * The first screen that the user encounters when starting the application.
 */

class StartingScreen : AppCompatActivity() {

    /** Launcher for sign up activity */
    private lateinit var signUpLauncher: ActivityResultLauncher<Intent>
    /** Launcher for log in activity */
    private lateinit var logInLauncher: ActivityResultLauncher<Intent>

    //Use splashscreen for this activity
    /**
     * Displays splash screen when user boots up the application for the first time. Also loads in
     * trails to be used by the activity, buttons, and launchers.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starting_screen)
        val application = this.application as TrailApplication
        splashScreen.setKeepOnScreenCondition{application.isStarting}
        // Handler for splashscreen
        val handler = Handler(Looper.getMainLooper())
        var delay:Long = 0
        if(application.isStarting) delay = DELAY
        // Splashscreen runs for 5 seconds
        handler.postDelayed({application.isStarting = false}, delay)
        application.init()
        val signUp:Button = this.findViewById(R.id.sign_up)
        val logIn:Button = this.findViewById(R.id.log_in)
        setLaunchers()
        signUp.setOnClickListener { signUp() }
        logIn.setOnClickListener { logIn() }
    }

    /** Calls sign up launcher */
    private fun signUp() {
        val intent = Intent(this, SignUp::class.java)
        signUpLauncher.launch(intent)
    }

    /** Calls log in launcher */
    private fun logIn() {
        val intent = Intent(this, LogIn::class.java)
        logInLauncher.launch(intent)
    }

    /**
     * Sets up launchers to be start activities.
     *
     * If the result from the sign up activity was successful, launches log in activity. If the
     * result from the the log in activity was successful, starts entry screen activity.
     */
    private fun setLaunchers() {
        logInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // If result code was successfully, starts menu activity (EntryScreen)
            if(it.resultCode == RESULT_OK) {
                val intent = Intent(this, EntryScreen::class.java)
                val prevIntent = it.data
                // If user was an admin, passes admin data to EntryScreen activity
                if(prevIntent != null) {
                    val prevExtras = prevIntent.extras
                    if (prevExtras != null) {
                        val isAdmin = prevExtras.getBoolean(ADMIN)
                        Log.v("debugging", "You are an admin")
                        intent.putExtra(ADMIN, isAdmin)
                    }
                }
                startActivity(intent)
            }
        }
        signUpLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // If result code was successfully, launches log in activity
            if(it.resultCode == RESULT_OK) {
                val intent = Intent(this, LogIn::class.java)
                logInLauncher.launch(intent)
            }
        }
    }
}