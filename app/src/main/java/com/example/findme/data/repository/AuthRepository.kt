package com.example.findme.data.repository

import com.example.findme.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun login(email: String, pass: String): Resource<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            result.user?.let { Resource.Success(it) } ?: Resource.Error("User null")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Authentication Failed")
        }
    }

    // register ফাংশনে displayName যোগ করা হয়েছে
    suspend fun register(email: String, pass: String, displayName: String): Resource<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            // Firebase Auth এ Display Name সেট করে দেওয়া
            user?.let {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                it.updateProfile(profileUpdates).await()
                Resource.Success(it)
            } ?: Resource.Error("Registration Failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration Failed")
        }
    }

    fun logout() {
        auth.signOut()
    }
}