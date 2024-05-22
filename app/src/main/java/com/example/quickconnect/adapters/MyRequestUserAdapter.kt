package com.example.quickconnect.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.R
import com.example.quickconnect.databinding.NotificationItemLayoutBinding
import com.example.quickconnect.model.notificationmodel.MyRequestDataList
import com.example.quickconnect.model.notificationmodel.RequestUserData
import com.example.quickconnect.model.notificationmodel.SocialRequestsData
import com.squareup.picasso.Picasso

class MyRequestUserAdapter(var requestUserList: MutableList<MyRequestDataList>, val context: Context) :
    RecyclerView.Adapter<MyRequestUserAdapter.RequestPermissionViewHolder>() {

    lateinit var onItemClick: ((MyRequestDataList) -> Unit)
    lateinit var innerAdapterForRequests: InnerAdapterForRequests
    var innerList: MutableList<SocialRequestsData> = mutableListOf()

//    lateinit var onItemClickListener: OnItemClickListener
    lateinit var onItemClickListenerForReject: OnItemClickListenerForReject
    lateinit var onAcceptClickListener: OnAcceptClickListener


    inner class RequestPermissionViewHolder(val binding: NotificationItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int): MyRequestUserAdapter.RequestPermissionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NotificationItemLayoutBinding.inflate(inflater, parent, false)
        return RequestPermissionViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MyRequestUserAdapter.RequestPermissionViewHolder,
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
//
//            val isExpanded = listOfUser.id == expandedItemId
//            holder.binding.innerRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
//
//            holder.itemView.setOnClickListener {
//                if (mainItem.mainId == expandedItemId) {
//                    expandedItemId = null
//                } else {
//                    expandedItemId = mainItem.mainId
//                }
//
//                notifyDataSetChanged()
//                onItemClick(mainItem.mainId, !isExpanded)
//            }

            if (requestUserList.isNotEmpty()){
                mainAcceptIv.isVisible = false
                mainRejectIv.isVisible = false
            }

        }
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
}