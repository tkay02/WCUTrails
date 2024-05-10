package edu.wcu.cs.thomas_kay.gpskotlin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * Fragment to hold the code of a host's game.
 */

class HostCode : Fragment() {

    /** TextView to display the code */
    private lateinit var textView: TextView

    /**
     * Creates the view for the host code fragment.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host_code, container, false)
    }

    /**
     * Sets the code of the textview.
     */
    fun setCode(code:String) {
        val view = requireView()
        textView = view.findViewById(R.id.host_code)
        textView.text = code
    }

}