package edu.wcu.cs.thomas_kay.gpskotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class StartingScreen : AppCompatActivity() {

    //Use splashscreen for this activity
    override fun onCreate(savedInstanceState: Bundle?) {
        //val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starting_screen)
        //val application = this.application as TrailApplication
        //splashScreen.setKeepOnScreenCondition{application.isStarting}
        //val handler = Handler(Looper.getMainLooper())
        //var delay:Long = 0
        //if(application.isStarting) delay = DELAY
        //handler.postDelayed({application.isStarting = false}, delay)
        //application.init() //Do this in splashscreen
        val signUp:Button = this.findViewById(R.id.sign_up)
        val logIn:Button = this.findViewById(R.id.log_in)
        signUp.setOnClickListener { signUp() }
        logIn.setOnClickListener { logIn() }
    }

    private fun signUp() {
        val intent = Intent(this, SignUp::class.java)
        //Change this to a launcher to logIn screen
        startActivity(intent)
    }

    private fun logIn() {

    }
}