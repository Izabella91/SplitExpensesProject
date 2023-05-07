package com.example.myapplication


import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddFriendActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val saveFriendButton = findViewById<Button>(R.id.saveFriendButton)
        saveFriendButton.setOnClickListener {val firstName = findViewById<EditText>(R.id.friendFirstNameEditText).text.toString().trim()
            val lastName = findViewById<EditText>(R.id.friendLastNameEditText).text.toString().trim()
            val email = findViewById<EditText>(R.id.friendEmailEditText).text.toString().trim()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty()) {
                saveFriend(firstName, lastName, email)
            } else {
                Toast.makeText(this, getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show()
            }
        }

    }
    @SuppressLint("StringFormatInvalid")
    private fun saveFriend(firstName: String, lastName: String, email: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val friend = Friend(firstName, lastName, email)
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("friends")
                .add(friend)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.friend_added), Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, getString(R.string.error_adding_friend, e.localizedMessage), Toast.LENGTH_SHORT).show()
                }
        }
    }
}
