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

const val FIREBASE_USER = "User"
const val FIREBASE_ADMIN = "Admin"
const val MIN_PASSWORD_LENGTH = 8
class SignUp : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var password1EditText: EditText
    private lateinit var password2EditText: EditText
    private val reference = FirebaseDatabase.getInstance().getReference(FIREBASE_USER)
    private lateinit var usernames: ArrayList<String>

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

    private fun hasNoSpaces(text:String):Boolean {
        val charArray = text.toCharArray()
        for(i in charArray) {
            if(i.isWhitespace()) return false
        }
        return true
    }

    private fun hasADigit(password:String):Boolean {
        val charArray = password.toCharArray()
        for(i in charArray) {
            if(i.isDigit()) return true
        }
        return false
    }

    private fun hasASymbol(password:String):Boolean {
        return password.contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())
    }

    private fun getUserNames() {
        this.reference.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.v("debugging", "Num of children: ${snapshot.childrenCount}")
                usernames = ArrayList()
                for(i in snapshot.children) {
                    usernames.add(i.getValue(User::class.java)!!.username)
                    Log.v("debugging", "Username: ${usernames[usernames.size - 1]}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.v("error", "$error")
            }

        })
    }

    private fun createAccount() {
        if(usernameCheck() && passwordCheck()) {
            val password1 = password1EditText.text.toString()
            val password2 = password2EditText.text.toString()
            if(password1 == password2) {
                val username = usernameEditText.text.toString()
                val salt = reference.push().key!!
                // Salt and hash password
                val finalPassword = securePassword(salt + password1)
                Log.v("debugging", "Password: $finalPassword")
                val user = User(username, finalPassword)
                reference.child(salt).setValue(user)
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_LONG).show()
                val intent = Intent(this, SignUp::class.java)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, "The two passwords do not match", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Only used for creating admins
    private fun createAdmin() {
        val adminReference = FirebaseDatabase.getInstance().getReference(FIREBASE_ADMIN)
        if(usernameCheck() && passwordCheck()) {
            val password1 = password1EditText.text.toString()
            val password2 = password2EditText.text.toString()
            if(password1 == password2) {
                val username = usernameEditText.text.toString()
                val salt = adminReference.push().key!!
                val finalPassword = securePassword(salt + password1)
                val admin = User(username, finalPassword)
                adminReference.child(salt).setValue(admin)
            }
        }
    }

    private fun usernameCheck():Boolean {
        var isValid = false
        val username = this.usernameEditText.text.toString()
        when {
            username == "" -> {
                Toast.makeText(this, "Username cannot be nothing", Toast.LENGTH_LONG).show()
            }
            usernames.contains(username) -> {
                Toast.makeText(this, "Username has already been chosen", Toast.LENGTH_LONG).show()
            }
            !hasNoSpaces(username) -> {
                Toast.makeText(this, "Username cannot have spaces", Toast.LENGTH_LONG).show()
            }
            else -> {
                isValid = true
            }
        }
        return isValid
    }

    private fun passwordCheck():Boolean {
        var isValid = false
        val password = this.password1EditText.text.toString()
        when {
            password.length < MIN_PASSWORD_LENGTH -> {
                Toast.makeText(this, "Invalid length for password", Toast.LENGTH_LONG).show()
            }
            !hasNoSpaces(password) -> {
                Toast.makeText(this, "Password cannot have space characters",
                    Toast.LENGTH_LONG).show()
            }
            !hasADigit(password) -> {
                Toast.makeText(this, "Password must require at least one digit character",
                    Toast.LENGTH_LONG).show()
            }
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

// Include scores as fields
data class User(val username: String = "", val password: String = "")