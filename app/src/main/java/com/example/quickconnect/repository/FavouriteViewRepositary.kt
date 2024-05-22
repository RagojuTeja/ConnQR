package com.example.quickconnect.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quickconnect.viewmodels.FavouriteViewModel
import com.example.quickconnect.viewmodels.UserViewViewModel

class FavouriteViewRepositary : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavouriteViewModel() as T
    }
}