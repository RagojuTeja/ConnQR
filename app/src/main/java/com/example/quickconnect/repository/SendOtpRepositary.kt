package com.example.quickconnect.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quickconnect.viewmodels.SendOtpViewModel

class SendOtpRepositary : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SendOtpViewModel() as T
    }
}