package edu.wcu.cs.thomas_kay.gpskotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Intent

const val NAME:String = "TrailName"
class RecordTrail : AppCompatActivity() {

    private lateinit var editText:EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_trail)
        editText = this.findViewById(R.id.input)
        val button:Button = this.findViewById(R.id.go)
        button.setOnClickListener{go()}
    }

    private fun checkInput(input:String):Boolean {
        val isValid:Boolean
        val reference = resources.getStringArray(R.array.name_of_trails)
        when {
            input.trim() == "" -> {
                Toast.makeText(this, "Cannot not have blank name for trail",
                    Toast.LENGTH_LONG).show()
                isValid = false
            }
            reference.contains(input) -> {
                Toast.makeText(this, "Trail name is already used; please enter a different name",
                    Toast.LENGTH_LONG).show()
                isValid = false
            }
            else -> isValid = true
        }
        return isValid
    }

    private fun go() {
        val trailName = editText.text.toString()
        if(checkInput(trailName)) {
            val intent = Intent(this, RecordTrail::class.java)
            intent.putExtra(NAME, trailName)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

}