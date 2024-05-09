package edu.wcu.cs.thomas_kay.gpskotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * Adapter that holds data for the list of trail names for a recycler view.
 */

/**
 * @property host The host activity that is using the trail adapter.
 * @property data The data of trail names.
 * @property listener Listener to use when a trail name is clicked.
 */
class TrailAdapter(private val host:Activity, private var data:Array<String>,
                   private val listener:TrailItemWasClickedListener?) :
    Adapter<TrailAdapter.TrailViewHolder>() {

    /**
     * Interface for listening when a trail item was is clicked within the adapter.
     */
    interface TrailItemWasClickedListener {

        /**
         * Activates action when a trail item within the adapter is clicked on.
         */
        fun trailItemWasClicked(text:String)
    }

    /**
     * View holder that displays the trail name.
     *
     * @property trailName TextView that holds the trail name.
     */
    class TrailViewHolder(val trailName:TextView) : ViewHolder(trailName)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrailViewHolder {
        val tv = LayoutInflater.from(parent.context).inflate(
            R.layout.trail_row_item,parent,false) as TextView
        tv.setOnClickListener{
            if(listener != null) {
                val vt = it as TextView
                listener.trailItemWasClicked(vt.text.toString())
            }
        }
        return TrailViewHolder(tv)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterTrailNames(trailName:Array<String>) {
        this.data = trailName
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: TrailViewHolder, position: Int) {
        holder.trailName.text = data[position]
    }

}