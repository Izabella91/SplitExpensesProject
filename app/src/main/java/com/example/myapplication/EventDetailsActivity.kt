package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventDetailsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var participantsAdapter: EventParticipantsAdapter
    private lateinit var expensesAdapter: ExpensesAdapter

    private var friendsList = listOf<Friend>()

    private lateinit var eventNameTextView: TextView
    private lateinit var eventDateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fetchFriendsList()
        observeEventDetails()
        val eventId = intent.getStringExtra("eventId") ?: return

        eventNameTextView = findViewById(R.id.eventNameTextView)
        eventDateTextView = findViewById(R.id.eventDateTextView)
        val addParticipantsButton = findViewById<Button>(R.id.addParticipantsButton)
        val addExpenseButton = findViewById<Button>(R.id.addExpenseButton)
        val participantsRecyclerView = findViewById<RecyclerView>(R.id.participantsRecyclerView)
        val expensesRecyclerView = findViewById<RecyclerView>(R.id.expensesRecyclerView)
        val showBalancesButton = findViewById<Button>(R.id.showBalancesButton)

        participantsAdapter = EventParticipantsAdapter(listOf())
        participantsRecyclerView.layoutManager = LinearLayoutManager(this)
        participantsRecyclerView.adapter = participantsAdapter

        expensesAdapter = ExpensesAdapter(listOf(), friendsList)
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)
        expensesRecyclerView.adapter = expensesAdapter

        addParticipantsButton.setOnClickListener {
            // Navigate to AddEventParticipantActivity
            val intent = Intent(this, AddEventParticipantActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }

        addExpenseButton.setOnClickListener {
            // Navigate to AddExpenseActivity
            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }

        showBalancesButton.setOnClickListener {
            fetchEvent(eventId) { event ->
                Log.d("DEBUG", "Fetched event: $event")
                val intent = Intent(this, BalancesActivity::class.java)
                intent.putExtra("event", event) // przekazuje obiekt Event
                startActivity(intent)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        Log.d("DEBUG", "onResume called")
        // Refresh the friends list and participants list
        fetchFriendsList()
        observeEventDetails()
    }

    private fun fetchFriendsList() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    friendsList =
                        documents.mapNotNull { document -> document.toObject(Friend::class.java) }
                    participantsAdapter.updateParticipants(friendsList)
                    expensesAdapter.updateFriends(friendsList)
                }
                .addOnFailureListener { e ->
                    // Handle error
                    Toast.makeText(
                        this,
                        getString(R.string.error_fetching_friends) + e.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun observeEventDetails() {
        val eventId = intent.getStringExtra("eventId") ?: return
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).collection("events")
                .document(eventId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Handle error
                        Toast.makeText(
                            this,
                            getString(R.string.error_fetching_participants, error.localizedMessage),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        // Get event details
                        val event = snapshot.toObject(Event::class.java)
                        // Update participants list
                        val participantIds = event?.participants ?: listOf()
                        val participants = friendsList.filter { it.email in participantIds }
                        participantsAdapter.updateParticipants(participants)

                        // Fetch expenses
                        fetchExpenses(eventId)

                        // Update event details
                        eventNameTextView.text = event?.name
                        eventDateTextView.text = event?.date
                    }
                }
        }
    }

    private fun fetchExpenses(eventId: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).collection("events")
                .document(eventId)
                .collection("expenses")
                .get()
                .addOnSuccessListener { documents ->
                    val expenses = documents.mapNotNull { it.toObject(Expense::class.java) }
                    Log.d("DEBUG", "Expenses fetched: $expenses")
                    expensesAdapter.updateExpenses(expenses)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, getString(R.string.error_fetching_expenses, e.message), Toast.LENGTH_SHORT).show()
                }
        }
    }
    @SuppressLint("StringFormatInvalid")
    private fun fetchEvent(eventId: String, onSuccess: (Event) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener { document ->
                    val event = document.toObject(Event::class.java)
                    if (event != null) {
                        onSuccess(event)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        getString(R.string.error_fetching_event_details, e.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
