package com.example.quickconnect.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.R
import com.example.quickconnect.databinding.NotificationItemLayoutBinding
import com.example.quickconnect.model.notificationmodel.MyRequestDataList
import com.example.quickconnect.model.notificationmodel.RequestUserData
import com.example.quickconnect.model.notificationmodel.SocialRequestsData
import com.squareup.picasso.Picasso

class RequestPermissionAdapter(var requestUserList: MutableList<MyRequestDataList>, val context: Context) :
    RecyclerView.Adapter<RequestPermissionAdapter.RequestPermissionViewHolder>() {

    lateinit var onItemClick: ((MyRequestDataList) -> Unit)
    lateinit var innerAdapterForRequests: InnerAdapterForRequests
    var innerList: MutableList<SocialRequestsData> = mutableListOf()

//    lateinit var onItemClickListener: OnItemClickListener
    lateinit var onItemClickListenerForReject: OnItemClickListenerForReject
    lateinit var onAcceptClickListener: OnAcceptClickListener

    val sharedPreferences = context.getSharedPreferences("UserData" , Context.MODE_PRIVATE)
   val userToken = sharedPreferences.getString("userToken","").toString()

    private var expandedItemId: Int? = null
    private lateinit var innerAdapter: InnerAdapterForRequests

    lateinit var Listofsocialequest : List<SocialRequestsData>


    private val apiServices = RetrofitClient.client.create(Api::class.java)



    inner class RequestPermissionViewHolder(val binding: NotificationItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int): RequestPermissionAdapter.RequestPermissionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NotificationItemLayoutBinding.inflate(inflater, parent, false)
        return RequestPermissionViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RequestPermissionAdapter.RequestPermissionViewHolder,
        position: Int
    ) {
        val listOfUser = requestUserList[position]

        holder.binding.apply {
            notificationRqName.text = listOfUser.fullName

            val imageUrl = "${listOfUser.profilePic}"


            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.profile_placeholder) // Placeholder image resource
                .error(R.drawable.profile_placeholder) // Error image resource
                .into(holder.binding.notificationIv)


            holder.itemView.setOnClickListener {
                onItemClick.invoke(listOfUser)
            }

            mainAcceptIv.setOnClickListener {
                onAcceptClickListener?.onAcceptAllClick(listOfUser, position)
            }

            if (requestUserList.isNotEmpty()){
                mainAcceptIv.isVisible = false
                mainRejectIv.isVisible = false
            }
        }

//
//        val isExpanded = listOfUser.id == expandedItemId
//        holder.binding.innerRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
//
//        holder.itemView.setOnClickListener {
//            if (listOfUser.id == expandedItemId) {
//                expandedItemId = null
//            } else {
//                expandedItemId = listOfUser.id
//            }
//
//            notifyDataSetChanged()
//            onItemClick(listOfUser, listOfUser.id!!, !isExpanded)
//        }
//
//        holder.binding.innerRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
//        holder.binding.innerRecyclerView.adapter = innerAdapter
//        innerAdapter.setInnerItems(mySocialRequesteList(userToken,MyRequestFragment.userName))
//
//        // Update the visibility of the innerRecyclerView
//        holder.binding.innerRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return requestUserList.size
    }

    interface OnItemClickListener {
        fun onClickForAccept(item: RequestUserData, position: Int)
    }

    interface OnItemClickListenerForReject {
        fun onClickForReject(item: RequestUserData, position: Int)
    }

    fun rejectAccess(position: Int) {
        if (position >= 0 && position < innerList.size) {
            innerList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    interface OnAcceptClickListener {
        fun onAcceptAllClick(item: MyRequestDataList, position: Int)
    }

//    fun mySocialRequesteList(Authorization : String, username : String): List<SocialRequestsData> {
//
//        try {
//
//            val call = apiServices.mySocialRequestList("Token $Authorization",username)
//            call.enqueue(object : Callback<ListOfSocialRequestsData> {
//                override fun onResponse(
//                    call: Call<ListOfSocialRequestsData>,
//                    response: Response<ListOfSocialRequestsData>
//                ) {
//                    response.body()?.let { listofsocialequest ->
////                        listOfSocialRequestLiveData.postValue(Listofsocialequest.data)
//                        Log.e("TAG", "successCallActionList:$Listofsocialequest", )
//
//                        Listofsocialequest = response.body()!!.data
//
//
//                    }
//                }
//
//                override fun onFailure(call: Call<ListOfSocialRequestsData>, t: Throwable) {
////                    Log.e("TAG", "onFailurelistOfsocialRequestData: $listOfSocialRequestLiveData", )
//                }
//            })
//        }catch (e: Exception){
//            e.printStackTrace()
//        }
//        return Listofsocialequest
//    }
//
//    // Close other expanded items
//    fun closeOtherItems(currentItemId: Int) {
//        if (expandedItemId != null && expandedItemId != currentItemId) {
//            expandedItemId = null
//            notifyDataSetChanged()
//        }
//    }
}