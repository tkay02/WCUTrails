package edu.wcu.cs.thomas_kay.gpskotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode

const val RESULT_CODE:Int = 101

class QRScanner : AppCompatActivity() {

    private lateinit var scanner: CodeScanner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscanner)
        checkPermission()
        codeScanner(findViewById<CodeScannerView>(R.id.code_scanner))
    }

    override fun onResume() {
        super.onResume()
        this.scanner.startPreview()
    }

    override fun onPause() {
        this.scanner.releaseResources()
        super.onPause()
    }

    private fun codeScanner(codeScannerView: CodeScannerView) {
        this.scanner = CodeScanner(this, codeScannerView)
        this.scanner.camera = CodeScanner.CAMERA_BACK
        this.scanner.formats = CodeScanner.ALL_FORMATS
        this.scanner.autoFocusMode = AutoFocusMode.SAFE
        this.scanner.isAutoFocusEnabled = true
        this.scanner.isFlashEnabled = false
        this.scanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val intent = Intent(this, QRScanner::class.java)
                intent.putExtra(QRCODE, it.text)
                // include? scanner.releaseResources()
                setResult(RESULT_OK, intent)
                finish()
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

    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), RESULT_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == RESULT_CODE) {
            Toast.makeText(this,"Camera permission granted", Toast.LENGTH_LONG).show()
        }
    }

}