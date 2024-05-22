package com.example.quickconnect.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.quickconnect.R
import com.example.quickconnect.activities.ProfilesActivity
import com.example.quickconnect.databinding.SocialRequestListItemBinding
import com.example.quickconnect.model.favouritesmodel.FavouriteData
import com.example.quickconnect.model.notificationmodel.ListOfSocialRequestsData
import com.example.quickconnect.model.notificationmodel.OtherData
import com.example.quickconnect.model.notificationmodel.SocialLink
import com.example.quickconnect.model.notificationmodel.SocialRequestsData
import com.example.quickconnect.model.notificationmodel.SocialRequestsOthersData
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class OtherInnerAdapterForRequests(var innerList: MutableList<SocialRequestsData>, val context: Context) :
    RecyclerView.Adapter<OtherInnerAdapterForRequests.InnerViewHolder>() {

    lateinit var onItemClickListener: OnItemClickListener
    lateinit var onItemClickListenerForReject: OnItemClickListenerForReject
    lateinit var onAcceptAllClickListener: OnAcceptAllClickListener

    lateinit var otherRequestPermissionAdapter: OtherRequestPermissionAdapter

    var mainList: MutableList<OtherData> = mutableListOf()




    inner class InnerViewHolder(val binding: SocialRequestListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        val binding = SocialRequestListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InnerViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        val innerData = innerList[position]

        holder.binding.apply {
            // Bind your inner data
            // innerImageView.setImageResource(innerData.image)
            // innerTextView.text = innerData.text

            val requestName = innerData.socialLink!!.name


            requestAccessNameTv.text = "Requested $requestName"


            val imageUrl = innerData.socialLink!!.category!!.icon



            try {

                val imageLoader = ImageLoader.Builder(context)
                    .components {
                        add(SvgDecoder.Factory())
                    }
                    .build()

                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .target(holder.binding.requestPersonIv)
                    .build()


                imageLoader.enqueue(request)

            } catch (e: NullPointerException) {
                e.printStackTrace()
            }



//            Picasso.get()
//                .load(imageUrl)
//                .placeholder(R.drawable.profile_placeholder) // Placeholder image resource
//                .error(R.drawable.profile_placeholder) // Error image resource
//                .into(holder.binding.requestPersonIv)

            otherRequestPermissionAdapter = OtherRequestPermissionAdapter(mainList,context)


            // Check if the request is already accepted
            if (innerData.isAccepted == true) {
                acceptIv.isVisible = false
                rejectIv.isVisible = false
                deleteIcon.isVisible = true
                requestAccessNameTv.setText("Accepted $requestName Request")
//                acceptIv.setBackgroundColor(androidx.appcompat.R.color.material_grey_50)
//                rejectIv.isEnabled = false
//                rejectIv.setBackgroundColor(androidx.appcompat.R.color.material_grey_50)
                // Update text or any other UI changes
                // For example, you can change the visibility or text of some TextView
                // textView.text = "Already Accepted"
//                notifyDataSetChanged()
            } else {
                // If not accepted, handle click events
                acceptIv.setOnClickListener {
                    onItemClickListener?.onClickForAccept(innerData, position)
                }
            }

//            if (innerData.message == "Access request declined and deleted."){
//                notifyDataSetChanged()
//            }

            rejectIv.setOnClickListener {
                onItemClickListenerForReject?.onClickForReject(innerData, position)
            }
            deleteIcon.setOnClickListener {
                onItemClickListenerForReject?.onClickForReject(innerData, position)
            }

//            acceptIv.setOnClickListener {
//
//                if (innerData.isAccepted != true){
//
//                    Log.e("TAG", "onBindViewHolder: ",)
//                    onItemClickListener?.onClickForAccept(innerData, position)
//                    true
//
//                }else {
//                    acceptIv.isEnabled = false
//                    acceptIv.setBackgroundColor(androidx.appcompat.R.color.material_grey_50)
//                    Toast.makeText(context, "Already accepted", Toast.LENGTH_SHORT).show()
//                }
//
//            }
//
//            rejectIv.setOnClickListener {
//                Log.e("TAG", "onBindViewHolder: ", )
//                onItemClickListenerForReject?.onClickForReject(innerData,position)
//                true
//
//            }


        }
    }

    override fun getItemCount(): Int {
        return innerList.size
    }

    // Add a function to get the count of unique item IDs for a specific user name
//    fun getUniqueItemIdsCountForUser(userName: String): Int {
//        return innerList
//            .filter { it.socialLink?.name == userName }
//            .map { it.socialLink?.id }
//            .toSet()
//            .size
//    }

    fun getUniqueItemIdsCountForUser(userName: String): Int {
        val filteredList = innerList.filter { it.socialLink?.name == userName }
        val uniqueItemIds = filteredList.map { it.socialLink?.id }.toSet()

        // Print some debug logs to understand the data
        Log.d("InnerAdapter", "InnerList: $innerList")
        Log.d("InnerAdapter", "FilteredList for $userName: $filteredList")
        Log.d("InnerAdapter", "UniqueItemIds for $userName: $uniqueItemIds")

        return uniqueItemIds.size
    }


    interface OnItemClickListener {
        fun onClickForAccept(item: SocialRequestsData, position: Int)
    }

    interface OnItemClickListenerForReject {
        fun onClickForReject(item: SocialRequestsData, position: Int)
    }

    fun rejectAccess(position: Int) {
        if (position >= 0 && position < innerList.size) {
            innerList.removeAt(position)
            notifyItemRemoved(position)

//            if (innerList.isEmpty()) {
//                // Notify the outer adapter that all items are rejected
//                otherRequestPermissionAdapter.notifyDataSetChanged()
//            }

        }
    }

    interface OnAcceptAllClickListener {
        fun onAcceptAllClick(item: SocialRequestsData, position: Int)
    }

//    fun updateUIAfterAccept(position: Int) {
//        // Update UI elements in the item view after accepting the request
//        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) as? InnerViewHolder
//        viewHolder?.binding?.apply {
//            acceptIv.isVisible = false
//            rejectIv.isVisible = false
//            requestAccessNameTv.text = "Request Accepted"
//        }
//    }



    fun acceptAllRequests() {
        // Iterate through the innerList and accept all requests
        for (position in 0 until innerList.size) {
            val item = innerList[position]
            if (item.isAccepted != true) {
                // Call your ViewModel or Repository method to accept the request
                onAcceptAllClickListener?.onAcceptAllClick(item, position)
            }
        }
    }
}
