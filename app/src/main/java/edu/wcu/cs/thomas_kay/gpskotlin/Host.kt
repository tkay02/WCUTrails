package edu.wcu.cs.thomas_kay.gpskotlin

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Host : AppCompatActivity() {

    private lateinit var recycle:RecyclerView
    private lateinit var code:String
    private val reference = FirebaseDatabase.getInstance().getReference(MULTIPLAYER_NAME)
    private lateinit var codeArrayList: ArrayList<String>
    private var count = 0;
    private lateinit var switchButton: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
        switchButton = findViewById(R.id.switch_frags)
        switchButton.setOnClickListener { switchFragments() }
    }

    private fun setReference() {
        reference.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun switchFragments() {
        val frag:Fragment
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        if(count % 2 == 0) {
            frag = HostQR()
            frag.createQR(this.code)
            switchButton.text = "Switch to Code"
        } else {
            frag = HostCode()
            frag.setCode(this.code)
            switchButton.text = "Switch to QRCode"
        }
        fragmentTransaction.replace(R.id.host_code, frag)
        fragmentTransaction.commit()
        this.count++
    }
}