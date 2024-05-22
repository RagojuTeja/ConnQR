package com.example.quickconnect.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.model.favouritesmodel.AddFavouriteData
import com.example.quickconnect.model.favouritesmodel.FavouriteData
import com.example.quickconnect.model.favouritesmodel.FavouriteListData
import com.example.quickconnect.model.favouritesmodel.RemoveFavouriteData
import com.example.quickconnect.model.favouritesmodel.ViewersData
import com.example.quickconnect.model.favouritesmodel.ViewersList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavouriteViewModel : ViewModel() {

    private var addFavouriteLiveData = MutableLiveData<AddFavouriteData>()
    private var favouriteListLiveData = MutableLiveData<MutableList<FavouriteData>>()
    private var removeFavouriteLiveData = MutableLiveData<RemoveFavouriteData>()
    private var viewersListLiveData = MutableLiveData<MutableList<ViewersList>>()


    private val apiServices = RetrofitClient.client.create(Api::class.java)


    fun addFavourite (Authorization : String, username : String){
        try {

        val call = apiServices.addFavorites("Token $Authorization", username)

        call.enqueue(object : Callback<AddFavouriteData>{
            override fun onResponse(
                call: Call<AddFavouriteData>,
                response: Response<AddFavouriteData>
            ) {
                if (response.isSuccessful){
                    addFavouriteLiveData.value = response.body()
                }
            }

            override fun onFailure(call: Call<AddFavouriteData>, t: Throwable) {
                Log.e("TAG", "onFailureAddFavourite: $t", )
            }

        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    fun favouriteList(Authorization : String){
        try {

        val call = apiServices.getFavouriteList("Token $Authorization")

        Log.e("TAG", "onResponse: $call", )

        call.enqueue(object : Callback<FavouriteListData>{
            override fun onResponse(
                call: Call<FavouriteListData>,
                response: Response<FavouriteListData>
            ) {
                response.body().let { favouriteList ->
                    favouriteListLiveData.postValue(favouriteList?.data)

                }
            }

            override fun onFailure(call: Call<FavouriteListData>, t: Throwable) {
                Log.e("TAG", "onFailureFavouriteList: $t", )
            }

        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun removeFavourite(Authorization : String, user_id : Int){

        try {

        val call = apiServices.removeFavourite("Token $Authorization", user_id)

        call.enqueue(object  : Callback<RemoveFavouriteData>{
            override fun onResponse(
                call: Call<RemoveFavouriteData>,
                response: Response<RemoveFavouriteData>
            ) {
                if (response.isSuccessful){
                    removeFavouriteLiveData.value = response.body()
                    Log.e("TAG", "successremoveFavourite: ${response.body()}", )
                }
            }

            override fun onFailure(call: Call<RemoveFavouriteData>, t: Throwable) {
                Log.e("TAG", "onFailureRRemoveFavourite: $t", )
            }

        })
        }catch (e: Exception){
            e.printStackTrace()
        }
    }



    fun viewersList(Authorization : String){
        try {

            val call = apiServices.getViewersList("Token $Authorization")

            Log.e("TAG", "onResponse: $call", )

            call.enqueue(object : Callback<ViewersData>{
                override fun onResponse(
                    call: Call<ViewersData>,
                    response: Response<ViewersData>
                ) {
                    response.body().let { viewerList ->
                        Log.e("TAG", "viewerListSuccess: $viewerList", )
                        viewersListLiveData.postValue(viewerList?.data)

                    }
                }

                override fun onFailure(call: Call<ViewersData>, t: Throwable) {
                    Log.e("TAG", "onFailureviewerList: $t", )
                }

            })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun observeaddFavourite() : LiveData<AddFavouriteData>{
        return addFavouriteLiveData
    }

    fun observeFavouriteList() : LiveData<MutableList<FavouriteData>>{
        return favouriteListLiveData
    }

    fun observeRemoveFavourite() : LiveData<RemoveFavouriteData>{
        return removeFavouriteLiveData
    }

    fun observeViewersData() : LiveData<MutableList<ViewersList>>{
        return viewersListLiveData
    }

}