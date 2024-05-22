package com.example.quickconnect.adapters

 import android.content.Context
 import android.util.Log
 import android.view.LayoutInflater
 import android.view.ViewGroup
 import androidx.recyclerview.widget.RecyclerView
 import coil.ImageLoader
 import coil.decode.SvgDecoder
 import coil.request.ImageRequest
 import com.example.quickconnect.databinding.TypeOfActionItemBinding
 import com.example.quickconnect.model.CallToActionModel.CallListCategorieData

class TypeofActionAdapter(var typeofActionList: List<CallListCategorieData>, var context: Context):
    RecyclerView.Adapter<TypeofActionAdapter.ViewHolder>() {


    var onItemClick: ((CallListCategorieData)->Unit) ?= null



    inner class ViewHolder(val binding: TypeOfActionItemBinding):RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeofActionAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TypeOfActionItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TypeofActionAdapter.ViewHolder, position: Int) {
       val typeOfActionData = typeofActionList[position]

        holder.binding.apply {
            try {

                val imageLoader = ImageLoader.Builder(context)
                    .components {
                        add(SvgDecoder.Factory())
                    }
                    .build()

                val request = ImageRequest.Builder(context)
                    .data(typeOfActionData.icon)
                    .crossfade(true)
                    .target(holder.binding.taIv)
                    .build()


                imageLoader.enqueue(request)

            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            taTv.text = typeOfActionData.name
        }

        holder.itemView.setOnClickListener {
            Log.e("TAG", "ItemView clicked at position $position")

            onItemClick?.invoke(typeofActionList[position])
        }
    }

    override fun getItemCount(): Int {
        return typeofActionList.size
    }



    interface ItemClickListener {
        fun onItemClick(item: CallListCategorieData)
    }
}