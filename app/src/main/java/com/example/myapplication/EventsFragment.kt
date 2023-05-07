package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventsFragment : Fragment(), EventsAdapter.OnItemClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var eventsAdapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val eventsRecyclerView = view.findViewById<RecyclerView>(R.id.eventsRecyclerView)
        eventsAdapter = EventsAdapter(emptyList(), this)
        eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        eventsRecyclerView.adapter = eventsAdapter

        val addEventButton = view.findViewById<FloatingActionButton>(R.id.addEventButton)
        addEventButton.setOnClickListener {
            val intent = Intent(requireContext(), AddEventActivity::class.java)
            startActivity(intent)
        }

        fetchEvents()
    }

    override fun onResume() {
        super.onResume()
        fetchEvents()
    }

    @SuppressLint("StringFormatInvalid")
    private fun fetchEvents() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("events")
                .get()
                .addOnSuccessListener { snapshot ->
                    val events = snapshot.documents.mapNotNull { document ->
                        val eventSnapshot = document.data
                        if (eventSnapshot != null) {
                            val participants = eventSnapshot["participants"] as? List<String> ?: emptyList()
                            Event(
                                id = document.id,
                                name = eventSnapshot["name"] as String,
                                date = eventSnapshot["date"] as String,
                                description = eventSnapshot["description"] as String,
                                participants = participants
                            )
                        } else {
                            null
                        }
                    }
                    eventsAdapter.updateEvents(events)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), getString(R.string.error_fetching_events, e.localizedMessage), Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onEventItemClick(eventId: String) {
        val intent = Intent(requireContext(), EventDetailsActivity::class.java)
        intent.putExtra("eventId", eventId)
        startActivity(intent)
    }
}
