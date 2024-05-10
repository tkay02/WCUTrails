package edu.wcu.cs.thomas_kay.gpskotlin

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


/**
 * Not implemented yet
 */

/** Reference to the multiplayer game database to host games  */
const val MULTIPLAYER_NAME = "Games"
/** The required length for the code/id of a game */
const val CODE_LENGTH = 6
/** Half */
const val HALF = .5

class MultiplayerGame(var key:String? = null) {
    /** Code/id of the game to join/host */
    lateinit var code:String
    /** List of each player's coordinates */
    lateinit var playerCoords: ArrayList<LatLng>
    /** List of each player's time */
    lateinit var playerTime: ArrayList<Int>
    /** List of each player's qr codes */
    lateinit var playerQRCodes: ArrayList<Int>
    /** Reference to multiplayer game database */
    val reference = FirebaseDatabase.getInstance().getReference(MULTIPLAYER_NAME)

    /** Sets up reference to server */
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



    /** Create a new game entry */
    fun createGame() {
        generateCode()
        val game = Game(this.code)
        key = reference.push().key
        reference.child(key!!).setValue(game)
    }

    /** Join a game from an inputted code */
    fun joinGame(code:String) {
        val player = Player()
        val playerKey = reference.child(key!!).push().key
        reference.child(key!!).child(playerKey!!).setValue(player)
    }

    /** Generate the code to be used for a multiplayer game */
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

    /** Remove the game from the database */
    fun destoryGame() {
        this.reference.child(key!!).removeValue()
    }

    //Need removePlayer function as well

}

//Not used data classes
data class Game(val code:String = "")

data class Player(val lat: Double = 0.0, val lng: Double = 0.0, val time: Int = 0,
    val qrCodes: Int = 0)