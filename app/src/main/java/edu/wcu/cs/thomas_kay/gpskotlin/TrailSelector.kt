package edu.wcu.cs.thomas_kay.gpskotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

const val TRAIL_NAME:String = "TrailName"

class TrailSelector : AppCompatActivity(), TrailAdapter.TrailItemWasClickedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trail_selector)

        //val search:SearchView = this.findViewById(R.id.search_bar)
        val recyclerView:RecyclerView = this.findViewById(R.id.recycler)
        recyclerView.setHasFixedSize(true)
        val trailArray = resources.getStringArray(R.array.name_of_trails)
        trailArray.sort()
        val adapter = TrailAdapter(this, trailArray, this)
        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        //IMPLEMENT THIS!!!
        /*
        search.setOnQueryTextListener(object:AnonymousOnQueryTextListener() {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                TODO("Not yet implemented")
            }

        })
         */
    }

    override fun trailItemWasClicked(text: String) {
        val intent = Intent(this, TrailObserver::class.java)
        intent.putExtra(TRAIL_NAME, text)
        startActivity(intent)
    }
}

abstract class AnonymousOnQueryTextListener() : SearchView.OnQueryTextListener
