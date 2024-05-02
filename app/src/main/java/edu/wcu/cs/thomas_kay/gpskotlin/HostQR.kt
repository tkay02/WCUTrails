package edu.wcu.cs.thomas_kay.gpskotlin

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.annotation.RequiresApi

class HostQR : Fragment() {
    private lateinit var qrCode: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var encoder: QRGEncoder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host_q_r, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createQR(code:String) {
        val view = requireView()
        this.qrCode = view.findViewById(R.id.host_qr)
        generateQR(code)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateQR(code:String) {
        val code2 = "$TITLE code $code"
        val encryptedCode = encodeQR(code2)
        this.encoder = QRGEncoder(encryptedCode, QRGContents.Type.TEXT)
        this.bitmap = this.encoder.bitmap
        this.qrCode.setImageBitmap(this.bitmap)
    }

}