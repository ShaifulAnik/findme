package com.example.findme.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.findme.databinding.ActivitySignUpBinding
import com.example.findme.ui.friendlist.FriendListActivity
import com.example.findme.util.Resource

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        binding.btnSubmitSignUp.setOnClickListener {
            val name = binding.etSignUpName.text.toString().trim()
            val email = binding.etSignUpEmail.text.toString().trim()
            val pass = binding.etSignUpPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.register(email, pass, name)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBarSignUp.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBarSignUp.visibility = View.GONE
                    val intent = Intent(this, FriendListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBarSignUp.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}