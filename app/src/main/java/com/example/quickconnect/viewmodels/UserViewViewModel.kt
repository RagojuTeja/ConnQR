package com.example.quickconnect.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.model.viewersmodel.UserViewersData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewViewModel : ViewModel() {

    private var viewersLiveData = MutableLiveData<UserViewersData>()

    val apiServices = RetrofitClient.client.create(Api::class.java)


    fun UserView(Authorization : String, username : String){

        try {

        val call = apiServices.userViewLinks("Token $Authorization", username)
        call.enqueue(object : Callback<UserViewersData>{
            override fun onResponse(
                call: Call<UserViewersData>,
                response: Response<UserViewersData>
            ) {
                if (response.isSuccessful){
                    viewersLiveData.value = response.body()
                    Log.e("TAG", "onResponseuserview: ${response.body()}")
                }
            }

            override fun onFailure(call: Call<UserViewersData>, t: Throwable) {
                Log.e("TAG", "onFailure: $t", )
            }

        })

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun observableUserViewData() : LiveData<UserViewersData>{
        return viewersLiveData
    }

}