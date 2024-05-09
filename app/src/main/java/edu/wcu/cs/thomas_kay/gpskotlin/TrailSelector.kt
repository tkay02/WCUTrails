package edu.wcu.cs.thomas_kay.gpskotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * Activity used to allow the user to select on a trail.
 */

/** Name for trail name data for an intent */
const val TRAIL_NAME:String = "TrailName"
/** Name for intent that the activity is used for an activity for result */
const val RESULT_ACTIVITY:String = "ForResult"

class TrailSelector : AppCompatActivity(), TrailAdapter.TrailItemWasClickedListener {

    /** Boolean that determines if the activity is being used as an activity for result or not */
    private var isActivityForResult = false
    /** Reference to adapter that contains trail name date */
    private lateinit var adapter: TrailAdapter

    /**
     * Sets up the activity. If bundle is passed, determines if the activity is treated as a normal
     * activity or has an activity for result. Sets up a toolbar so that a search view can be used
     * for the activity to search for trail names. Sets up an adapter and recycler view to navigate
     * through list of trail names.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trail_selector)
        val bundle = intent.extras
        if(bundle != null) {
            isActivityForResult = bundle.getBoolean(RESULT_ACTIVITY)
        }
        // Sets toolbar to display menu with search view
        val toolbar:Toolbar = this.findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val recyclerView:RecyclerView = this.findViewById(R.id.recycler)
        recyclerView.setHasFixedSize(true)
        val application = application as TrailApplication
        val trailArray = application.getTrailNames()
        trailArray.sort()
        // Sets adapter
        adapter = TrailAdapter(this, arrayListToArray(trailArray), this)
        // Sets recycler view to display date in adapter
        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
    }

    /**
     * Creates menu within the activity. Sets up the search view within the menu. The search view is
     * used to help the user to find the trail they want by typing what parts of the trail name is
     * used.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflator = menuInflater
        inflator.inflate(R.menu.search_menu, menu)
        val search = menu?.findItem(R.id.search_trail)
        val searchView = search?.actionView as SearchView
        searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            // Updates the data within the adapter through the filter method
            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText!!)
                return false
            }
        })
        return true
    }

    /**
     * Filters through data in the adapter to display trails that match the closest to the user's
     * inputted text.
     *
     * @param text Inputted text from the search view by an user.
     */
    private fun filter(text:String) {
        val app = application as TrailApplication
        // Gets list of trail names
        val trailNames = app.getTrailNames()
        // Create a new list for filtering names
        val filterList = ArrayList<String>()
        // Iterate through all trail names
        for(trail in trailNames) {
            // If the name contains some of the text inputted by the user, adds the name into the
            // filtering list
            if(trail.lowercase().contains(text.lowercase())) {
                filterList.add(trail)
            }
        }
        // Updates the data of the adapter if filter list is not empty
        if(filterList.isNotEmpty()) {
            adapter.filterTrailNames(arrayListToArray(filterList))
        }
    }

    /**
     * Implemented method of trailItemWasClicked.
     *
     * When clicked, the activity determines whether to end the activity or not depending on the
     * isActivityForResult field. If enabled, passes text into an intent, sets result code to ok,
     * and exits activity. Otherwise, starts new activity with text being passed.
     *
     * @param text The name of the trail that the user clicked on.
     */
    override fun trailItemWasClicked(text: String) {
        if(isActivityForResult) {
            val intent = Intent(this, TrailSelector::class.java)
            intent.putExtra(TRAIL_NAME, text)
            setResult(RESULT_OK, intent)
            finish()
        } else {
            val intent = Intent(this, TrailQR::class.java)
            intent.putExtra(TRAIL_NAME, text)
            startActivity(intent)
        }
    }

    /** Converts an arraylist of strings into an array of strings */
    private fun arrayListToArray(list: ArrayList<String>):Array<String> {
        val array = Array(list.size) {""}
        var i = 0
        while(i < array.size) {
            array[i] = list[i]
            i++
        }
        return array
    }
}
