package com.example.quickconnect.adapters

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.quickconnect.R
import com.example.quickconnect.databinding.SocialGetListItemLayoutBinding
import com.example.quickconnect.model.CallToActionModel.ActionList
import com.example.quickconnect.model.viewersmodel.SocialLinks

class UserViewSocialLinkAdapter(val userViewList : MutableList<SocialLinks>, val context: Context) :
    RecyclerView.Adapter<UserViewSocialLinkAdapter.UserViewHolder>(){

    lateinit var onItemClick: ((SocialLinks) -> Unit)


    inner class UserViewHolder(val  binding: SocialGetListItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ): UserViewSocialLinkAdapter.UserViewHolder {
        val binding = SocialGetListItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor", "LongLogTag")
    override fun onBindViewHolder(holder: UserViewSocialLinkAdapter.UserViewHolder, position: Int) {
        val userData = userViewList[position]

        holder.binding.apply {

            socialGetLiItemTv.text = userData.name


            if (userData.links!!.isLocked == true && userData.links!!.link == null) {

//                holder.binding.socialGetLiItemIv.setImageResource(R.drawable.lock_icon)

                holder.binding.lockAppCompatImageButton.isVisible = true

                try {

                    val imageLoader = ImageLoader.Builder(context)
                        .components {
                            add(SvgDecoder.Factory())
                        }
                        .build()

                    holder.binding.socialGetLiItemIv.invalidate()

                    val request = ImageRequest.Builder(context)
                        .data(userData.icon)
                        .crossfade(true)
                        .target(holder.binding.socialGetLiItemIv)
                        .build()

                    Log.d("ImageLoader", "Loading image from URL: ${userData.icon}")



                    imageLoader.enqueue(request)

                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }

            } else if (userData.links!!.isLocked == null && userData.links!!.link == null) {

                holder.binding.socialGetLiItemIv.setColorFilter(
                    ContextCompat.getColor(context, androidx.constraintlayout.widget.R.color.primary_text_disabled_material_dark),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                holder.binding.socialGetLiItemTv.setTextColor(
                    ContextCompat.getColor(context, androidx.constraintlayout.widget.R.color.primary_text_disabled_material_dark)
                )

//                // If not locked and no link, apply fade effect
//                val fadeOut = ObjectAnimator.ofFloat(socialGetLiItemIv, "alpha", 3f, 0.9f)
//                fadeOut.duration = 1000 // Adjust the duration as needed
//                fadeOut.start()

            }else{

                try {

                    val imageLoader = ImageLoader.Builder(context)
                        .components {
                            add(SvgDecoder.Factory())
                        }
                        .build()

                    holder.binding.socialGetLiItemIv.invalidate()

                    val request = ImageRequest.Builder(context)
                        .data(userData.icon)
                        .crossfade(true)
                        .target(holder.binding.socialGetLiItemIv)
                        .build()

                    Log.d("ImageLoader", "Loading image from URL: ${userData.icon}")



                    imageLoader.enqueue(request)

                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }


            }


        }

//        holder.itemView.setOnClickListener {
//            onItemClick.invoke(userData)
//        }

        holder.itemView.setOnClickListener {
            if (::onItemClick.isInitialized) {
                onItemClick.invoke(userData)
                Log.d("UserViewSocialLinkAdapter", "onItemClick invoked")
            } else {
                Log.e("UserViewSocialLinkAdapter", "onItemClick is not initialized")
            }
        }



    }

    fun updateData(newList: MutableList<SocialLinks>) {
        userViewList.clear()
        userViewList.addAll(newList)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return userViewList.size
    }

}