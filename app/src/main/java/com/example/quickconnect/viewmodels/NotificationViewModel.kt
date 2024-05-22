package com.example.quickconnect.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.model.notificationmodel.ApprovalRequestData
import com.example.quickconnect.model.notificationmodel.ListOfSocialRequestsData
import com.example.quickconnect.model.notificationmodel.ListOfUserRequestData
import com.example.quickconnect.model.notificationmodel.MyRequestDataList
import com.example.quickconnect.model.notificationmodel.MyRequestUserListData
import com.example.quickconnect.model.notificationmodel.MyRequestedData
import com.example.quickconnect.model.notificationmodel.MyRequestedListData
import com.example.quickconnect.model.notificationmodel.OtherData
import com.example.quickconnect.model.notificationmodel.OtherRequestUserData
import com.example.quickconnect.model.notificationmodel.OtherRequestedListData
import com.example.quickconnect.model.notificationmodel.RequestUserData
import com.example.quickconnect.model.notificationmodel.SendRequestData
import com.example.quickconnect.model.notificationmodel.SocialLinkRequested
import com.example.quickconnect.model.notificationmodel.SocialRequestsData
import com.example.quickconnect.model.notificationmodel.SocialRequestsOthersData
import com.example.quickconnect.model.usermodel.QrData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationViewModel : ViewModel() {

    private var notificationLiveData = MutableLiveData<SendRequestData>()
    private var approvalLiveData = MutableLiveData<ApprovalRequestData>()

    private var myRequestUserListLiveData = MutableLiveData<MutableList<MyRequestDataList>>()
    private var otherRequestUserListLiveData = MutableLiveData<MutableList<OtherData>>()
    private var myRequestedListLiveData = MutableLiveData<MutableList<MyRequestedData>>()




    private var listOfUserRequestLiveData = MutableLiveData<MutableList<RequestUserData>>()
    private var listOfSocialRequestLiveData = MutableLiveData<MutableList<SocialRequestsData>>()
    private var listOfSocialRequestOtherLiveData = MutableLiveData<MutableList<SocialRequestsData>>()


    private val apiServices = RetrofitClient.client.create(Api::class.java)

    fun sendRequest (Authorization : String,social_link_id : Int ) {

        try {

        val call = apiServices.sendRequest("Token $Authorization",social_link_id)
        Log.e("TAG", "onResponse:$call")
        call.enqueue(object : Callback<SendRequestData> {
            override fun onResponse(call: Call<SendRequestData>, response: Response<SendRequestData>) {
                if (response.isSuccessful){
                    notificationLiveData.value = response.body()
                    Log.e("TAG", "onResponsesucces_sendrequest:${response.body()} ")
                }
            }

            override fun onFailure(call: Call<SendRequestData>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: retro failure $t", )

            }
        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun permissionStatus(Authorization : String, request_id : Int, action : String){

        try {

        val call = apiServices.approvalRequest("Token $Authorization",request_id, action)

        call.enqueue(object : Callback<ApprovalRequestData>{
            override fun onResponse(
                call: Call<ApprovalRequestData>,
                response: Response<ApprovalRequestData>
            ) {
                if (response.isSuccessful){
                    Log.e("TAG", "onResponsesucces_permissionStatus:${response.body()} ")
                    approvalLiveData.value = response.body()
                }
            }

            override fun onFailure(call: Call<ApprovalRequestData>, t: Throwable) {
                Log.e("TAG", "onFailurePermissionStatus: $t", )
            }

        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }




//    fun listOfUserRequest(Authorization : String){
//
//        val call = apiServices.listOfUserRequest("Token $Authorization")
//        call.enqueue(object : Callback<ListOfUserRequestData>{
//            override fun onResponse(
//                call: Call<ListOfUserRequestData>,
//                response: Response<ListOfUserRequestData>
//            ) {
//                response.body()?.let { Listofuserequest ->
//                    listOfUserRequestLiveData.postValue(Listofuserequest.data)
//                    Log.e("TAG", "successCallActionList:$Listofuserequest", )
//
//                }
//            }
//
//            override fun onFailure(call: Call<ListOfUserRequestData>, t: Throwable) {
//                Log.e("TAG", "onFailurelistOfUserRequestLiveData: $listOfUserRequestLiveData", )
//            }
//
//        })
//
//    }


//    fun listSocialRequest(Authorization : String, username : String){
//
//        val call = apiServices.listOfSocialRequest("Token $Authorization", username)
//        call.enqueue(object : Callback<ListOfSocialRequestsData>{
//            override fun onResponse(
//                call: Call<ListOfSocialRequestsData>,
//                response: Response<ListOfSocialRequestsData>
//            ) {
//                response.body()?.let { Listofsocialequest ->
//                    listOfSocialRequestLiveData.postValue(Listofsocialequest.data)
//                    Log.e("TAG", "successCallActionList:$Listofsocialequest", )
//
//                }
//            }
//
//            override fun onFailure(call: Call<ListOfSocialRequestsData>, t: Throwable) {
//                Log.e("TAG", "onFailurelistOfsocialRequestData: $listOfSocialRequestLiveData", )
//            }
//
//        })
//    }

    fun myUserRequestList(Authorization : String){
        try {

        val call = apiServices.myRequestUserList("Token $Authorization")
        call.enqueue(object : Callback<MyRequestUserListData>{
            override fun onResponse(
                call: Call<MyRequestUserListData>,
                response: Response<MyRequestUserListData>
            ) {
                response.body()?.let { Listofsocialequest ->
                    myRequestUserListLiveData.postValue(Listofsocialequest.data)
                    Log.e("TAG", "successMyList:$Listofsocialequest", )

                }
            }

            override fun onFailure(call: Call<MyRequestUserListData>, t: Throwable) {
                Log.e("TAG", "onFailurelistOfsocialRequestData: $listOfSocialRequestLiveData", )
            }

        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun mySocialRequesteList(Authorization : String, username : String){

        try {

        val call = apiServices.mySocialRequestList("Token $Authorization",username)
        call.enqueue(object : Callback<ListOfSocialRequestsData>{
            override fun onResponse(
                call: Call<ListOfSocialRequestsData>,
                response: Response<ListOfSocialRequestsData>
            ) {
                response.body()?.let { Listofsocialequest ->
                    listOfSocialRequestLiveData.postValue(Listofsocialequest.data)
                    Log.e("TAG", "successCallActionList:$Listofsocialequest", )

                }
            }

            override fun onFailure(call: Call<ListOfSocialRequestsData>, t: Throwable) {
                Log.e("TAG", "onFailurelistOfsocialRequestData: $listOfSocialRequestLiveData", )
            }

        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    fun otherUserRequestList(Authorization : String){

        try {

        val call = apiServices.otherRequestUserList("Token $Authorization")
        call.enqueue(object : Callback<OtherRequestUserData>{
            override fun onResponse(
                call: Call<OtherRequestUserData>,
                response: Response<OtherRequestUserData>
            ) {
                response.body()?.let { Listofsocialequest ->
                    otherRequestUserListLiveData.postValue(Listofsocialequest.data)
                    Log.e("TAG", "successMyList:$Listofsocialequest", )

                }
            }

            override fun onFailure(call: Call<OtherRequestUserData>, t: Throwable) {
                Log.e("TAG", "onFailurelistOfsocialRequestData: $listOfSocialRequestLiveData", )
            }

        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun otherSocialRequesteList(Authorization : String, username : String){

        try {

        val call = apiServices.otherRequestSocialList("Token $Authorization",username)
        call.enqueue(object : Callback<ListOfSocialRequestsData>{
            override fun onResponse(
                call: Call<ListOfSocialRequestsData>,
                response: Response<ListOfSocialRequestsData>
            ) {
                response.body()?.let { Listofsocialequest ->
                    listOfSocialRequestOtherLiveData.postValue(Listofsocialequest.data)
                    Log.e("TAG", "successCallActionList:$Listofsocialequest", )

                }
            }

            override fun onFailure(call: Call<ListOfSocialRequestsData>, t: Throwable) {
                Log.e("TAG", "onFailurelistOfsocialRequestData: $listOfSocialRequestLiveData", )
            }

        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }





    fun observeSendRequest() : LiveData<SendRequestData> {
        return notificationLiveData
    }

    fun observeApprovalStatus() : LiveData<ApprovalRequestData>{
        return approvalLiveData
    }

    fun observeMyRequstData() : LiveData<MutableList<MyRequestDataList>>{
        return myRequestUserListLiveData
    }

    fun observeOtherRequstUserData() : LiveData<MutableList<OtherData>>{
        return otherRequestUserListLiveData
    }

    fun observeSocialRequestList() : LiveData<MutableList<SocialRequestsData>>{
        return listOfSocialRequestLiveData
    }

    fun observeOtherSocialRequestList() : LiveData<MutableList<SocialRequestsData>>{
        return listOfSocialRequestOtherLiveData
    }



}