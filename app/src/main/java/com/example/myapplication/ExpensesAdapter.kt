package com.example.myapplication
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpensesAdapter(private var expenses: List<Expense>, private var friends: List<Friend>) : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expenseDescriptionTextView: TextView = itemView.findViewById(R.id.expense_description)
        val expenseAmountTextView: TextView = itemView.findViewById(R.id.expense_amount)
        val paidByTextView: TextView = itemView.findViewById(R.id.paid_by)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.expense_item, parent, false)
        return ExpenseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val currentItem = expenses[position]
        Log.d("ExpensesAdapter", "Binding expense: $currentItem")
        holder.expenseDescriptionTextView.text = currentItem.description ?: ""
        holder.expenseAmountTextView.text = currentItem.amount?.toString() ?: ""
        holder.paidByTextView.text = friends.firstOrNull { it.email == currentItem.paidBy }?.let {
            "${it.firstName} ${it.lastName}"
        } ?: currentItem.paidBy ?: "" // Use friend's name if available, otherwise use email
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    fun updateExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }

    fun updateFriends(newFriends: List<Friend>) {
        friends = newFriends
        notifyDataSetChanged()
    }
}

