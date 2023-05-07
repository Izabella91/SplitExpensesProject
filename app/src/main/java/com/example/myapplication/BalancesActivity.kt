package com.example.myapplication
import kotlin.math.min

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class BalancesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var balancesAdapter: BalancesAdapter
    private lateinit var eventNameTextView: TextView
    private lateinit var summaryTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balances)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        eventNameTextView = findViewById(R.id.eventNameTextView)
        val balancesRecyclerView = findViewById<RecyclerView>(R.id.balancesRecyclerView)
        summaryTextView = findViewById(R.id.summaryTextView)

        balancesAdapter = BalancesAdapter(listOf())
        balancesRecyclerView.layoutManager = LinearLayoutManager(this)
        balancesRecyclerView.adapter = balancesAdapter
        val eventName = intent.getStringExtra("eventName") ?: ""
        eventNameTextView.text = eventName
        val event = intent.getParcelableExtra<Event>("event")
        if (event != null) {
            fetchEventDetailsAndCalculateBalances(event.id)
        } else {
            Toast.makeText(
                this,
                getString(R.string.error_no_event_data),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    @SuppressLint("StringFormatInvalid")
    private fun fetchEventDetailsAndCalculateBalances(eventId: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val eventRef = firestore.collection("users").document(currentUser.uid)
                .collection("events").document(eventId)

            eventRef.get().addOnSuccessListener { document ->
                val event = document.toObject(Event::class.java)
                if (event != null) {
                    eventNameTextView.text = event.name
                    fetchFriends { friends ->
                        val participants = friends.filter { it.email in event.participants }
                        calculateBalances(eventId, participants)
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    getString(R.string.error_fetching_event_details, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun calculateBalances(eventId: String, participants: List<Friend>) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).collection("events")
                .document(eventId)
                .collection("expenses")
                .get()
                .addOnSuccessListener { documents ->
                    val expenses = documents.mapNotNull { it.toObject(Expense::class.java) }
                    val event = intent.getParcelableExtra<Event>("event")
                    if (event != null) {
                        val totalExpenses = expenses.mapNotNull { it.amount }.sum()
                        val equalShare = totalExpenses / participants.size

                        val expensesByFriend = mutableMapOf<String, Double>()
                        val balances = mutableListOf<Balance>()

                        for (friend in participants) {
                            val expensesForFriend = expenses.filter { it.paidBy == friend.email }
                            val amountPaid = expensesForFriend.sumOf { it.amount ?: 0.0 }
                            expensesByFriend[friend.email ?: ""] = amountPaid
                        }

                        val debts = expensesByFriend.mapValues { equalShare - it.value }

                        var remainingDebts = debts.toMutableMap()

                        while (remainingDebts.isNotEmpty()) {
                            val maxDebt = remainingDebts.maxByOrNull { it.value }
                            val minDebt = remainingDebts.minByOrNull { it.value }

                            if (maxDebt != null && minDebt != null) {
                                val transferAmount = min(maxDebt.value, -minDebt.value)
                                val friend1 = participants.first { it.email == maxDebt.key }
                                val friend2 = participants.first { it.email == minDebt.key }

                                balances.add(Balance(friend1, transferAmount, friend2))

                                val maxDebtNewValue = maxDebt.value - transferAmount
                                val minDebtNewValue = minDebt.value + transferAmount

                                if (maxDebtNewValue <= 0.01) {
                                    remainingDebts.remove(maxDebt.key)
                                } else {
                                    remainingDebts[maxDebt.key] = maxDebtNewValue
                                }

                                if (minDebtNewValue >= -0.01) {
                                    remainingDebts.remove(minDebt.key)
                                } else {
                                    remainingDebts[minDebt.key] = minDebtNewValue
                                }
                            }
                        }

                        saveBalancesToFirestore(eventId, balances)
                        balancesAdapter.updateBalances(balances)

                        summaryTextView.text = String.format(
                            getString(R.string.summary_format_zloty),
                            totalExpenses,
                            equalShare
                        )
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        getString(R.string.error_fetching_expenses, e.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun saveBalancesToFirestore(eventId: String, balances: List<Balance>) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val balancesCollection = firestore.collection("users").document(currentUser.uid).collection("events")
                .document(eventId).collection("balances")

            // Batch write to save all balances in one transaction
            firestore.runBatch { batch ->
                for (balance in balances) {
                    val balanceDoc = balancesCollection.document() // Create a new document with a generated ID
                    batch.set(balanceDoc, balance) // Save the balance to the new document
                }
            }.addOnSuccessListener {
                Log.d("DEBUG", "Balances saved successfully")
            }.addOnFailureListener { e ->
                Log.e("DEBUG", "Error saving balances: ${e.message}")
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun fetchFriends(callback: (List<Friend>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    val friends = documents.mapNotNull { it.toObject(Friend::class.java) }
                    callback(friends)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        getString(R.string.error_fetching_friends, e.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}