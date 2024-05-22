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
import com.example.quickconnect.databinding.SocialRequestListItemBinding
import com.example.quickconnect.model.notificationmodel.SocialRequestsData

class InnerAdapterForRequests(var innerList: MutableList<SocialRequestsData>, val context: Context) :
    RecyclerView.Adapter<InnerAdapterForRequests.InnerViewHolder>() {

    lateinit var onItemClickListener: OnItemClickListener
    lateinit var onItemClickListenerForReject: OnItemClickListenerForReject
    lateinit var onAcceptAllClickListener: OnAcceptAllClickListener

    private val innerItems: MutableList<SocialRequestsData> = mutableListOf(

    )


    inner class InnerViewHolder(val binding: SocialRequestListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        val binding = SocialRequestListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InnerViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        val innerData = innerList[position]

        holder.binding.apply {

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

//            Picasso.get().invalidate(imageUrl)
//
//            Picasso.get().setLoggingEnabled(true)
//
//            Picasso.get()
//                .load(imageUrl)
//                .placeholder(R.drawable.clear_icon)
//                .error(R.drawable.clear_icon)
//                .into(holder.binding.requestPersonIv, object : Callback {
//                    override fun onSuccess() {
//                        Log.d("TAG", "Image loaded successfully")
//                    }
//                    override fun onError(e: Exception?) {
//                        Log.e("TAG", "Error loading image", e)
//                    }
//                })




//            Picasso.get()
//                .load(imageUrl)
//                .placeholder(R.drawable.clear_icon) // Placeholder image resource
//                .error(R.drawable.clear_icon) // Error image resource
//                .into(holder.binding.requestPersonIv)

            acceptIv.setOnClickListener {

                if (innerData.isAccepted != true){

                    Log.e("TAG", "onBindViewHolder: ",)
                    onItemClickListener?.onClickForAccept(innerData, position)
                    true

                }
                else {
                    acceptIv.isEnabled = false
                    acceptIv.setBackgroundColor(androidx.appcompat.R.color.material_grey_50)
                    Toast.makeText(context, "Already accepted", Toast.LENGTH_SHORT).show()
                }
            }

            rejectIv.setOnClickListener {
                Log.e("TAG", "onBindViewHolder: ", )
                onItemClickListenerForReject?.onClickForReject(innerData,position)
                true

            }

            if (innerList.isNotEmpty()){
                acceptIv.isVisible = false
                rejectIv.isVisible = false
            }
        }
    }

    override fun getItemCount(): Int {
        return innerList.size
    }


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
        }
    }

    interface OnAcceptAllClickListener {
        fun onAcceptAllClick(item: SocialRequestsData, position: Int)
    }


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

    fun setInnerItems(items: List<SocialRequestsData>) {
        innerItems.clear()
        innerItems.addAll(items)
        notifyDataSetChanged()
    }
}
