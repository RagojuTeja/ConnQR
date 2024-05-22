package com.example.quickconnect.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.databinding.CalltoActionListBinding
import com.example.quickconnect.model.CallToActionModel.ActionList
import com.example.quickconnect.model.CallToActionModel.ActionsDeleteData
import com.example.quickconnect.model.CallToActionModel.CallListCategorieData
import com.example.quickconnect.model.CallToActionModel.CallListTypeCategorieData
import com.example.quickconnect.model.CallToActionModel.CallToActionListData
import com.example.quickconnect.model.CallToActionModel.CreateActionData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallToActionViewModel : ViewModel() {

    private var callTypeActionListLiveData = MutableLiveData<List<CallListCategorieData>>()
    private var callActionListLiveData = MutableLiveData<MutableList<ActionList>>()
    private var createActionLiveData = MutableLiveData<CreateActionData>()
    private var deleteActionLiveData = MutableLiveData<ActionsDeleteData>()
    val apiServices = RetrofitClient.client.create(Api::class.java)


    fun typeCategorieList(){
        try {

            val call = apiServices.getActionTypeList()
            call.enqueue(object : Callback<CallListTypeCategorieData> {
                override fun onResponse(
                    call: Call<CallListTypeCategorieData>,
                    response: Response<CallListTypeCategorieData>
                ) {
                    response.body()!!.let { ActionTypeList ->
                        callTypeActionListLiveData.postValue(ActionTypeList.data)
                        Log.e("TAG", "successCallActionTypeList:$ActionTypeList",)

                    }
                }

                override fun onFailure(call: Call<CallListTypeCategorieData>, t: Throwable) {
                    Log.e("TAG", "failCallActionTypeList:$t",)

                }
            })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    fun createAction(Authorization : String, name : String, link : String, category_id : Int){
        try {

        val call = apiServices.createAction("Token $Authorization",name,link,category_id)
        call.enqueue(object : Callback<CreateActionData>{
            override fun onResponse(
                call: Call<CreateActionData>,
                response: Response<CreateActionData>
            ) {
                if (response.isSuccessful){
                    Log.e("TAG", "createActionSucess: ${response.body()}", )
                    createActionLiveData.value = response.body()
                }
            }

            override fun onFailure(call: Call<CreateActionData>, t: Throwable) {
                Log.e("TAG", "onFailureCreateAction: $t", )
            }

        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun callToActionList(Authorization : String){
        try {

        val call = apiServices.getActionList("Token $Authorization")
        call.enqueue(object : Callback<CallToActionListData>{
            override fun onResponse(
                call: Call<CallToActionListData>, response: Response<CallToActionListData>
            ) {
                response.body()?.let { callToActionList ->
                    callActionListLiveData.postValue(callToActionList.data)
                    Log.e("TAG", "successCallActionList:$callToActionList", )

                }
            }

            override fun onFailure(call: Call<CallToActionListData>, t: Throwable) {
                Log.e("TAG", "onFailureCallActionList: $t", )
            }
        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun deleteAction(Authorization : String, id : Int){
        try {

        val call = apiServices.actionsDelete("Token $Authorization",id)
        call.enqueue(object : Callback<ActionsDeleteData>{
            override fun onResponse(
                call: Call<ActionsDeleteData>,
                response: Response<ActionsDeleteData>
            ) {
                if (response.isSuccessful){
                    deleteActionLiveData.value = response.body()
                }
            }

            override fun onFailure(call: Call<ActionsDeleteData>, t: Throwable) {
                Log.e("TAG", "onFailuredelete: $t")
            }

        })
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun observbleCreateAction() : LiveData<CreateActionData>{
        return createActionLiveData
    }

    fun observbleCallActionTypeList() : LiveData<List<CallListCategorieData>>{
        return callTypeActionListLiveData
    }

    fun observbleCallActionList() : LiveData<MutableList<ActionList>>{
        return callActionListLiveData
    }

    fun observbleDeleteAction() : LiveData<ActionsDeleteData>{
        return deleteActionLiveData
    }

}