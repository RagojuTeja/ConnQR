package com.example.quickconnect.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.quickconnect.databinding.SocialGetListItemLayoutBinding
import com.example.quickconnect.model.CallToActionModel.ActionList
import com.example.quickconnect.model.sociallinksdata.CategoryData

class SocialGetListAdapter(val context: Context, var socialGetList: MutableList<CategoryData>, val itemClickListener: ((CategoryData) -> Unit)? = null) :
    RecyclerView.Adapter<SocialGetListAdapter.SocialViewHolder>() {

    lateinit var onMessageLongClickListener: OnMessageLongClickListener
    lateinit var onItemClick: ((CategoryData) -> Unit)



    inner class SocialViewHolder(val binding : SocialGetListItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SocialGetListAdapter.SocialViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SocialGetListItemLayoutBinding.inflate(inflater, parent, false)
        return SocialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SocialGetListAdapter.SocialViewHolder, position: Int) {
        val getData = socialGetList[position]

        holder.binding.apply {

            socialGetLiItemTv.text = getData.name

            try {

                val imageLoader = ImageLoader.Builder(context)
                    .components {
                        add(SvgDecoder.Factory())
                    }
                    .build()

                holder.binding.socialGetLiItemIv.invalidate()

                val request = ImageRequest.Builder(context)
                    .data(getData.icon)
                    .crossfade(true)
                    .target(holder.binding.socialGetLiItemIv)
                    .build()

                Log.d("ImageLoader", "Loading image from URL: ${getData.icon}")



                imageLoader.enqueue(request)

            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            holder.itemView.setOnClickListener {
                onItemClick.invoke(getData)
            }


            holder.itemView.setOnLongClickListener {
                onMessageLongClickListener?.onMessageLongClick(getData,position)
                true
            }


        }
    }

    override fun getItemCount(): Int {
        return socialGetList.size
    }

    interface OnMessageLongClickListener {
        fun onMessageLongClick(item: CategoryData, position: Int)
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < socialGetList.size) {
            socialGetList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

}