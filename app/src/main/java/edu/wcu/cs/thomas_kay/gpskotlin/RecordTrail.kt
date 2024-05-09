package edu.wcu.cs.thomas_kay.gpskotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Intent

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * The activity that requests an user to input a name for the trail that they want to record to the
 * database.
 */

/** Name for trail name data for an intent */
const val NAME:String = "TrailName"

class RecordTrail : AppCompatActivity() {

    /** Edittext to store the name of the trail inputted by the user */
    private lateinit var editText:EditText

    /**
     * Sets up the activity, the edittext, and the button.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_trail)
        editText = this.findViewById(R.id.input)
        val button:Button = this.findViewById(R.id.go)
        button.setOnClickListener{go()}
    }

    /**
     * Checks if the trail name inputted by the user is valid.
     *
     * The trail name is valid if it is either not empty or has not been used yet for a name of a
     * trail.
     *
     * @param input The trail name inputted by a user via edittext.
     * @return True if the trail name is valid; false otherwise.
     */
    private fun checkInput(input:String):Boolean {
        val isValid:Boolean
        val app = application as TrailApplication
        // The list of trail names
        val reference = app.getTrailNames()
        when {
            // Invalid if the input is empty
            input.trim() == "" -> {
                Toast.makeText(this, "Cannot not have blank name for trail",
                    Toast.LENGTH_LONG).show()
                isValid = false
            }
            // Invalid if the input contains the name of a trail that has been already chosen
            reference.contains(input) -> {
                Toast.makeText(this, "Trail name is already used; please enter a different name",
                    Toast.LENGTH_LONG).show()
                isValid = false
            }
            else -> isValid = true
        }
        return isValid
    }

    /**
     * Quits out of the activity and passes the name of the trail into an intent if what was
     * inputted by the user is valid.
     *
     * Sets the result code to be ok.
     */
    private fun go() {
        val trailName = editText.text.toString()
        // If user input is valid, quit out of activity
        if(checkInput(trailName)) {
            val intent = Intent(this, RecordTrail::class.java)
            intent.putExtra(NAME, trailName)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

}