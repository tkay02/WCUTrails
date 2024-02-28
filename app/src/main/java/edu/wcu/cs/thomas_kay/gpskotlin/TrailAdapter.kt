package edu.wcu.cs.thomas_kay.gpskotlin

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class TrailAdapter(val host:Activity, val data:Array<String>,
                   val listener:TrailItemWasClickedListener?) :
    Adapter<TrailAdapter.TrailViewHolder>() {

    interface TrailItemWasClickedListener {
        fun trailItemWasClicked(text:String)
    }

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

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: TrailViewHolder, position: Int) {
        holder.trailName.text = data[position]
    }

}