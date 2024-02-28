package edu.wcu.cs.thomas_kay.gpskotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.wcu.cs.thomas_kay.gpskotlin.TrailDatabaseHelper.Companion.LIST_OF_TRAILS

const val TRAIL_NAME:String = "TrailName"
const val TRAIL_INDEX:String = "TrailIndex"

class TrailSelector : AppCompatActivity(), TrailAdapter.TrailItemWasClickedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trail_selector)

        //val search:SearchView = this.findViewById(R.id.search_bar)
        val recyclerView:RecyclerView = this.findViewById(R.id.recycler)
        recyclerView.setHasFixedSize(true)
        val adapter = TrailAdapter(this, LIST_OF_TRAILS, this)
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
        intent.putExtra(TRAIL_INDEX, LIST_OF_TRAILS.indexOf(text))
        startActivity(intent)
    }
}

abstract class AnonymousOnQueryTextListener() : SearchView.OnQueryTextListener
