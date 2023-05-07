package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class AddExpenseActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var eventId: String
    private lateinit var expenseNameEditText: EditText
    private lateinit var expenseAmountEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var saveExpenseButton: Button
    private lateinit var payerSpinner: Spinner
    private var selectedCategory: String = ""
    private var selectedPayer: Friend? = null
    private var participants: MutableList<Friend> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        eventId = intent.getStringExtra("eventId") ?: ""

        expenseNameEditText = findViewById(R.id.expense_name_edit_text)
        expenseAmountEditText = findViewById(R.id.expense_amount_edit_text)
        categorySpinner = findViewById(R.id.categorySpinner)
        saveExpenseButton = findViewById(R.id.save_expense_button)
        payerSpinner = findViewById(R.id.payer_spinner)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupCategorySpinner()
        setupPayerSpinner()

        fetchEventParticipants()

        saveExpenseButton.setOnClickListener {
            saveExpense()
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf(
            "Food",
            "Transportation",
            "House",
            "Other"
        ) // Tutaj wprowadź swoje kategorie
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = ""
            }
        }
    }

    private fun setupPayerSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, participants)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        payerSpinner.adapter = adapter

        payerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPayer = participants[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedPayer = null
            }
        }
    }

    private fun fetchEventParticipants() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener { document ->
                    val event = document.toObject(Event::class.java)
                    val participantIds = event?.participants ?: listOf()
                    firestore.collection("users").document(currentUser.uid).collection("friends")
                        .whereIn("email", participantIds)
                        .get()
                        .addOnSuccessListener { documents ->
                            val fetchedParticipants = documents.mapNotNull { it.toObject(Friend::class.java) }
                            participants.clear()
                            participants.addAll(fetchedParticipants)
                            setupPayerSpinner()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                getString(R.string.error_fetching_friends) + e.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        getString(R.string.error_fetching_participants) + e.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    private fun saveExpense() {
        val expenseName = expenseNameEditText.text.toString().trim()
        val expenseAmountString = expenseAmountEditText.text.toString().trim()
        val payerEmail = selectedPayer?.email ?: ""

        if (expenseName.isEmpty() || expenseAmountString.isEmpty() || selectedCategory.isEmpty() || payerEmail.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT)
                .show()
            return
        }

        val expenseAmount = expenseAmountString.toDoubleOrNull()
        if (expenseAmount == null) {
            Toast.makeText(this, getString(R.string.error_invalid_amount), Toast.LENGTH_SHORT)
                .show()
            return
        }

        val expense = Expense(
            description = expenseName,
            amount = expenseAmount,
            category = selectedCategory,
            paidBy = payerEmail
        )

        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).collection("events")
                .document(eventId).collection("expenses")
                .add(expense)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        getString(R.string.expense_saved_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        getString(R.string.error_saving_expense) + e.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(this, getString(R.string.error_user_not_logged_in), Toast.LENGTH_SHORT)
                .show()
        }
    }
}