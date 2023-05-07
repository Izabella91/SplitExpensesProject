package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddEventParticipantActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event_participant)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val eventId = intent.getStringExtra("eventId") ?: return

        val friendsRecyclerView = findViewById<RecyclerView>(R.id.friendsRecyclerView)
        val addParticipantsButton = findViewById<Button>(R.id.addParticipantsButton)

        friendsAdapter = FriendsAdapter(listOf(), true)
        friendsRecyclerView.layoutManager = LinearLayoutManager(this)
        friendsRecyclerView.adapter = friendsAdapter

        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    val friends = documents.mapNotNull { it.toObject(Friend::class.java) }
                    friendsAdapter.updateFriends(friends)
                }
                .addOnFailureListener { e ->
                    // Handle error
                }
        }

        addParticipantsButton.setOnClickListener {
            val selectedFriends = friendsAdapter.getSelectedFriends()

            if (selectedFriends.isNotEmpty()) {
                val selectedFriendsEmails = selectedFriends.map { it.email } // Map to emails

                firestore.collection("users").document(currentUser?.uid ?: "")
                    .collection("events").document(eventId)
                    .update("participants", FieldValue.arrayUnion(*selectedFriendsEmails.toTypedArray()))
                    .addOnSuccessListener {
                        Toast.makeText(this, R.string.participants_added_successfully, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, R.string.error_adding_participants, Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, R.string.select_at_least_one_friend, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
