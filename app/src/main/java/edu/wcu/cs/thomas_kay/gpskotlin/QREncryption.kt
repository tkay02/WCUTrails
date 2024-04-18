package edu.wcu.cs.thomas_kay.gpskotlin

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Base64

@RequiresApi(Build.VERSION_CODES.O)
fun encodeQR(plaintext:String):String {
    val encoder = Base64.getEncoder()!!
    return encoder.encodeToString(plaintext.encodeToByteArray())
}

@RequiresApi(Build.VERSION_CODES.O)
fun decodeQR(ciphertext:String):String {
    val decoder = Base64.getDecoder()!!
    return decoder.decode(ciphertext).decodeToString()
}