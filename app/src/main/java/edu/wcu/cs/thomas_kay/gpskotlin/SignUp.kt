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
 * Creates an user/admin for the database.
 */

/** Path to the user database in Firebase */
const val FIREBASE_USER = "User"
/** Path to the admin database in Firebase */
const val FIREBASE_ADMIN = "Admin"
/** The minimal length of for an user password */
const val MIN_PASSWORD_LENGTH = 8
class SignUp : AppCompatActivity() {

    /** Edittext to input username */
    private lateinit var usernameEditText: EditText
    /** Edittext to input user's password */
    private lateinit var password1EditText: EditText
    /** Edittext to repeat user's password to help them remember it */
    private lateinit var password2EditText: EditText
    /** Reference to the user database */
    private val reference = FirebaseDatabase.getInstance().getReference(FIREBASE_USER)
    /** Reference to the admin database */
    private val adminReference = FirebaseDatabase.getInstance().getReference(FIREBASE_ADMIN)
    /** Reference to the list of usernames for uniqueness */
    private lateinit var usernames: ArrayList<String>
    /** Reference to the list of admin names for uniqueness */
    private lateinit var adminNames: ArrayList<String>

    /**
     * Sets up edit texts and button to be used in the activity. Also loads in usernames to maintain
     * check for uniqueness for new user's username.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        getUserNames()
        val submit:Button = this.findViewById(R.id.submit)
        usernameEditText = this.findViewById(R.id.username_edit)
        password1EditText = this.findViewById(R.id.password_one_edit)
        password2EditText = this.findViewById(R.id.password_two_edit)
        submit.setOnClickListener { createAccount() }
        // Comment this out
        //val admin:Button = this.findViewById(R.id.create_admin)
        // Comment this one out as well
        //admin.setOnClickListener { createAdmin() }
    }

    /**
     * Checks if a username or password contains no space characters.
     *
     * @param text A user's username or password.
     * @return True if there were no space characters found; false otherwise.
     */
    private fun hasNoSpaces(text:String):Boolean {
        val charArray = text.toCharArray()
        for(i in charArray) {
            if(i.isWhitespace()) return false
        }
        return true
    }

    /**
     * Checks if a digit is contained within the password.
     *
     * @param password The user's inputted password.
     * @return True if the password contains a digit; false otherwise.
     */
    private fun hasADigit(password:String):Boolean {
        val charArray = password.toCharArray()
        for(i in charArray) {
            if(i.isDigit()) return true
        }
        return false
    }

    /**
     * Checks if a symbol is found within the password by using regex.
     *
     * @param password The user's inputted password.
     * @return True if the password contains a symbol; false otherwise.
     */
    private fun hasASymbol(password:String):Boolean {
        return password.contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())
    }

    /**
     * Obtain list of usernames found in the user database and the admin database.
     *
     * List is obtained to make sure that no users share the same password.
     */
    private fun getUserNames() {
        this.reference.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.v("debugging", "Num of children: ${snapshot.childrenCount}")
                usernames = ArrayList()
                for(i in snapshot.children) {
                    // Adds username to the list
                    usernames.add(i.getValue(User::class.java)!!.username)
                    Log.v("debugging", "Username: ${usernames[usernames.size - 1]}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.v("error", "$error")
            }
        })
        this.adminReference.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                adminNames = ArrayList()
                for(i in snapshot.children) {
                    // Adds admin name to the list
                    adminNames.add(i.getValue(User::class.java)!!.username)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.v("error", "$error")
            }
        })
    }

    /**
     * Creates a new user to the database if the username and password is valid.
     *
     * Also checks if both passwords inputted by the user are the same as well. Once the username
     * and passwords are valid, salts and hashes the password so that it can be stored onto the
     * user's database. If the user was successfully in creating an activity, exits out of the
     * activity.
     */
    private fun createAccount() {
        if(usernameCheck() && passwordCheck()) {
            val password1 = password1EditText.text.toString()
            val password2 = password2EditText.text.toString()
            // Check if both passwords are the same
            if(password1 == password2) {
                val username = usernameEditText.text.toString()
                val salt = reference.push().key!!
                // Salt and hash password
                val finalPassword = securePassword(salt + password1)
                Log.v("debugging", "Password: $finalPassword")
                // Creates a user and uploads user to the database
                val user = User(username, finalPassword)
                reference.child(salt).setValue(user)
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_LONG).show()
                val intent = Intent(this, SignUp::class.java)
                // Sets result to ok and leaves activity.
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, "The two passwords do not match", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Only used for creating admins
    /**
     * Creates a new admin to the database if the username and password is valid.
     *
     * Also checks if both passwords inputted by the user are the same as well. Once the username
     * and passwords are valid, salts and hashes the password so that it can be stored onto the
     * admin's database.
     */
    private fun createAdmin() {
        if(usernameCheck() && passwordCheck()) {
            val password1 = password1EditText.text.toString()
            val password2 = password2EditText.text.toString()
            // Check if both passwords are the same
            if(password1 == password2) {
                val username = usernameEditText.text.toString()
                val salt = adminReference.push().key!!
                // Salt and hash password
                val finalPassword = securePassword(salt + password1)
                // Creates an admin and uploads admin to the database
                val admin = User(username, finalPassword)
                adminReference.child(salt).setValue(admin)
            }
        }
    }

    /**
     * Checks if the username inputted by the user is valid.
     *
     * @return True if the username is valid; false is otherwise.
     */
    private fun usernameCheck():Boolean {
        var isValid = false
        val username = this.usernameEditText.text.toString()
        when {
            // Invalid if the username is nothing
            username == "" -> {
                Toast.makeText(this, "Username cannot be nothing", Toast.LENGTH_LONG).show()
            }
            // Invalid if the username already exists in the user database
            usernames.contains(username) -> {
                Toast.makeText(this, "Username has already been chosen", Toast.LENGTH_LONG).show()
            }
            // Invalid if the username already exists in the admin database
            adminNames.contains(username) -> {
                Toast.makeText(this, "Username has already been chosen", Toast.LENGTH_LONG).show()
            }
            // Invalid if the username has space characters
            !hasNoSpaces(username) -> {
                Toast.makeText(this, "Username cannot have spaces", Toast.LENGTH_LONG).show()
            }
            else -> {
                isValid = true
            }
        }
        return isValid
    }

    /**
     * Checks if the password inputted by the user is valid.
     *
     * @return True if the password is valid; false is otherwise.
     */
    private fun passwordCheck():Boolean {
        var isValid = false
        val password = this.password1EditText.text.toString()
        when {
            // Invalid if the password is less than the required minimal length
            password.length < MIN_PASSWORD_LENGTH -> {
                Toast.makeText(this, "Invalid length for password", Toast.LENGTH_LONG).show()
            }
            // Invalid if the password has a space character
            !hasNoSpaces(password) -> {
                Toast.makeText(this, "Password cannot have space characters",
                    Toast.LENGTH_LONG).show()
            }
            // Invalid if the password doesn't have a digit
            !hasADigit(password) -> {
                Toast.makeText(this, "Password must require at least one digit character",
                    Toast.LENGTH_LONG).show()
            }
            // Invalid if the password doesn't have a symbol
            !hasASymbol(password) -> {
                Toast.makeText(this, "Password must require at least one symbol character",
                    Toast.LENGTH_LONG).show()
            }
            else -> {
                isValid = true
            }
        }
        return isValid
    }

}

/**
 * Data class that is used to represent the schema of a user within the user and admin database.
 *
 * @property username The unique name of the user.
 * @property password The hashed password of the user.
 */
data class User(val username: String = "", val password: String = "")