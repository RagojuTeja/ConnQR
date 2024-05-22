package com.example.quickconnect.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.databinding.ActionCallItemBinding
import com.example.quickconnect.model.CallToActionModel.ActionList

class CallToActionAdapter(var actionCallList: MutableList<ActionList>, var context: Context) :
    RecyclerView.Adapter<CallToActionAdapter.CallToActionViewHolder>() {

    var typeofActionAdapter : TypeofActionAdapter? = null

    lateinit var onMessageLongClickListener: OnMessageLongClickListener
    lateinit var onItemClick: ((ActionList) -> Unit)



    inner class CallToActionViewHolder(var binding: ActionCallItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallToActionAdapter.CallToActionViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        var binding = ActionCallItemBinding.inflate(inflater, parent, false)
        return CallToActionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CallToActionAdapter.CallToActionViewHolder, position: Int) {
        var actionData = actionCallList[position]

        holder.binding.apply {

            val imageUrl = actionData.category?.icon // Create absolute URL

            try {

                val imageLoader = ImageLoader.Builder(context)
                    .components {
                        add(SvgDecoder.Factory())
                    }
                    .build()

                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .target(holder.binding.acImg)
                    .build()


                imageLoader.enqueue(request)

            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            acTv.text = actionData.name
        }


        holder.itemView.setOnLongClickListener {
            onMessageLongClickListener?.onMessageLongClick(actionData,position)
            true
        }

        holder.itemView.setOnClickListener {
            onItemClick.invoke(actionData)
        }
    }

    override fun getItemCount(): Int {
        return actionCallList.size
    }


    fun removeItem(position: Int) {
        if (position >= 0 && position < actionCallList.size) {
            actionCallList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    interface OnMessageLongClickListener {
        fun onMessageLongClick(item: ActionList, position: Int)
    }



}