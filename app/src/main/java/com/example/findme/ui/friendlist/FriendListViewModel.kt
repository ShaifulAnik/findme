package com.example.findme.ui.friendlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findme.data.model.AppUser
import com.example.findme.data.repository.AuthRepository
import com.example.findme.data.repository.UserRepository
import com.example.findme.util.Resource
import kotlinx.coroutines.launch

class FriendListViewModel : ViewModel() {

    private val userRepo = UserRepository()
    private val authRepo = AuthRepository()

    private val _usersState = MutableLiveData<Resource<List<AppUser>>>()
    val usersState: LiveData<Resource<List<AppUser>>> = _usersState

    fun loadUsers() {
        viewModelScope.launch {
            _usersState.value = Resource.Loading
            _usersState.value = userRepo.getAllUsers()
        }
    }

    fun logout() {
        authRepo.logout()
    }
}