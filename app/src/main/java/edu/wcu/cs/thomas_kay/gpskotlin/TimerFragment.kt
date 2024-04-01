package edu.wcu.cs.thomas_kay.gpskotlin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/** The max amount of seconds calculated */
const val MAX_SECONDS = 10800
/** The value of one second in milliseconds */
const val SECOND:Long = 1000
/** The amount of seconds in a minute and the amount of minutes in a hour */
const val SIXTY = 60


/**
 * Fragment that calculates down the seconds when started.
 */
class TimerFragment : Fragment() {
    /** TextView that contains the time counted */
    private lateinit var textBox:TextView
    /** The counter for the amount of seconds */
    private var seconds:Long = 0 //For counter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    fun setTimer() {
        val view = requireView()
        textBox = view.findViewById(R.id.timer_fragment)
        startTimer()
    }

    fun startTimer() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object:Runnable {
            override fun run() {
                val sec = seconds % SIXTY
                val minutes = (seconds / SIXTY) % SIXTY
                val hours = (seconds / SIXTY) / SIXTY
                val time = "${getTimeString(hours, true)}:${getTimeString(minutes, false)}:"+
                        getTimeString(sec, false)
                textBox.text = time
                if(seconds < MAX_SECONDS) {
                    seconds++
                }
                handler.postDelayed(this, SECOND)
            }
        }
        handler.post(runnable)
    }

    private fun getTimeString(time:Long, isHr: Boolean):String {
        if(isHr) {
            return time.toString()
        } else {
            val timeString = time.toString()
            if(timeString.length < 2) {
                return "0$timeString"
            }
            return timeString
        }
    }

    fun getSeconds():Int {
        return this.seconds.toInt()
    }
}