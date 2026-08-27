package com.example.findme.ui.friendlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findme.data.model.AppUser
import com.example.findme.databinding.ActivityFriendListBinding
import com.example.findme.ui.auth.AuthActivity
import com.example.findme.ui.map.GoogleMapActivity
import com.example.findme.ui.profile.MyProfileActivity
import com.example.findme.util.Resource
import com.google.firebase.auth.FirebaseAuth

class FriendListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendListBinding
    private val viewModel: FriendListViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()

        // RecyclerView-এর নিচের "Show All Users on Map" বাটন
        binding.btnShowAllUsersMap.setOnClickListener {
            val intent = Intent(this, GoogleMapActivity::class.java).apply {
                putExtra("SHOW_SINGLE_USER", false) // সবার লোকেশন দেখাবে
            }
            startActivity(intent)
        }

        binding.fabMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }

        viewModel.loadUsers()
    }

    private fun setupRecyclerView() {
        // ফ্রেন্ড লিস্টের নির্দিষ্ট ইউজারে ক্লিক করলে শুধুমাত্র তার ম্যাপ দেখাবে
        adapter = UserAdapter(emptyList()) { selectedUser ->
            val intent = Intent(this, GoogleMapActivity::class.java).apply {
                putExtra("LATITUDE", selectedUser.latitude)
                putExtra("LONGITUDE", selectedUser.longitude)
                putExtra("USER_NAME", getUserDisplayName(selectedUser))
                putExtra("SHOW_SINGLE_USER", true)
            }
            startActivity(intent)
        }
        binding.rvFriends.layoutManager = LinearLayoutManager(this)
        binding.rvFriends.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.usersState.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    val allUsers = resource.data

                    val currentUser = allUsers.find { it.userId == currentUserId }
                    val email = currentUser?.userEmail ?: firebaseUser?.email ?: ""
                    val rawName = currentUser?.displayName ?: getUserNameFromEmail(email)
                    val formattedName = rawName.replaceFirstChar { it.uppercase() }

                    binding.tvHeaderGreeting.text = "Hello $formattedName"
                    binding.tvHeaderEmail.text = email
                    binding.tvHeaderLat.text = "Lat: ${currentUser?.latitude ?: 0.0}"
                    binding.tvHeaderLng.text = "Long: ${currentUser?.longitude ?: 0.0}"

                    // ১. উপরের হেডার কার্ডের ক্লিক ডিসেবল (কিছুই হবে না)
                    binding.cardCurrentUser.setOnClickListener(null)
                    binding.cardCurrentUser.isClickable = false

                    // ফ্রেন্ড লিস্টে বাকি ইউজারদের পাঠানো
                    val otherUsers = allUsers.filter { it.userId != currentUserId }
                    adapter.updateData(otherUsers)
                }
                is Resource.Error -> Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    private fun getUserNameFromEmail(email: String): String {
        return if (email.contains("@")) email.substringBefore("@") else "User"
    }

    private fun getUserDisplayName(user: AppUser): String {
        return user.displayName ?: getUserNameFromEmail(user.userEmail)
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add("My Profile")
        popup.menu.add("Show My Location") // ২. অপশন যোগ করা হলো
        popup.menu.add("Logout")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "My Profile" -> {
                    startActivity(Intent(this, MyProfileActivity::class.java))
                    true
                }
                "Show My Location" -> {
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    val currentUser = viewModel.usersState.value?.let { resource ->
                        if (resource is Resource.Success) resource.data.find { it.userId == currentUserId } else null
                    }

                    if (currentUser != null && currentUser.latitude != 0.0 && currentUser.longitude != 0.0) {
                        val rawName = currentUser.displayName ?: getUserNameFromEmail(currentUser.userEmail)
                        val formattedName = rawName.replaceFirstChar { it.uppercase() }

                        val intent = Intent(this, GoogleMapActivity::class.java).apply {
                            putExtra("LATITUDE", currentUser.latitude)
                            putExtra("LONGITUDE", currentUser.longitude)
                            putExtra("USER_NAME", "$formattedName (You)")
                            putExtra("SHOW_SINGLE_USER", true) // শুধু লগইন ইউজার দেখাবে
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Your location is not available yet", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                "Logout" -> {
                    viewModel.logout()
                    val intent = Intent(this, AuthActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}