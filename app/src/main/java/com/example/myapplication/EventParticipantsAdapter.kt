package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class EventParticipantsAdapter(private var participants: List<Friend>) : RecyclerView.Adapter<EventParticipantsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.participant_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.participant_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = participants[position]
        holder.nameTextView.text = "${friend.firstName} ${friend.lastName}"
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    fun updateParticipants(newParticipants: List<Friend>) {
        participants = newParticipants
        notifyDataSetChanged()
    }
}
