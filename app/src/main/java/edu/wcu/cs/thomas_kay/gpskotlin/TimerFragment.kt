package edu.wcu.cs.thomas_kay.gpskotlin

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * Fragment that calculates down the seconds when started.
 */

/** The max amount of seconds calculated */
const val MAX_SECONDS = 10800
/** The value of one second in milliseconds */
const val SECOND:Long = 1000
/** The amount of seconds in a minute and the amount of minutes in a hour */
const val SIXTY = 60

class TimerFragment : Fragment() {
    /** TextView that contains the time counted */
    private lateinit var textBox:TextView
    /** The counter for the amount of seconds */
    private var seconds:Long = 0 //For counter
    /** Timer used to count down seconds */
    private lateinit var timer:CountDownTimer

    /**
     * Creates the view for the timer.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    /**
     * Initializes text box to display seconds counted and starts timer.
     */
    fun setTimer() {
        val view = requireView()
        textBox = view.findViewById(R.id.timer_fragment)
        startTimer()
    }

    /**
     * Initializes timer and begins count. Updates the current seconds for the user to see.
     */
    private fun startTimer() {
        this.timer = object: CountDownTimer(Long.MAX_VALUE, SECOND) {
            // Updates the score and the display of the text box
            override fun onTick(millisUntilFinished: Long) {
                val sec = seconds % SIXTY
                val minutes = (seconds / SIXTY) % SIXTY
                val hours = (seconds / SIXTY) / SIXTY
                // Displays time in the text view
                val time = "${getTimeString(hours, true)}:${getTimeString(minutes, false)}:"+
                        getTimeString(sec, false)
                textBox.text = time
                // Increments seconds if they are less than the maximum amount
                if(seconds < MAX_SECONDS) {
                    seconds++
                }
                Log.v("Count", "${getSeconds()}")
            }
            override fun onFinish() {
                TODO("Not yet implemented")
            }
        }
        this.timer.start()
    }

    /**
     * Creates a string to display the current time collected while on the trail.
     *
     * @param time The time collected from the timer.
     * @param isHr True if the time doesn't need to display 2 digits; false otherwise.
     * @return A string that represents the hour or the minute/second.
     */
    private fun getTimeString(time:Long, isHr: Boolean):String {
        if(isHr) {
            return time.toString()
        } else {
            val timeString = time.toString()
            // Places a zero in front if the number of digits is less than 2
            if(timeString.length < 2) {
                return "0$timeString"
            }
            return timeString
        }
    }

    /**
     * Stops the timer.
     */
    fun stopCount() {
        this.timer.cancel()
    }

    /** Returns the number of seconds counted */
    fun getSeconds():Int {
        // For testing
        //Toast.makeText(context, "${seconds.toInt()}", Toast.LENGTH_SHORT).show()
        return this.seconds.toInt()
    }
}