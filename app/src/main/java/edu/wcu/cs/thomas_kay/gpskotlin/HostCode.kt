package edu.wcu.cs.thomas_kay.gpskotlin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class HostCode : Fragment() {

    private lateinit var textView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host_code, container, false)
    }

    fun setCode(code:String) {
        val view = requireView()
        textView = view.findViewById(R.id.host_code)
        textView.text = code
    }

}