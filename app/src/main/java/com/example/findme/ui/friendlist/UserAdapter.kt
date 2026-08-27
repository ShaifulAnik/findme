package com.example.findme.ui.friendlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.findme.data.model.AppUser
import com.example.findme.databinding.ItemUserBinding

class UserAdapter(
    private var users: List<AppUser>,
    private val onItemClick: (AppUser) -> Unit // Click callback
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.tvDisplayName.text = user.displayName ?: "Default Name"
        holder.binding.tvEmail.text = user.userEmail

        // ইউজার কার্ডে ক্লিক করলে কাজ করবে
        holder.itemView.setOnClickListener {
            onItemClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<AppUser>) {
        users = newUsers
        notifyDataSetChanged()
    }
}