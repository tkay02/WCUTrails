package edu.wcu.cs.thomas_kay.gpskotlin

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

const val MULTIPLAYER_NAME = "Games"
const val CODE_LENGTH = 6
const val HALF = .5

class MultiplayerGame(var key:String? = null) {
    lateinit var code:String
    lateinit var playerCoords: ArrayList<LatLng>
    lateinit var playerTime: ArrayList<Int>
    lateinit var playerQRCodes: ArrayList<Int>
    val reference = FirebaseDatabase.getInstance().getReference(MULTIPLAYER_NAME)

    init {
        reference.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(key != null) {
                    Log.v("game loading", "My key: $key")
                    for(i in snapshot.children) {
                        Log.v("game loading", "Snapshot key: ${i.key}")
                        if(key == i.key) {
                            for((cnt,j) in i.children.withIndex()) {
                                val player = j.getValue(Player::class.java)
                                playerCoords[cnt] = LatLng(player?.lat!!, player.lng)
                                playerTime[cnt] = player.time
                                playerQRCodes[cnt] = player.qrCodes
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.v("error", "Error: ${error.message}")
            }

        })
    }



    fun createGame() {
        generateCode()
        val game = Game(this.code)
        key = reference.push().key
        reference.child(key!!).setValue(game)
    }

    fun joinGame(code:String) {
        val player = Player()
        val playerKey = reference.child(key!!).push().key
        reference.child(key!!).child(playerKey!!).setValue(player)
    }

    private fun generateCode() {
        var code = ""
        for(i in 0..<CODE_LENGTH) {
            val rng1 = Math.random()
            val rng2 = if(rng1 < HALF) {
                (48..57).shuffled().first() //Number list
            } else {
                (65..90).shuffled().first() //Capital list
            }
            val codeChar = rng2.toChar().toString()
            code += codeChar
        }
        this.code = code
    }

    fun destoryGame() {
        this.reference.child(key!!).removeValue()
    }

    //Need removePlayer function as well

}

data class Game(val code:String = "")

data class Player(val lat: Double = 0.0, val lng: Double = 0.0, val time: Int = 0,
    val qrCodes: Int = 0)