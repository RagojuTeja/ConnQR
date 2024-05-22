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
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.R
import com.example.quickconnect.databinding.FavouriteItemLayoutBinding
import com.example.quickconnect.model.favouritesmodel.FavouriteData
import com.squareup.picasso.Picasso
import java.util.Locale

class FavListAdapter(var favList : MutableList<FavouriteData>, val context: Context) :
    RecyclerView.Adapter<FavListAdapter.FavViewHolder>(), Filterable {

    lateinit var onItemClick: ((FavouriteData) -> Unit)
    lateinit var onItemClickListener: OnItemClickListener

    private var originalList: List<FavouriteData> = emptyList()
    private var filteredList: List<FavouriteData> = emptyList()



    inner class FavViewHolder(val  binding: FavouriteItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ): FavListAdapter.FavViewHolder {
        val binding = FavouriteItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavListAdapter.FavViewHolder, position: Int) {
        val favData = favList[position]

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
            }else{
                holder.binding.favouriteImageIv.setBackgroundResource(R.drawable.profile_placeholder)
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


            }

        holder.binding.unFavTv.setOnClickListener {
            Log.e("TAG", "onBindViewHolder: ", )
            onItemClickListener?.onClick(favData,position)
            true
        }

        holder.itemView.setOnClickListener {
            onItemClick.invoke(favData)
        }

    }

    override fun getItemCount(): Int {
        return favList.size
    }

    interface OnItemClickListener {
        fun onClick(item: FavouriteData, position: Int)
    }


    fun removeFav(position: Int) {
        if (position >= 0 && position < favList.size) {
            favList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setData(data: List<FavouriteData>) {
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

        favList = filteredList
        notifyDataSetChanged()
    }

    // In FavListAdapter.kt
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
                filteredList = results?.values as List<FavouriteData>
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


//    private fun loadImageServer(imageUri: String, holder: FavViewHolder) {
//        Log.e("TAG", "loadImageServer: ", )
//        Picasso.get()
//            .load(imageUri)
//            .error(R.drawable.profile_placeholder)
//            .placeholder(R.drawable.profile_placeholder)
//            .into(holder.binding.favouriteImageIv)
//
//        // Request layout to ensure the ImageView is properly laid out
//        holder.binding.favouriteImageIv!!.requestLayout()
//        holder.binding.favouriteImageIv!!.invalidate()
//
//        // Post a delayed action to retry loading the image after a short delay
//        holder.binding.favouriteImageIv!!.postDelayed({
//            // Get the drawable from the ImageView
//            val drawable = holder.binding.favouriteImageIv!!.drawable
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
//                            ContextCompat.getColor(context, R.color.black)
//                        ) ?: ContextCompat.getColor(context, R.color.black)
//
//                        // Set the background color for your ScrollView
//                        holder.binding.favLinear.setBackgroundColor(backgroundColor)
//                    }
//
//                    // Upload the image to the server (replace this with your actual upload logic)
////                    uploadImageToServer(bitmap)
//                } else {
//                    // Handle the case when the bitmap is null
//                    Toast.makeText(context, "Error loading image: Bitmap is null", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                // Handle the case when the drawable is not a BitmapDrawable
//                Toast.makeText(context, "Error loading image: Not a BitmapDrawable", Toast.LENGTH_SHORT).show()
//            }
//        }, 500) // Delay for 500 milliseconds before retrying
//    }
}