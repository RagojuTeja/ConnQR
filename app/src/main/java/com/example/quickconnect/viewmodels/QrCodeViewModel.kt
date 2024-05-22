package com.example.quickconnect.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.model.usermodel.QrData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QrCodeViewModel : ViewModel() {


    private var qrCodeLiveData = MutableLiveData<QrData>()
    private val apiServices = RetrofitClient.client.create(Api::class.java)

    fun qrCode (Authorization : String) {

        try {

        val call = apiServices.getProfile("Token $Authorization")
        Log.e("TAG", "onResponse:$call")
        call.enqueue(object : Callback<QrData>{
            override fun onResponse(call: Call<QrData>, response: Response<QrData>) {
                if (response.isSuccessful){
                    qrCodeLiveData.value = response.body()
                    val errorCode: Int = response.code()

                    Log.e("TAG", "onResponsesucces_getprofiledata:${response.body()} ")
                    Log.e("TAG", "onResponsesucces_getprofiledata:$errorCode ")

                } else {
                    // Handle error response
                    val errorBody: String? = response.errorBody()?.string()
                    val errorCode: Int = response.code()

                    Log.e("TAG", "Error Code: $errorCode, Error Body: $errorBody")
                }
            }

            override fun onFailure(call: Call<QrData>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: retro failure $t", )

            }
        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    fun observeQrData() : LiveData<QrData>{
        return qrCodeLiveData
    }

}