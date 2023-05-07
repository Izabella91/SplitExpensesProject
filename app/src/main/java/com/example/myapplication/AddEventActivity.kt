package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddEventActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val eventName = findViewById<EditText>(R.id.eventNameEditText)
        val eventDate = findViewById<EditText>(R.id.eventDateEditText)
        val eventDescription = findViewById<EditText>(R.id.eventDescriptionEditText)
        val saveEventButton = findViewById<Button>(R.id.saveEventButton)

        saveEventButton.setOnClickListener {
            val name = eventName.text.toString().trim()
            val date = eventDate.text.toString().trim()
            val description = eventDescription.text.toString().trim()

            if (name.isEmpty() || date.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser != null) {
                val event = Event(name = name, date = date, description = description)
                firestore.collection("users").document(currentUser.uid).collection("events")
                    .add(event)
                    .addOnSuccessListener { documentReference ->
                        val eventId = documentReference.id
                        event.id = eventId
                        firestore.collection("users").document(currentUser.uid).collection("events")
                            .document(eventId)
                            .set(event)
                            .addOnSuccessListener {
                                Toast.makeText(this, getString(R.string.event_added_successfully), Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, getString(R.string.error_adding_event, e.localizedMessage), Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, getString(R.string.error_adding_event, e.localizedMessage), Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}