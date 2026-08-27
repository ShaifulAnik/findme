package com.example.findme.data.repository

import com.example.findme.data.model.AppUser
import com.example.findme.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val usersCollection = firestore.collection("AppUsers")

    suspend fun saveUser(user: AppUser): Resource<Unit> {
        return try {
            usersCollection.document(user.userId).set(user).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save user")
        }
    }

    suspend fun updateDisplayName(userId: String, name: String): Resource<Unit> {
        return try {
            usersCollection.document(userId).update("displayName", name).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update display name")
        }
    }

    suspend fun getCurrentUser(userId: String): Resource<AppUser> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            val user = doc.toObject(AppUser::class.java)
            if (user != null) Resource.Success(user) else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch user")
        }
    }

    suspend fun getAllUsers(): Resource<List<AppUser>> {
        return try {
            val snapshot = usersCollection.get().await()
            val list = snapshot.toObjects(AppUser::class.java)
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch users")
        }
    }
}