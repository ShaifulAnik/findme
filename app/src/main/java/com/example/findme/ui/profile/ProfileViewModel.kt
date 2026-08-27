package com.example.findme.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findme.data.model.AppUser
import com.example.findme.data.repository.AuthRepository
import com.example.findme.data.repository.UserRepository
import com.example.findme.util.Resource
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val userRepo = UserRepository()
    private val authRepo = AuthRepository()

    private val _profileState = MutableLiveData<Resource<AppUser>>()
    val profileState: LiveData<Resource<AppUser>> = _profileState

    private val _updateState = MutableLiveData<Resource<Unit>>()
    val updateState: LiveData<Resource<Unit>> = _updateState

    fun loadProfile() {
        val uid = authRepo.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _profileState.value = Resource.Loading
            _profileState.value = userRepo.getCurrentUser(uid)
        }
    }

    fun updateName(newName: String) {
        val uid = authRepo.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val res = userRepo.updateDisplayName(uid, newName)
            _updateState.value = res
            if (res is Resource.Success) loadProfile()
        }
    }
}