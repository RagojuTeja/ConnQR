package com.example.quickconnect.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quickconnect.viewmodels.UserViewViewModel

class UserViewRepositary : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewViewModel() as T
    }
}