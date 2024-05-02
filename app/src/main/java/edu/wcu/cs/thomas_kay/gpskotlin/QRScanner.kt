package edu.wcu.cs.thomas_kay.gpskotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode

/*
 * MIT License
 *
 * Copyright (c) 2017 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * Activity to scan in a QR code.
 */

/** Result code for camera permission */
const val RESULT_CODE:Int = 101

class QRScanner : AppCompatActivity() {

    /** Scanner to use for reading QR code data */
    private lateinit var scanner: CodeScanner

    /**
     * Sets up activity, checks camera permission, and sets up code scanner find.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscanner)
        checkPermission()
        codeScanner(findViewById(R.id.code_scanner))
    }

    /**
     * Starts preview of code scanner when activity is in the foreground.
     */
    override fun onResume() {
        super.onResume()
        this.scanner.startPreview()
    }

    /**
     * Releases resources of code scanner when activity is not in the foreground.
     */
    override fun onPause() {
        this.scanner.releaseResources()
        super.onPause()
    }

    /**
     * Sets up the code scanner to be used for the activity.
     *
     * Uses observator pattern when a successful QR code is read in.
     *
     * @param codeScannerView The view that contains info for the code scanner class.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun codeScanner(codeScannerView: CodeScannerView) {
        this.scanner = CodeScanner(this, codeScannerView)
        // Sets up the code scanner in regards to its settings
        this.scanner.camera = CodeScanner.CAMERA_BACK
        this.scanner.formats = CodeScanner.ALL_FORMATS
        this.scanner.autoFocusMode = AutoFocusMode.SAFE
        this.scanner.scanMode = ScanMode.CONTINUOUS
        this.scanner.isAutoFocusEnabled = true
        this.scanner.isFlashEnabled = false
        this.scanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                // If data is successfully decrypted and valid, passes data into an intent and exits
                // out of activity
                try {
                    val decrypted = decodeQR(it.text)
                    val decryptedList = decrypted.split(" ")
                    if(decryptedList.size != 3 || decryptedList[0] != TITLE) {
                        Toast.makeText(this, "Incorrect QR code was scanned",
                            Toast.LENGTH_LONG).show()
                    } else {
                        val intent = Intent(this, QRScanner::class.java)
                        // include scanner.releaseResources()?
                        intent.putExtra(QRCODE, decrypted)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                } catch(e:IllegalArgumentException) {
                        Toast.makeText(this, "Incorrect QR code was scanned",
                            Toast.LENGTH_LONG).show()
                }
            }
        }
        this.scanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
        codeScannerView.setOnClickListener {
            scanner.startPreview()
        }
    }

    /**
     * Checks if the user's camera permissions has been granted to the application.
     */
    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                RESULT_CODE)
        }
    }

    /**
     * Checks if user agreed to grant permission to use its camera.
     *
     * If the camera permission is not enabled, exits out of activity.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"Camera permission granted", Toast.LENGTH_LONG).show()
        } else {
            finish()
        }
    }

}