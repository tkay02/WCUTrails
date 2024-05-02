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

class StartingScreen : AppCompatActivity() {

    private lateinit var signUpLauncher: ActivityResultLauncher<Intent>
    private lateinit var logInLauncher: ActivityResultLauncher<Intent>

    //Use splashscreen for this activity
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starting_screen)
        val application = this.application as TrailApplication
        splashScreen.setKeepOnScreenCondition{application.isStarting}
        val handler = Handler(Looper.getMainLooper())
        var delay:Long = 0
        if(application.isStarting) delay = DELAY
        handler.postDelayed({application.isStarting = false}, delay)
        application.init() //Do this in splashscreen
        val signUp:Button = this.findViewById(R.id.sign_up)
        val logIn:Button = this.findViewById(R.id.log_in)
        setLaunchers()
        signUp.setOnClickListener { signUp() }
        logIn.setOnClickListener { logIn() }
    }

    private fun signUp() {
        val intent = Intent(this, SignUp::class.java)
        signUpLauncher.launch(intent)
    }

    private fun logIn() {
        val intent = Intent(this, LogIn::class.java)
        logInLauncher.launch(intent)
    }

    private fun setLaunchers() {
        logInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) {
                val intent = Intent(this, EntryScreen::class.java)
                val prevIntent = it.data
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
            if(it.resultCode == RESULT_OK) {
                val intent = Intent(this, LogIn::class.java)
                logInLauncher.launch(intent)
            }
        }
    }
}