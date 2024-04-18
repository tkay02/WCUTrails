package edu.wcu.cs.thomas_kay.gpskotlin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 AndroidMad / Mushtaq M A
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

const val RESULT_CODE_WRITE = 102
const val TITLE = "WCUTrails"
class QRGenerator : AppCompatActivity() {

    private lateinit var encoder:QRGEncoder
    private lateinit var bitmap:Bitmap
    private lateinit var qr:ImageView
    private lateinit var editText: EditText
    private var canSave = false

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrgenerator)
        checkPermission()
        this.qr = findViewById(R.id.qr_image)
        val create:Button = findViewById(R.id.create_qr)
        create.setOnClickListener { createQR() }
        val save:Button = findViewById(R.id.save_qr)
        save.setOnClickListener { saveQR() }
        this.editText = findViewById(R.id.user_input)
    }

    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE), RESULT_CODE_WRITE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Write to external storage granted",Toast.LENGTH_LONG).show()
        } else {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createQR() {
        val data = editText.text.toString()
        if(data.isEmpty()) {
            Toast.makeText(this, "Cannot create QR code with no data", Toast.LENGTH_LONG).show()
            return
        }
        val dataArray = data.split(" ")
        Log.v("test", data)
        Log.v("test", "${dataArray.size}")
        for(i in dataArray) {
            Log.v("array memeber", i)
        }
        when {
            dataArray.size != 3 -> {
                Toast.makeText(this,"Invalid amount of lines of data; must be 3 lines of data",
                    Toast.LENGTH_LONG).show()
            }
            dataArray[0] != TITLE -> {
                Toast.makeText(this, "Invalid header; must be $TITLE", Toast.LENGTH_LONG).show()
            }
            !isLatOrLng(dataArray[1]) || !isLatOrLng(dataArray[2]) -> {
                Toast.makeText(this, "Invalid data; latitude and longitude values must be doubles",
                    Toast.LENGTH_LONG).show()
            }
            else -> {
                val window = getSystemService(WINDOW_SERVICE) as WindowManager
                val dimen = window.currentWindowMetrics.bounds
                var size = dimen.width()
                if(dimen.width() >= dimen.height()) {
                    size = dimen.height()
                }
                size = size * 3/4
                val encryptedData = encodeQR(data)
                this.encoder = QRGEncoder(encryptedData, QRGContents.Type.TEXT, size)
                this.bitmap = this.encoder.bitmap
                this.qr.setImageBitmap(this.bitmap)
                this.canSave = true
            }
        }
    }

    private fun saveQR() {
        when {
            !this.canSave -> {
                Toast.makeText(this, "Cannot save if QR code is not created yet",
                    Toast.LENGTH_LONG).show()
            }
            !isExternalWritable() -> {
                Toast.makeText(this, "Cannot save without mounted media", Toast.LENGTH_LONG).show()
            }
            else -> {
                try {
                    val path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                    )
                    val fileName = String.format("QRcode%d.jpeg", System.currentTimeMillis())
                    val file = File(path, fileName)
                    val outputStream = FileOutputStream(file)
                    this.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    Toast.makeText(this, "QR code has been successfully saved",
                        Toast.LENGTH_LONG).show()
                } catch(e:FileNotFoundException) {
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
                } catch(e:IOException) {
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun isLatOrLng(data:String):Boolean {
        var isDouble = true
        try {
            data.toDouble()
        } catch(e:NumberFormatException) {
            isDouble = false
        }
        return isDouble
    }

    private fun isExternalWritable():Boolean {
        val state = Environment.getExternalStorageState()!!
        if(Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

}