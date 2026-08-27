package com.example.findme.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.findme.data.model.AppUser
import com.example.findme.data.repository.AuthRepository
import com.example.findme.data.repository.LocationRepository
import com.example.findme.data.repository.UserRepository
import com.example.findme.util.Resource
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository()
    private val userRepo = UserRepository()
    private val locationRepo = LocationRepository(application)

    private val _authState = MutableLiveData<Resource<Unit>>()
    val authState: LiveData<Resource<Unit>> = _authState

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            when (val res = authRepo.login(email, pass)) {
                is Resource.Success -> {
                    handleUserLocationAndFirestore(res.data.uid, email, null)
                }
                is Resource.Error -> _authState.value = Resource.Error(res.message)
                else -> {}
            }
        }
    }

    // register ফাংশনে name প্যারামিটারটি এখানে যোগ করা হয়েছে
    fun register(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            when (val res = authRepo.register(email, pass, name)) {
                is Resource.Success -> {
                    handleUserLocationAndFirestore(res.data.uid, email, name)
                }
                is Resource.Error -> _authState.value = Resource.Error(res.message)
                else -> {}
            }
        }
    }

    private suspend fun handleUserLocationAndFirestore(userId: String, email: String, name: String?) {
        val location = locationRepo.getLastLocation()
        val lat = location?.latitude ?: 0.0
        val lng = location?.longitude ?: 0.0

        val appUser = AppUser(
            userId = userId,
            userEmail = email,
            displayName = name,
            latitude = lat,
            longitude = lng
        )

        when (val saveRes = userRepo.saveUser(appUser)) {
            is Resource.Success -> _authState.value = Resource.Success(Unit)
            is Resource.Error -> _authState.value = Resource.Error(saveRes.message)
            else -> {}
        }
    }
}