package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventsAdapter(
    private var events: List<Event>,
    private val listener: OnItemClickListener?
) : RecyclerView.Adapter<EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event, listener)
    }

    fun updateEvents(events: List<Event>) {
        this.events = events
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onEventItemClick(eventId: String)
    }
}

class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val eventNameTextView: TextView = itemView.findViewById(R.id.eventNameTextView)
    private val eventDateTextView: TextView = itemView.findViewById(R.id.eventDateTextView)

    fun bind(event: Event, listener: EventsAdapter.OnItemClickListener?) {
        eventNameTextView.text = event.name
        eventDateTextView.text = event.date
        itemView.setOnClickListener {
            listener?.onEventItemClick(event.id)
        }
    }
}
