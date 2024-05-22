package com.example.quickconnect.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.R
import com.example.quickconnect.databinding.FavouriteItemLayoutBinding
import com.example.quickconnect.model.favouritesmodel.FavouriteData
import com.example.quickconnect.model.favouritesmodel.ViewersList
import com.squareup.picasso.Picasso
import java.util.Locale

class ViewersListAdapter(var viewerList : MutableList<ViewersList>, val context: Context) :
    RecyclerView.Adapter<ViewersListAdapter.FavViewHolder>(), Filterable {

    lateinit var onItemClick: ((ViewersList) -> Unit)
    lateinit var onItemClickListener: OnItemClickListener

    private var originalList: List<ViewersList> = emptyList()
    private var filteredList: List<ViewersList> = emptyList()



    inner class FavViewHolder(val  binding: FavouriteItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ): ViewersListAdapter.FavViewHolder {
        val binding = FavouriteItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewersListAdapter.FavViewHolder, position: Int) {
        val favData = viewerList[position]

        holder.binding.apply {

            val name =  favData.fullName
            if (!name.isNullOrEmpty()){
            favouriteNameTv.text = favData.fullName
            favouriteWorkAtTv.text = favData.workAt
            favouriteDescTv.text = favData.description
            }else{
                favouriteNameTv.text = favData.username
            }

//            if (profilePicString.isNotEmpty()) {

                val imageUrl = favData.profilePic


            if (!imageUrl.isNullOrEmpty()) {
                loadImageServer(imageUrl!!,holder)
            }

//                Picasso.get()
//                    .load(imageUrl)
//                    .placeholder(R.drawable.profile_placeholder) // Placeholder image resource
//                    .error(R.drawable.profile_placeholder) // Error image resource
//                    .into(holder.binding.favouriteImageIv)


            if (favData.workAt!!.isEmpty()){
                favouriteWorkAtTv.visibility = View.GONE
            }else if (favData.description!!.isEmpty()){
                favouriteDescTv.visibility = View.GONE
            }

            holder.binding.unFavTv.isVisible = false

            }

        holder.binding.unFavTv.setOnClickListener {
            Log.e("TAG", "onBindViewHolder: ", )
            onItemClickListener?.onClick(favData,position)
            true

        }

//        holder.itemView.setOnClickListener {
//            onItemClick.invoke(favData)
//        }

    }

    override fun getItemCount(): Int {
        return viewerList.size
    }

    interface OnItemClickListener {
        fun onClick(item: ViewersList, position: Int)
    }


    fun removeFav(position: Int) {
        if (position >= 0 && position < viewerList.size) {
            viewerList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setData(data: List<ViewersList>) {
        originalList = data
        notifyDataSetChanged()
    }


    fun filter(query: String) {
        val filteredList = if (query.isBlank()) {
            originalList.toMutableList()
        } else {
            originalList.filter {
                it.fullName?.toLowerCase(Locale.getDefault())
                    ?.contains(query.toLowerCase(Locale.getDefault())) == true
            }.toMutableList()
        }

        viewerList = filteredList
        notifyDataSetChanged()
    }

    // In ViewersListAdapter.kt
//    fun filter(query: String) {
//        val filteredList = if (query.isBlank()) {
//            originalList.toMutableList()
//        } else {
//            originalList.filter {
//                it.fullName?.toLowerCase(Locale.getDefault())
//                    ?.contains(query.toLowerCase(Locale.getDefault())) == true
//            }.toMutableList()
//        }
//
//        setData(filteredList)
//        notifyDataSetChanged()
//    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                val queryString = constraint?.toString()?.toLowerCase(Locale.getDefault())

                filteredList = if (queryString.isNullOrBlank()) {
                    originalList
                } else {
                    originalList.filter {
                        it.fullName?.toLowerCase(Locale.getDefault())?.contains(queryString) == true
                    }
                }

                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<ViewersList>
                notifyDataSetChanged()
            }
        }
    }


    private fun loadImageServer(imageUri: String,holder: FavViewHolder) {
        Picasso.get()
            .load(imageUri)
            .error(R.drawable.profile_placeholder)
            .placeholder(R.drawable.profile_placeholder)
            .into(holder.binding.favouriteImageIv)

        holder.binding.favouriteImageIv!!.postDelayed({
            val drawable = holder.binding.favouriteImageIv!!.drawable

            if (drawable != null && drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap

                if (bitmap != null) {
                    val bottomBitmap = Bitmap.createBitmap(bitmap, 0,
                        (bitmap.height * 0.8).toInt(), bitmap.width, (bitmap.height * 0.2).toInt()
                    )

                    Palette.from(bottomBitmap).generate { palette ->
                        val backgroundColor = palette?.getDominantColor(
                            ContextCompat.getColor(context, R.color.black)
                        ) ?: ContextCompat.getColor(context, R.color.black)

                        // Apply gradient to the background
                        applyGradientBackground(backgroundColor, holder)
                    }
                } else {
                    Toast.makeText(context, "Error loading image: Bitmap is null", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Error loading image: Not a BitmapDrawable", Toast.LENGTH_SHORT).show()
            }
        }, 500)
    }

    private fun applyGradientBackground(color: Int,holder: FavViewHolder) {
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(color, ContextCompat.getColor(context, R.color.black))
        )

        holder.binding.favLinear.background = gradientDrawable

    }


//    private fun loadImageServer(imageUri: String) {
//        Picasso.get()
//            .load(imageUri)
//            .error(R.drawable.profile_placeholder)
//            .placeholder(R.drawable.profile_placeholder)
//            .into(profile_pic)
//
//        // Request layout to ensure the ImageView is properly laid out
//        profile_pic!!.requestLayout()
//        profile_pic!!.invalidate()
//
//        // Post a delayed action to retry loading the image after a short delay
//        profile_pic!!.postDelayed({
//            // Get the drawable from the ImageView
//            val drawable = profile_pic!!.drawable
//
//            if (drawable != null && drawable is BitmapDrawable) {
//                // Get the bitmap from the BitmapDrawable
//                val bitmap = drawable.bitmap
//
//                // Check if the bitmap is not null
//                if (bitmap != null) {
//                    // Calculate the bottom 20% of the image
//                    val bottomBitmap = Bitmap.createBitmap(bitmap, 0,
//                        (bitmap.height * 0.8).toInt(), bitmap.width, (bitmap.height * 0.2).toInt()
//                    )
//
//                    // Use Palette to extract the dominant color from the bottomBitmap
//                    Palette.from(bottomBitmap).generate { palette ->
//                        val backgroundColor = palette?.getDominantColor(
//                            ContextCompat.getColor(this, R.color.black)
//                        ) ?: ContextCompat.getColor(this, R.color.black)
//
//                        // Set the background color for your ScrollView
//                        findViewById<LinearLayout>(R.id.bg_layout_linear).setBackgroundColor(backgroundColor)
//                    }
//
//                    // Upload the image to the server (replace this with your actual upload logic)
////                    uploadImageToServer(bitmap)
//                } else {
//                    // Handle the case when the bitmap is null
//                    Toast.makeText(this, "Error loading image: Bitmap is null", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                // Handle the case when the drawable is not a BitmapDrawable
//                Toast.makeText(this, "Error loading image: Not a BitmapDrawable", Toast.LENGTH_SHORT).show()
//            }
//        }, 500) // Delay for 500 milliseconds before retrying
//    }

}