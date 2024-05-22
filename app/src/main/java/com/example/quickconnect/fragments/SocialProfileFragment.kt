package com.example.quickconnect.fragments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.quickconnect.R
import com.example.quickconnect.activities.SocialProfileActivity
import com.example.quickconnect.adapters.SocialGetListAdapter
import com.example.quickconnect.databinding.FragmentSocialProfileBinding
import com.example.quickconnect.model.sociallinksdata.CategoryData
import com.example.quickconnect.repository.SocialLinksRepositary
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.SocialLinksViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sdsmdg.tastytoast.TastyToast
import dev.shreyaspatil.MaterialDialog.MaterialDialog


class SocialProfileFragment(private var items: MutableList<CategoryData>) : BottomSheetDialogFragment(), SocialGetListAdapter.OnMessageLongClickListener {

    private lateinit var binding: FragmentSocialProfileBinding
    lateinit var progressBarHelper: ProgressBarHelper

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userSharedPreferences: SharedPreferences
    lateinit var saveUrl : String
    lateinit var userToken: String


    lateinit var socialGetAdapter : SocialGetListAdapter

    private val socialDataViewModel: SocialLinksViewModel by lazy {
        val appDataViewModelFactory = SocialLinksRepositary()
        ViewModelProvider(this, appDataViewModelFactory)[SocialLinksViewModel::class.java]
    }



    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentSocialProfileBinding.inflate(layoutInflater)

        progressBarHelper =  ProgressBarHelper(requireActivity())

        userSharedPreferences = requireContext().getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        userToken = userSharedPreferences.getString("userToken","").toString()
        sharedPreferences = requireContext().getSharedPreferences("GetListData", Context.MODE_PRIVATE)


        saveUrl = sharedPreferences.getString("urlString","").toString()

//        initilization()
        val recyclerView = binding.bottomSheetRecyclerView


        recyclerView.layoutManager = GridLayoutManager(context, 4)

        socialGetAdapter = SocialGetListAdapter(requireContext(),items)

        socialGetAdapter.notifyDataSetChanged()


        socialGetAdapter.onItemClick = { url ->

            try {

            val url = url.links?.link

            // Handle item click by opening the web browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
            requireContext().startActivity(intent)
        }catch (e:ActivityNotFoundException){
            e.printStackTrace()
            // Check if the caught exception is an ActivityNotFoundException
            if (e is ActivityNotFoundException) {
                // Display a Toast message
                TastyToast.makeText(context, "Please check URL", TastyToast.LENGTH_LONG, TastyToast.CONFUSING)

//                Toast.makeText(context, "Please check URL", Toast.LENGTH_SHORT).show()
            }
        }
   }

        recyclerView.adapter = socialGetAdapter
        socialGetAdapter.onMessageLongClickListener = this
//        observableSocialLinkDelete()



        binding.addmoreLinear.setOnClickListener {
            startActivity(Intent(context, SocialProfileActivity::class.java))
        }

        observableSocialLinkDelete()


        return binding.root
    }


    override fun onResume() {
        super.onResume()
        // Ensure that the LiveData is observed for changes
    }

    @SuppressLint("ResourceAsColor", "SuspiciousIndentation")
    private fun observableSocialLinkDelete() {
        socialDataViewModel.observableSocialLinkDelete().observe(viewLifecycleOwner) { deleteData ->
            try {
                // Handle the response from the API if needed
                Log.d("DeleteItem", "Deleted successfully: ${deleteData?.message}")

                val toast = TastyToast.makeText(context, "Removed Successfully", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)
                toast.setGravity(Gravity.FILL_VERTICAL,0,0)
                toast.show()
//                socialGetAdapter.submitList(items.toMutableList())

                socialGetAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun onMessageLongClick(item: CategoryData, position: Int) {
        showDeleteConfirmationDialog(item, position)
    }

    private fun showDeleteConfirmationDialog(item: CategoryData, position: Int) {

        val mDialog = MaterialDialog.Builder(requireActivity())
            .setTitle("Delete?")
            .setMessage("Are you sure want to delete this file?")
            .setAnimation(R.raw.technologies)
            .setCancelable(false)
            .setPositiveButton(
                "Delete", R.drawable.delete_icon
            ) { dialogInterface, which ->
                // Delete Operation
                // Remove item from the adapter and update the list
                socialGetAdapter.removeItem(position)
                socialGetAdapter.notifyDataSetChanged()
//                // Step 4: Update backend
                deleteItemFromBackend(item.links!!.id!!)
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                "Cancel", R.drawable.clear_icon
            ) { dialogInterface, which -> dialogInterface.dismiss() }
            .build()

        // Show Dialog
        mDialog.show()

//        AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
//            .setTitle("Delete Item")
//            .setMessage("Are you sure you want to delete this item?")
//            .setPositiveButton("Yes") { _, _ ->
//                // Remove item from the adapter and update the list
//                socialGetAdapter.removeItem(position)
//                socialGetAdapter.notifyDataSetChanged()
//                // Step 4: Update backend
//                deleteItemFromBackend(item.links!!.id!!)
//            }
//            .setNegativeButton("No", null)
//            .show()
    }

    private fun deleteItemFromBackend(itemId: Int) {
        // Implement backend logic to delete the item
        // This could involve calling a ViewModel or Repository method
        // to update the data in your backend system.
        socialDataViewModel.socialLinkDelete("$userToken", itemId)
    }
}












