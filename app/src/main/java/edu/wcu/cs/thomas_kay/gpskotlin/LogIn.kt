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

const val ADMIN:String = "Admin"

class LogIn : AppCompatActivity() {

    private lateinit var userEditText:EditText
    private lateinit var passwordEditText:EditText
    private val reference = FirebaseDatabase.getInstance().getReference(FIREBASE_USER)
    private val adminReference = FirebaseDatabase.getInstance().getReference(FIREBASE_ADMIN)
    private lateinit var adminNames:ArrayList<String>
    private lateinit var adminPasswords:ArrayList<String>
    private lateinit var adminSalts:ArrayList<String>
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
                Log.v("error", error.message)
            }
        })
        adminReference.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                adminNames = ArrayList()
                adminPasswords = ArrayList()
                adminSalts = ArrayList()
                for(i in snapshot.children) {
                    adminSalts.add(i.key!!)
                    Log.v("debugging", "Admin Salt: ${i.key}")
                    val admin = i.getValue(User::class.java)!!
                    adminNames.add(admin.username)
                    adminPasswords.add(admin.password)
                    Log.v("debugging", "Admin Username: ${admin.username}")
                    Log.v("debugging", "Admin Password: ${admin.password}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.v("error", error.message)
            }
        })
    }

    private fun authenticateUser() {
        val application = application as TrailApplication
        if(isAnAdmin()) {
            val intent = Intent(this, LogIn::class.java)
            intent.putExtra(ADMIN, true)
            application.currentUser = userEditText.text.toString()
            setResult(RESULT_OK, intent)
            finish()
        } else {
            val index = authenticateUsername()
            if (index == -1) {
                Toast.makeText(this, "Username or password is incorrect", Toast.LENGTH_LONG).show()
            } else {
                if (!authenticatePassword(index)) {
                    Toast.makeText(this, "Username or password is incorrect", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(
                        this, "User has been successfully authenticated",
                        Toast.LENGTH_LONG
                    ).show()
                    //Insert code for saving settings
                    application.currentUser = userEditText.text.toString()
                    val intent = Intent(this, LogIn::class.java)
                    setResult(RESULT_OK, intent)
                    finish()
                }
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

    private fun isAnAdmin():Boolean {
        val username = userEditText.text.toString().trim()
        if(!adminNames.contains(username)) {
            return false
        }
        val adminIndex = adminNames.indexOf(username)
        val password = passwordEditText.text.toString().trim()
        val securePassword = adminPasswords[adminIndex]
        val salt = adminSalts[adminIndex]
        val adminPassword = securePassword(salt + password)
        if(securePassword != adminPassword) {
            return false
        }
        return true
    }
}