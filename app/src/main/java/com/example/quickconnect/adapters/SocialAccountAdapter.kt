package com.example.quickconnect.adapters

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.quickconnect.R
import com.example.quickconnect.databinding.SocialAccountItemLayoutBinding
import com.example.quickconnect.model.sociallinksdata.CategoryData
import java.io.Serializable
import java.util.Locale

class SocialAccountAdapter(var socialList: List<CategoryData>, var context: Context):
    RecyclerView.Adapter<SocialAccountAdapter.ViewHolder>(), Filterable {

    private var originalList: List<CategoryData> = emptyList()
    private var filteredList: List<CategoryData> = emptyList()

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userToken: String


    var itemClickListener: ItemClickListener? = null
    var switchChangeListener: SwitchChangeListener? = null

    var onItemClick: ((CategoryData) -> Unit)? = null

    var enteredUrls: MutableMap<Int, String> = mutableMapOf() // Provide access to enteredUrls

    var isLocked: Int? = null


    inner class ViewHolder(val binding: SocialAccountItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SocialAccountItemLayoutBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SocialAccountAdapter.ViewHolder, position: Int) {
        val typeOfActionData = socialList[position]

        holder.binding.apply {
            socialAcTv.text = typeOfActionData.name
        }
        Log.e("TAG", "linkcheck: ${typeOfActionData.links!!.link}",)

        try {
            val imageLoader = ImageLoader.Builder(context)
                .components {
                    add(SvgDecoder.Factory())
                }
                .build()

            holder.binding.socialAcIv.invalidate()

            val request = ImageRequest.Builder(context)
                .data(typeOfActionData.icon)
                .crossfade(true)
                .target(holder.binding.socialAcIv)
                .build()

            Log.d("ImageLoader", "Loading image from URL: ${typeOfActionData.icon}")

            imageLoader.enqueue(request)

        } catch (e: NullPointerException) {
            e.printStackTrace()
            Log.e("ImageLoading", "Exception during image loading: $e")
        }

        if (typeOfActionData.links?.link?.isNotEmpty() == true) {
            // Set background color to green only if the link of the current item is not empty
            holder.binding.socialItemLinearL.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.green
                )
            )
        } else {
            // Set the background color to the default color if the link is empty
            holder.binding.socialItemLinearL.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.light_ash
                )
            )
        }

//        if (typeOfActionData.links?.link?.isNotEmpty() == true) {
//            // Set background color to green if the link is not empty
//            holder.binding.socialItemLinearL.setBackgroundColor(
//                ContextCompat.getColor(
//                    holder.itemView.context,
//                    R.color.green
//                )
//            )
//        }

//
//        // Initialize switch state based on the isLocked property
//        val isLocked = typeOfActionData.links?.isLocked ?: false
//        holder.binding.switchAction.isChecked = isLocked
//
//        val lock = typeOfActionData.links?.isLocked ?: 0
//        holder.binding.switchAction.isChecked = lock == 1 // or any other condition that makes sense in your context
//
//        if (isLocked == true){
//            holder.binding.switchAction.isChecked = true
//        }else{
//            holder.binding.switchAction.isChecked = false
//        }
//
//        Log.e("TAG", "onBindViewHolder: ${typeOfActionData.links!!.link}",)
//
//        holder.binding.switchAction.isClickable = true
//
//
//        // Set an OnCheckedChangeListener to handle switch state changes
//        holder.binding.switchAction.setOnCheckedChangeListener { _, isChecked ->
//            // Update the isLocked property in your data model
//            typeOfActionData.links!!.isLocked = isChecked
//
//            holder.binding.switchAction.isChecked = true
//
////            switchAction()
//
////            if (isChecked) {
////               holder.binding.switchAction.text =  "Switch1:ON"
////            } else {
////                holder.binding.switchAction.text =  "Switch1:ON"
////            }
//
//
//            if (typeOfActionData.links!!.isLocked == true) {
//                holder.binding.switchAction.isChecked
//            }else{
//                holder.binding.switchAction.isChecked = false
//            }
//
//
//            enteredUrls[position] = isChecked.toString()
//
//
//            switchChangeListener?.onSwitchChanged(typeOfActionData.id!!, isChecked)
//
//
//        }



        // Set switch state based on isLocked property
        holder.binding.switchAction.setOnCheckedChangeListener(null) // Remove listener to prevent callback during recycling
        holder.binding.switchAction.isChecked = typeOfActionData.links?.isLocked == true

        // Set listener to handle switch state changes
        holder.binding.switchAction.setOnCheckedChangeListener { _, isChecked ->
            // Update the isLocked property in your data model
            typeOfActionData.links?.isLocked = isChecked
            switchChangeListener?.onSwitchChanged(typeOfActionData.id!!, isChecked)
        }




        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(typeOfActionData, typeOfActionData.id!!)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(socialList[position])
        }
    }


    override fun getItemCount(): Int {
        return socialList.size
    }


    fun setData(data: List<CategoryData>) {
        originalList = data
        notifyDataSetChanged()
    }


    fun filter(query: String) {
        val filteredList = if (query.isBlank()) {
            originalList
        } else {
            originalList.filter {
                it.name?.toLowerCase(Locale.getDefault())
                    ?.contains(query.toLowerCase(Locale.getDefault())) == true
            }
        }

        socialList = filteredList
        notifyDataSetChanged()
    }


    interface ItemClickListener {
        fun onItemClick(item: CategoryData, id: Int)
    }

    interface SwitchChangeListener {
        fun onSwitchChanged(id: Int, isChecked: Boolean)
    }

//    fun onSaveInstanceState(outState: Bundle) {
//        // Save the switch state for each item in the enteredUrls map
//        outState.putSerializable("enteredUrls", enteredUrls.toSerializableMap())
//    }
//
//    fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        // Restore the switch state for each item from the enteredUrls map
//        val restoredUrls = savedInstanceState.getSerializable("enteredUrls") as? SerializableMap<Int, String>
//        restoredUrls?.let {
//            enteredUrls.putAll(it.toMap())
//            notifyDataSetChanged()
//        }
//    }
//
//    data class SerializableMap<K : java.io.Serializable, V : java.io.Serializable>(val map: Map<K, V>) :
//        java.io.Serializable
//
//    fun <K : java.io.Serializable, V : java.io.Serializable> Map<K, V>.toSerializableMap(): SerializableMap<K, V> {
//        return SerializableMap(this)
//    }
//
//    fun <K : java.io.Serializable, V : Serializable> SerializableMap<K, V>.toMap(): Map<K, V> {
//        return map
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
                        it.name?.toLowerCase(Locale.getDefault())?.contains(queryString) == true
                    }
                }

                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<CategoryData>
                notifyDataSetChanged()
            }
        }
    }
}