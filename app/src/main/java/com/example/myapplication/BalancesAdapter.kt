package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BalancesAdapter(private var balances: List<Balance>) : RecyclerView.Adapter<BalancesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name1TextView: TextView = view.findViewById(R.id.name1)
        val owesTextView: TextView = view.findViewById(R.id.owes)
        val amountTextView: TextView = view.findViewById(R.id.amount)
        val name2TextView: TextView = view.findViewById(R.id.name2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.balances_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val balance = balances[position]

        holder.name1TextView.text = balance.friend1.firstName
        holder.owesTextView.text = holder.itemView.context.getString(R.string.owes)
        holder.amountTextView.text = "%.2f zł".format(balance.amount)
        holder.name2TextView.text = balance.friend2.firstName
    }

    override fun getItemCount(): Int {
        return balances.size
    }

    fun updateBalances(newBalances: List<Balance>) {
        balances = newBalances
        notifyDataSetChanged()
    }
}