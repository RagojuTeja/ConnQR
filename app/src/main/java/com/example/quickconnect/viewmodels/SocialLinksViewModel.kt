package com.example.quickconnect.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.model.sociallinksdata.CategoryData
import com.example.quickconnect.model.sociallinksdata.CreateSocialLinkData
import com.example.quickconnect.model.sociallinksdata.SearchData
import com.example.quickconnect.model.sociallinksdata.SocialLinkDeleteData
import com.example.quickconnect.model.sociallinksdata.SocialLinkListData
import com.example.quickconnect.model.sociallinksdata.SocialLinksUpdateData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SocialLinksViewModel : ViewModel() {

    private var socialListLiveData = MutableLiveData<MutableList<CategoryData>>()
    private var socialUpdateLiveData = MutableLiveData<SocialLinksUpdateData>()
    private var switchUpdateLiveData = MutableLiveData<SocialLinksUpdateData>()
    private var  createSocialLinkLiveData = MutableLiveData<CreateSocialLinkData>()
    private var socialLinksAllListLiveData = MutableLiveData<List<CategoryData>>()
    private var socialDeleteLinkLiveData = MutableLiveData<SocialLinkDeleteData>()
    private var searchLiveData = MutableLiveData<SearchData>()

    val apiServices = RetrofitClient.client.create(Api::class.java)




    fun socialListAll(Authorization : String){

        try {

        val call = apiServices.allSocialList("Token $Authorization")
        call.enqueue(object : Callback<SocialLinkListData>{
            override fun onResponse(call: Call<SocialLinkListData>, response: Response<SocialLinkListData>
            ) {
                if (response.isSuccessful){
                    Log.e("TAG", "onResponse: ${response.body()}", )

                    response.body()!!.let { socialListAllList ->
                        socialLinksAllListLiveData.postValue(socialListAllList.data)
                    }
                }
            }

            override fun onFailure(call: Call<SocialLinkListData>, t: Throwable) {
                Log.e("TAG", "onFailureCreate:$t")
            }

        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }



    fun createSocialLink(Authorization : String, name : String, category_id: Int, link: String, is_locked: Int){

        try {

        val call = apiServices.createSocialLink("Token $Authorization",name, category_id,link, is_locked)

        Log.e("TAG", "onResponseCreateLinkcall: $call", )

        call.enqueue(object : Callback<CreateSocialLinkData>{
                override fun onResponse(
                    call: Call<CreateSocialLinkData>,
                    response: Response<CreateSocialLinkData>
                ) {
                    if (response.isSuccessful){
                        Log.e("TAG", "onResponseCreateLink: ${response.body()}", )

                        createSocialLinkLiveData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<CreateSocialLinkData>, t: Throwable) {
                    Log.e("TAG", "socialListonFailure:$t ")
                }

            })
        }catch (e: Exception){
            e.printStackTrace()
        }

    }



    fun socialLinkList (Authorization : String) {

        try {

        val call = apiServices.socialList("Token $Authorization")

        call.enqueue(object : Callback<SocialLinkListData>{
            override fun onResponse(
                call: Call<SocialLinkListData>,
                response: Response<SocialLinkListData>
            ) {
                if (response.isSuccessful){
                    Log.e("TAG", "onResponse: ${response.body()}", )

                    response.body()!!.let { getList ->
                        socialListLiveData.postValue(getList.data)
                    }
//                    socialLiveData.value = response.body()
                }
            }

            override fun onFailure(call: Call<SocialLinkListData>, t: Throwable) {
                Log.e("TAG", "socialListonFailure:$t ")
            }

        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    fun socialLinkUpadte (Authorization : String, category_id : Int, link : String, is_locked : Int) {

        try {

        val call = apiServices.updateLinks("Token $Authorization",category_id,link,is_locked)

        call.enqueue(object : Callback<SocialLinksUpdateData>{
            override fun onResponse(
                call: Call<SocialLinksUpdateData>,
                response: Response<SocialLinksUpdateData>
            ) {
                if (response.isSuccessful){
                    Log.e("TAG", "onResponse: ${response.body()}", )

//                    response.body()!!.let { committeeList ->
//                        socialLiveData.postValue(committeeList.data)
//                    }
                    socialUpdateLiveData.value = response.body()
                }
            }

            override fun onFailure(call: Call<SocialLinksUpdateData>, t: Throwable) {
                Log.e("TAG", "socialListonFailure:$t ")
            }

        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun switchUpadte (Authorization : String, category_id : Int, is_locked : Int) {

        try {

        val call = apiServices.switchUpdate("Token $Authorization",category_id,is_locked)

        call.enqueue(object : Callback<SocialLinksUpdateData>{
            override fun onResponse(
                call: Call<SocialLinksUpdateData>,
                response: Response<SocialLinksUpdateData>
            ) {
                if (response.isSuccessful){
                    Log.e("TAG", "switchonResponse: ${response.body()}", )

//                    response.body()!!.let { committeeList ->
//                        socialLiveData.postValue(committeeList.data)
//                    }
                    switchUpdateLiveData.value = response.body()
                }
            }

            override fun onFailure(call: Call<SocialLinksUpdateData>, t: Throwable) {
                Log.e("TAG", "switchFailure:$t ")
            }

        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun socialLinkDelete(Authorization : String, Id : Int){

        try {

        val call = apiServices.deleteSocialUrl("Token $Authorization", Id)
        call.enqueue(object : Callback<SocialLinkDeleteData>{
            override fun onResponse(
                call: Call<SocialLinkDeleteData>,
                response: Response<SocialLinkDeleteData>
            ) {
                if (response.isSuccessful){
                    socialDeleteLinkLiveData.value = response.body()
                    Log.e("TAG", "onResponsedelete:${response.body()} ")
                }
            }

            override fun onFailure(call: Call<SocialLinkDeleteData>, t: Throwable) {
                Log.e("TAG", "onFailureDeleteLink: $t")
            }
        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun searchData(Authorization : String, category_name : String){
        try {

        val call = apiServices.search("Token $Authorization", category_name)
        call.enqueue(object : Callback<SearchData>{
            override fun onResponse(
                call: Call<SearchData>,
                response: Response<SearchData>
            ) {
                if (response.isSuccessful){
                    searchLiveData.value = response.body()
                    Log.e("TAG", "onResponsedelete:${response.body()} ")
                }
            }

            override fun onFailure(call: Call<SearchData>, t: Throwable) {
                Log.e("TAG", "onFailureDeleteLink: $t")
            }
        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }




//    Delete Social Link
//    i.e. Only link Url will be deleted

    fun observbleCreateSocialLink() : LiveData<CreateSocialLinkData> {
        return createSocialLinkLiveData
    }

    fun observbleSocialLinksAll() : LiveData<List<CategoryData>>{
        return socialLinksAllListLiveData
    }

    fun observbleSocialLinkList() : LiveData<MutableList<CategoryData>> {
        return socialListLiveData
    }

    fun observbleSocialLinkUpdate() : LiveData<SocialLinksUpdateData> {
        return socialUpdateLiveData
    }

    fun observbleSwitchUpdate() : LiveData<SocialLinksUpdateData> {
        return switchUpdateLiveData
    }

    fun observableSocialLinkDelete() : LiveData<SocialLinkDeleteData>{
        return socialDeleteLinkLiveData
    }

    fun observableSearchData() : LiveData<SearchData>{
        return searchLiveData
    }


}