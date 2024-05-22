package com.example.quickconnect.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.model.usermodel.ChangePasswordData
import com.example.quickconnect.model.usermodel.LogoutData
import com.example.quickconnect.model.usermodel.SignUpData
import com.example.quickconnect.model.usermodel.UserDeleteData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private var loginLiveData = MutableLiveData<SignUpData>()
    private var logoutLiveData = MutableLiveData<LogoutData>()
    private var changePasswordLiveData = MutableLiveData<ChangePasswordData>()
    private var userDeleteLiveData = MutableLiveData<UserDeleteData>()
    private val apiServices = RetrofitClient.client.create(Api::class.java)


    fun Login(login_type : String, mobile : String, password : String){

        try {

        val call = apiServices.Login(login_type, mobile, password)
        call.enqueue(object : Callback<SignUpData>{
            override fun onResponse(call: Call<SignUpData>, response: Response<SignUpData>) {
                if (response.isSuccessful){
                    loginLiveData.value = response.body()
                    Log.e(ContentValues.TAG, "loginSuccess ${response.body()}")
                }
            }

            override fun onFailure(call: Call<SignUpData>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: retro failure $t", )
            }

        })

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun logout(Authorization : String){

        try {

        val call = apiServices.logout("Token $Authorization")
        call.enqueue(object : Callback<LogoutData>{
            override fun onResponse(call: Call<LogoutData>, response: Response<LogoutData>) {
                if (response.isSuccessful){
                    logoutLiveData.value = response.body()
                    Log.e("TAG", "onResponse: ${response.body()}")
                }
            }

            override fun onFailure(call: Call<LogoutData>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: retro failure $t", )
            }

        })

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun changePassword(Authorization : String,old_password : String, new_password : String, confirm_password : String){

        try {

        val call = apiServices.changePassword("Token $Authorization",old_password,new_password,confirm_password)
        call.enqueue(object : Callback<ChangePasswordData>{
            override fun onResponse(call: Call<ChangePasswordData>, response: Response<ChangePasswordData>) {
                if (response.isSuccessful){
                    changePasswordLiveData.value = response.body()
                    Log.e("TAG", "onResponseChangePass: ${response.body()}")
                }
            }

            override fun onFailure(call: Call<ChangePasswordData>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailureChangePass: retro failure $t", )
            }
        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun userDelete(Authorization : String){
        try {

        val call = apiServices.userDelete("Token $Authorization")
        call.enqueue(object : Callback<UserDeleteData>{
            override fun onResponse(call: Call<UserDeleteData>, response: Response<UserDeleteData>) {
                if (response.isSuccessful){
                    userDeleteLiveData.value = response.body()
                    Log.e("TAG", "onResponseuserDelete: ${response.body()}")
                }
            }

            override fun onFailure(call: Call<UserDeleteData>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailureuserDelete: retro failure $t", )
            }
        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun observeLoginData() : LiveData<SignUpData>{
        return loginLiveData
    }

    fun observeLogoutData() : LiveData<LogoutData>{
        return logoutLiveData
    }

    fun observeChangePassData() : LiveData<ChangePasswordData>{
        return changePasswordLiveData
    }

    fun observeUserDelete() : LiveData<UserDeleteData>{
        return userDeleteLiveData
    }



}