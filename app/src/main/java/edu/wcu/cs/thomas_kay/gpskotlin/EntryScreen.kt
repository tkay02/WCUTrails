package edu.wcu.cs.thomas_kay.gpskotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

const val QRCODE:String = "QRCODE"
const val RADIUS:Double = 5.0

class EntryScreen : AppCompatActivity() {

    private lateinit var switch: Switch
    private lateinit var databaseEditText: EditText
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var qrcodeLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_screen)
        switch = findViewById(R.id.database_switch)
        databaseEditText = findViewById(R.id.database_name)
        button1 = findViewById(R.id.locate_button)
        button1.setOnClickListener {goToLocationActivity()}
        button2 = findViewById(R.id.demo_button)
        button2.setOnClickListener {goToDemoActivity()}
        button3 = findViewById(R.id.qr_code_button)
        setUpLauncher()
        button3.setOnClickListener {goToQRActivity()}
    }

    private fun goToLocationActivity() {
        if(!switch.isChecked) {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            val databaseName = databaseEditText.text.toString()
            when {          //when can be used as a switch-case and else-if block
                databaseName == "" -> {
                    Toast.makeText(this, "Cannot have an empty string for name of database",
                        Toast.LENGTH_LONG).show()
                }
                startsWithDigit(databaseName) -> {
                    Toast.makeText(this, "Cannot have database name start with a digit",
                        Toast.LENGTH_LONG).show()
                }
                !noSpaces(databaseName) -> {
                    Toast.makeText(this, "Cannot have spaces in database name",
                        Toast.LENGTH_LONG).show()
                }
                else -> {
                    intent = Intent(this, MainActivity::class.java)
                    intent.putExtra(DATABASE_NAME, databaseName)
                    startActivity(intent)
                }
            }
        }
    }

    private fun goToDemoActivity() {
        intent = Intent(this, TrailSelector::class.java)
        startActivity(intent)
    }

    private fun goToQRActivity() {
        val intent = Intent(this, QRScanner::class.java)
        this.qrcodeLauncher.launch(intent)
    }

    private fun noSpaces(name:String):Boolean {
        val arrayChar = name.toCharArray()
        for(i in arrayChar) {
            if(Character.isSpaceChar(i)) {
                return false
            }
        }
        return true
    }

    private fun startsWithDigit(name:String):Boolean {
        val arrayChar = name.toCharArray()
        return Character.isDigit(arrayChar[0])
    }

    //I only use this method to write my data onto Firebase automatically
    //ONLY USE THIS METHOD TO AUTOMATE WRITING PROCESS TO FIREBASE DATABASE!!!
    private fun writeToFirebase(fileName:String) {
        val pathDatabaseHelper = PathDatabaseHelper(this, fileName)
        val trailDatabaseHelper = TrailDatabaseHelper()
        val latlngList = pathDatabaseHelper.getCoordinates()
        trailDatabaseHelper.addPoints("Gribble Gap", latlngList)
    }

    private fun setUpLauncher() {
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
    }


    companion object {
        const val DATABASE_NAME = "DatabaseName"
    }
}