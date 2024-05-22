package com.example.quickconnect.viewmodels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.model.usermodel.SendOtpData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendOtpViewModel : ViewModel() {

    private var sendOtpLiveData = MutableLiveData<SendOtpData>()
    private val apiServices = RetrofitClient.client.create(Api::class.java)

    fun sendOtp (number: String, loginType : String){

        try {

        val call = apiServices.sendOTP(number, loginType)
        call.enqueue(object : Callback<SendOtpData>{
            override fun onResponse(call: Call<SendOtpData>, response: Response<SendOtpData>) {
                if (response.isSuccessful){
                    sendOtpLiveData.value = response.body()
                }
            }

            override fun onFailure(call: Call<SendOtpData>, t: Throwable) {
                Log.e(TAG, "onFailure: retro failure $t", )
            }

        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun observeSendOtpData() : LiveData<SendOtpData>{
        return sendOtpLiveData
    }
}