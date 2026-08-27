package com.example.findme.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.findme.databinding.ActivityMyProfileBinding
import com.example.findme.util.Resource

class MyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        binding.btnUpdateName.setOnClickListener {
            val name = binding.etProfileName.text.toString().trim()
            if (name.isNotEmpty()) {
                viewModel.updateName(name)
            }
        }

        viewModel.loadProfile()
    }

    private fun setupObservers() {
        viewModel.profileState.observe(this) { resource ->
            if (resource is Resource.Success) {
                val user = resource.data
                binding.etProfileName.setText(user.displayName ?: "")
                binding.tvProfileEmail.text = "Email: ${user.userEmail}"
                binding.tvProfileLat.text = "Latitude: ${user.latitude}"
                binding.tvProfileLng.text = "Longitude: ${user.longitude}"
            }
        }

        viewModel.updateState.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> Toast.makeText(this, "Updated successfully!", Toast.LENGTH_SHORT).show()
                is Resource.Error -> Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }
}