package com.example.myapplication
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Friend

class FriendsAdapter(private var friends: List<Friend>, private val selectable: Boolean) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    private val selectedFriends = mutableSetOf<Friend>()

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendNameTextView: TextView = itemView.findViewById(R.id.friend_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.friend_item, parent, false)
        return FriendViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentItem = friends[position]
        holder.friendNameTextView.text = "${currentItem.firstName} ${currentItem.lastName}"

        // Dodajemy obsługę zaznaczenia
        if (selectable) {
            holder.itemView.setOnClickListener {
                if (selectedFriends.contains(currentItem)) {
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                    selectedFriends.remove(currentItem)
                } else {
                    holder.itemView.setBackgroundColor(Color.LTGRAY)
                    selectedFriends.add(currentItem)
                }
            }

            // Ustawiamy kolor tła na podstawie stanu zaznaczenia
            if (selectedFriends.contains(currentItem)) {
                holder.itemView.setBackgroundColor(Color.LTGRAY)
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    fun updateFriends(friends: List<Friend>) {
        this.friends = friends
        notifyDataSetChanged()
    }

    fun getSelectedFriends(): List<Friend> {
        return selectedFriends.toList()
    }
}
