package edu.wcu.cs.thomas_kay.gpskotlin

import android.annotation.SuppressLint
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Cipher transformations supported by javax.crypto:
 *
 *     AES/CBC/NoPadding (128)
 *     AES/CBC/PKCS5Padding (128)
 *     AES/ECB/NoPadding (128)
 *     AES/ECB/PKCS5Padding (128)
 *     DES/CBC/NoPadding (56)
 *     DES/CBC/PKCS5Padding (56)
 *     DES/ECB/NoPadding (56)
 *     DES/ECB/PKCS5Padding (56)
 *     DESede/CBC/NoPadding (168)
 *     DESede/CBC/PKCS5Padding (168)
 *     DESede/ECB/NoPadding (168)
 *     DESede/ECB/PKCS5Padding (168)
 *     RSA/ECB/PKCS1Padding (1024, 2048)
 *     RSA/ECB/OAEPWithSHA-1AndMGF1Padding (1024, 2048)
 *     RSA/ECB/OAEPWithSHA-256AndMGF1Padding (1024, 2048)
 *
 * For key generating
 *
 *     AES (128)
 *     DES (56)
 *     DESede (168)
 *     HmacSHA1
 *     HmacSHA256
 *
 *
 */

const val QR_KEY = "b0cfd87aa7c58350"
const val CIPHER = "AES/ECB/PKCS5Padding" //128
const val MODE = "AES"

@SuppressLint("GetInstance")
fun encrypt(plaintext: String): ByteArray {
    val key = getKey()
    val cipher: Cipher = Cipher.getInstance(CIPHER)
    cipher.init(Cipher.ENCRYPT_MODE, key)
    return cipher.doFinal(plaintext.encodeToByteArray())
}

@SuppressLint("GetInstance")
fun decrypt(ciphertext:ByteArray): String {
    val key = getKey()
    val cipher: Cipher = Cipher.getInstance(CIPHER)
    cipher.init(Cipher.DECRYPT_MODE, key)
    return cipher.doFinal(ciphertext).decodeToString()
}

fun getKey():SecretKey {
    val encryptionKey = QR_KEY.encodeToByteArray()
    val key:SecretKey = SecretKeySpec(encryptionKey, MODE)
    return key
}

fun cryptoTesting() {
    Log.v("Cipher", "key: ${getKey()}")
    val ciphertext = encrypt("Hello World")
    Log.v("Cipher", "ciphertext: $ciphertext")
    val plaintext = decrypt(ciphertext)
    Log.v("Cipher", "plaintext: $plaintext")
}