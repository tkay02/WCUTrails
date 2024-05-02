package edu.wcu.cs.thomas_kay.gpskotlin

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.MessageDigest
import java.util.Base64

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * Security module to encrypt and decrypt QR code data and to hash password information.
 */

/**
 * Returns an encrypted Base64 ciphertext of message inputted.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun encodeQR(plaintext:String):String {
    val encoder = Base64.getEncoder()!!
    return encoder.encodeToString(plaintext.encodeToByteArray())
}

/**
 * Decrypts message from a Base64 encrypted message.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun decodeQR(ciphertext:String):String {
    val decoder = Base64.getDecoder()!!
    return decoder.decode(ciphertext).decodeToString()
}

/**
 * Returns a SHA256 hash of an inputted password.
 */
fun securePassword(password:String):String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    return messageDigest.digest(password.encodeToByteArray()).decodeToString()
}