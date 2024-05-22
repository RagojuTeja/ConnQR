package com.example.quickconnect.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.model.usermodel.SignUpData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpViewModel : ViewModel() {

    private var signupLiveData = MutableLiveData<SignUpData>()
    private val apiServices = RetrofitClient.client.create(Api::class.java)


    fun signUp (mobile: String, username : String, password : String, conformpassword : String){

        try {

        val call = apiServices.SignUp(mobile, username, password, conformpassword)
        Log.e("TAG", "CreatePassword: $call", )
        call.enqueue(object : Callback<SignUpData> {
            override fun onResponse(call: Call<SignUpData>, response: Response<SignUpData>) {
                if (response.isSuccessful){
                    signupLiveData.value = response.body()
                    Log.e("TAG", "CreatePassword: ${response.body()}")

                }
            }

            override fun onFailure(call: Call<SignUpData>, t: Throwable) {
                Log.e(ContentValues.TAG, "CreatePasswordonFailure: retro failure $t", )
            }

        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun observeSignUpData() : LiveData<SignUpData> {
        return signupLiveData
    }
}