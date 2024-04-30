package edu.wcu.cs.thomas_kay.gpskotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LogIn : AppCompatActivity() {

    private lateinit var userEditText:EditText
    private lateinit var passwordEditText:EditText
    private val reference = FirebaseDatabase.getInstance().getReference(FIREBASE_USER)
    private lateinit var userNames:ArrayList<String>
    private lateinit var passwords:ArrayList<String>
    private lateinit var salts:ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        userEditText = this.findViewById(R.id.username_edittext)
        passwordEditText = this.findViewById(R.id.password_edittext)
        getData()
        val button:Button = this.findViewById(R.id.submit)
        button.setOnClickListener { authenticateUser() }
    }

    private fun getData() {
        reference.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userNames = ArrayList()
                passwords = ArrayList()
                salts = ArrayList()
                for(i in snapshot.children) {
                    salts.add(i.key!!)
                    Log.v("debugging", "Salt: ${i.key}")
                    val user = i.getValue(User::class.java)!!
                    userNames.add(user.username)
                    passwords.add(user.password)
                    Log.v("debugging", "Username: ${user.username}")
                    Log.v("debugging", "Password: ${user.password}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.v("error", "$error")
            }

        })
    }

    private fun authenticateUser() {
        val index = authenticateUsername()
        if(index == -1) {
            Toast.makeText(this, "Username or password is incorrect", Toast.LENGTH_LONG).show()
        } else {
            if(!authenticatePassword(index)) {
                Toast.makeText(this, "Username or password is incorrect", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "User has been successfully authenticated",
                    Toast.LENGTH_LONG).show()
                //Insert code for saving settings
                val application = application as TrailApplication
                application.currentUser = userEditText.text.toString()
                val intent = Intent(this, LogIn::class.java)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun authenticateUsername():Int {
        val username = userEditText.text.toString().trim()
        return userNames.indexOf(username)
    }

    private fun authenticatePassword(userIndex: Int):Boolean {
        val password = passwordEditText.text.toString().trim()
        val securePassword = passwords[userIndex]
        Log.v("debugging", "Secure password: $securePassword")
        val salt = salts[userIndex]
        Log.v("debugging", "Salt: $salt")
        val userPassword = securePassword(salt + password)
        return userPassword == securePassword
    }
}