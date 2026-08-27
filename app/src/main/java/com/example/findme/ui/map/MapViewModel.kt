package com.example.findme.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findme.data.model.AppUser
import com.example.findme.data.repository.UserRepository
import com.example.findme.util.Resource
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val userRepo = UserRepository()

    private val _mapUsersState = MutableLiveData<Resource<List<AppUser>>>()
    val mapUsersState: LiveData<Resource<List<AppUser>>> = _mapUsersState

    fun loadAllUsers() {
        viewModelScope.launch {
            _mapUsersState.value = Resource.Loading
            _mapUsersState.value = userRepo.getAllUsers()
        }
    }
}