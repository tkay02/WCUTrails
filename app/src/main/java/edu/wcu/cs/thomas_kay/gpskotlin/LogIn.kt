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

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * Authenticates an user/admin for the database.
 */

/** Path to the admin database in Firebase */
const val ADMIN:String = "Admin"

class LogIn : AppCompatActivity() {

    /** Edittext to input username */
    private lateinit var userEditText:EditText
    /** Edittext to input user's password */
    private lateinit var passwordEditText:EditText
    /** Reference to the user database */
    private val reference = FirebaseDatabase.getInstance().getReference(FIREBASE_USER)
    /** Reference to the admin database */
    private val adminReference = FirebaseDatabase.getInstance().getReference(FIREBASE_ADMIN)
    /** Reference to the list of admin names */
    private lateinit var adminNames:ArrayList<String>
    /** Reference to the list of admin passwords */
    private lateinit var adminPasswords:ArrayList<String>
    /** Reference to the list of admin salts */
    private lateinit var adminSalts:ArrayList<String>
    /** Reference to the list of usernames */
    private lateinit var userNames:ArrayList<String>
    /** Reference to the list of user passwords */
    private lateinit var passwords:ArrayList<String>
    /** Reference to the list of user salts */
    private lateinit var salts:ArrayList<String>

    /**
     * Sets up activity, sets edittext, button, and references to admin and user database.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        userEditText = this.findViewById(R.id.username_edittext)
        passwordEditText = this.findViewById(R.id.password_edittext)
        getData()
        val button:Button = this.findViewById(R.id.submit)
        button.setOnClickListener { authenticateUser() }
    }

    /**
     * Loads in data from user and admin databases.
     */
    private fun getData() {
        reference.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Initializes usernames, passwords, and salts
                userNames = ArrayList()
                passwords = ArrayList()
                salts = ArrayList()
                for(i in snapshot.children) {
                    // Adds salt
                    salts.add(i.key!!)
                    Log.v("debugging", "Salt: ${i.key}")
                    val user = i.getValue(User::class.java)!!
                    // Adds username to list
                    userNames.add(user.username)
                    // Adds password to list
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
                // Initializes admin names, passwords, and salts
                adminNames = ArrayList()
                adminPasswords = ArrayList()
                adminSalts = ArrayList()
                for(i in snapshot.children) {
                    // Adds salt
                    adminSalts.add(i.key!!)
                    Log.v("debugging", "Admin Salt: ${i.key}")
                    val admin = i.getValue(User::class.java)!!
                    // Adds username
                    adminNames.add(admin.username)
                    // Adds password
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

    /**
     * Authenticates user to check is found within the player database.
     *
     * Grabs the index number of the inputted user name to be used for extracting password and salt.
     * If both the username and password are valid, exits of out of activity; prints error message
     * otherwise. If user is an admin, passes data to the intent before leaving activity.
     */
    private fun authenticateUser() {
        val application = application as TrailApplication
        // If user is found within the admin database, passes data into intent and exits out of
        // activity
        if(isAnAdmin()) {
            val intent = Intent(this, LogIn::class.java)
            intent.putExtra(ADMIN, true)
            application.currentUser = userEditText.text.toString()
            setResult(RESULT_OK, intent)
            finish()
        } else {
            // Checks index of username found
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

    /**
     * Checks if the inputted username exists in the database.
     *
     * @return Any number if the username exists; -1 if the username doesn't exist.
     */
    private fun authenticateUsername():Int {
        val username = userEditText.text.toString().trim()
        return userNames.indexOf(username)
    }

    /**
     * Checks if the inputted password exists in the database.
     *
     * @param userIndex The index of the username found in the order within the database.
     * @return True if the inputted password is equal to the password found in the database; false
     * otherwise.
     */
    private fun authenticatePassword(userIndex: Int):Boolean {
        val password = passwordEditText.text.toString().trim()
        // Extracts password
        val securePassword = passwords[userIndex]
        Log.v("debugging", "Secure password: $securePassword")
        // Extracts salt
        val salt = salts[userIndex]
        Log.v("debugging", "Salt: $salt")
        // Salts and hashes the password
        val userPassword = securePassword(salt + password)
        return userPassword == securePassword
    }

    /**
     * Checks if the username and password is found within the admin database.
     *
     * @return True if the inputted username and password is found within the admin database; false
     * otherwise.
     */
    private fun isAnAdmin():Boolean {
        val username = userEditText.text.toString().trim()
        // Checks if username exists in the list of admin names
        if(!adminNames.contains(username)) {
            return false
        }
        val adminIndex = adminNames.indexOf(username)
        val password = passwordEditText.text.toString().trim()
        val securePassword = adminPasswords[adminIndex]
        val salt = adminSalts[adminIndex]
        val adminPassword = securePassword(salt + password)
        // Checks if password exists in the list of admin passwords
        if(securePassword != adminPassword) {
            return false
        }
        return true
    }
}