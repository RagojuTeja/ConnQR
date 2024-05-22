package com.example.quickconnect.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quickconnect.viewmodels.EditProfileViewModel

class EditProfileRepositary : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditProfileViewModel() as T
    }

}