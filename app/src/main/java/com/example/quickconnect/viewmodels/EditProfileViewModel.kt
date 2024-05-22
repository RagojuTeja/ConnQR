package com.example.quickconnect.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.model.usermodel.EditProfileData
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileViewModel : ViewModel() {

    private var editPrrofileLiveData = MutableLiveData<EditProfileData>()
    private var photoUpdateLiveData = MutableLiveData<EditProfileData>()
    private val apiServices = RetrofitClient.client.create(Api::class.java)


    fun editProfile (Authorization : String, full_name : String,
                     email : String,
                     primary_phone_number : String,
                     secondary_phone_number : String,
                     work_at : String,
                     description : String) {
        viewModelScope.launch {
            try {

            val call = apiServices.editProfile(
                "Token $Authorization",
                full_name,
                email,
                primary_phone_number,
                secondary_phone_number,
                work_at,
                description
            )
            Log.e("TAG", "onResponse:$call")
            call.enqueue(object : Callback<EditProfileData> {
                override fun onResponse(
                    call: Call<EditProfileData>,
                    response: Response<EditProfileData>
                ) {
                    if (response.isSuccessful) {
                        editPrrofileLiveData.value = response.body()
                        Log.e("TAG", "onResponsesuccesEditProfile:${response.body()} ")
                    }
                }

                override fun onFailure(call: Call<EditProfileData>, t: Throwable) {
                    Log.e(ContentValues.TAG, "onFailure: retro failure $t",)

                }
            })
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun profileChange(Authorization : String, profile_pic : String){

        try {

        val call = apiServices.photoUpadate("Token $Authorization", profile_pic)
        call.enqueue(object  : Callback<EditProfileData>{
            override fun onResponse(
                call: Call<EditProfileData>,
                response: Response<EditProfileData>
            ) {
                if (response.isSuccessful){
                    photoUpdateLiveData.value = response.body()
                }
            }

            override fun onFailure(call: Call<EditProfileData>, t: Throwable) {
                Log.e("TAG", "onFailurephoto: $t", )
            }

        })

        }catch (e: Exception){
            e.printStackTrace()
        }

    }



    fun observeEditProfileData() : LiveData<EditProfileData> {
        return editPrrofileLiveData
    }

    fun observePhotoUpdate() : LiveData<EditProfileData>{
        return photoUpdateLiveData
    }

}