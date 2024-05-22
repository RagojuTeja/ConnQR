package com.example.quickconnect.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.R
import com.example.quickconnect.adapters.InnerAdapterForRequests
import com.example.quickconnect.adapters.OtherInnerAdapterForRequests
import com.example.quickconnect.adapters.OtherRequestPermissionAdapter
import com.example.quickconnect.databinding.FragmentOtherRequestBinding
import com.example.quickconnect.model.notificationmodel.OtherData
import com.example.quickconnect.model.notificationmodel.SocialRequestsData
import com.example.quickconnect.model.notificationmodel.SocialRequestsOthersData
import com.example.quickconnect.repository.NotificationRepositary
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.NotificationViewModel
import com.sdsmdg.tastytoast.TastyToast
import dev.shreyaspatil.MaterialDialog.MaterialDialog


class OtherRequestFragment : Fragment(),OtherInnerAdapterForRequests.OnItemClickListener,
    OtherInnerAdapterForRequests.OnItemClickListenerForReject {

    private lateinit var binding : FragmentOtherRequestBinding
    lateinit var progressBarHelper: ProgressBarHelper

    lateinit var otherRequestPermissionAdapter: OtherRequestPermissionAdapter
    lateinit var otherInnerAdapterForRequests: OtherInnerAdapterForRequests
    lateinit var otherequestRv : RecyclerView


    lateinit var sharedPreferences: SharedPreferences
    lateinit var userToken : String
    lateinit var userName : String
    lateinit var socialName : String


    private val requestViewModel: NotificationViewModel by lazy {
        val requestViewModelFactory = NotificationRepositary()
        ViewModelProvider(this, requestViewModelFactory )[NotificationViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentOtherRequestBinding.inflate(layoutInflater)


        Initialization()

        sharedPreferences = requireContext().getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        userToken = sharedPreferences.getString("userToken","").toString()
//        userNameMy = sharedPreferences.getString("userNameMy","").toString()
//        userName = sharedPreferences.getString("userName", "").toString()

        OtherRequestUsers()
        observerOthersRequestData()
        observerOtherSocialRequsetData()
        observeAcceptRequest()
//        observeRemoveRequest()



        return binding.root
    }



    fun Initialization(){

        progressBarHelper =  ProgressBarHelper(requireActivity())


        otherequestRv = binding.notificationRv
    }


    private fun OtherRequestUsers() {
        requestViewModel.otherUserRequestList("$userToken")
    }

    fun observerOthersRequestData(){
        requestViewModel.observeOtherRequstUserData().observe(viewLifecycleOwner){otheruserRequestList ->

            try {

            otherUserRequestListAddToAdapter(otheruserRequestList)

            Log.e("TAG", "observerMyData: ${otheruserRequestList}", )

            otherRequestPermissionAdapter.onItemClick = {list ->

                userName = list.username.toString()

                // Toggle the visibility of the linearInnerRv layout
                binding.linearInnerRv.isVisible = !binding.linearInnerRv.isVisible

                if (binding.linearInnerRv.isVisible) {
                    // If the linearInnerRv is now visible, fetch and display the inner RecyclerView data
                    listOfotherSocialRequest()
                }

            }
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    fun otherUserRequestListAddToAdapter(userRequestList : MutableList<OtherData>) {
        try {
            otherRequestPermissionAdapter = OtherRequestPermissionAdapter(userRequestList, requireContext())
            otherequestRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)  // Set layout manager here
            otherequestRv.adapter = otherRequestPermissionAdapter
//            requestPermissionAdapter.onAcceptClickListener = this
            // Update the existing list in the adapter
            otherRequestPermissionAdapter.requestUserList = userRequestList
            otherRequestPermissionAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun listOfotherSocialRequest (){
        requestViewModel.otherSocialRequesteList("$userToken", "$userName")
    }

    fun observerOtherSocialRequsetData(){
        requestViewModel.observeOtherSocialRequestList().observe(viewLifecycleOwner) { socialRequestData ->
            try {
                Log.e("TAG", "observerSocialRequsetDataMy: $socialRequestData")

                socialName = socialRequestData.last().socialLink!!.name.toString()

                otherInnerAdapterForRequests = OtherInnerAdapterForRequests(socialRequestData, requireContext())
                binding.innerRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)  // Set layout manager for innerRequestRv
                binding.innerRecyclerView.adapter = otherInnerAdapterForRequests

                binding.linearInnerRv.isVisible = true

                otherInnerAdapterForRequests.onItemClickListener = this
                otherInnerAdapterForRequests.onItemClickListenerForReject = this

                val notificationCountTv = requireView().findViewById<TextView>(R.id.notification_rq_tv)


//                val uniqueItemIdsCount = innerAdapterForRequest.getUniqueItemIdsCountForUser(userName)

//                notificationCountTv.text = "Requested $uniqueItemIdsCount Profile"


                val requestCount = otherInnerAdapterForRequests.innerList.count()

                notificationCountTv.setText("Requested $requestCount Profile")

                // Update the existing list in the adapter
                otherInnerAdapterForRequests.innerList = socialRequestData
                otherInnerAdapterForRequests.notifyDataSetChanged()
                otherRequestPermissionAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onClickForAccept(item: SocialRequestsData, position: Int) {
        acceptRequest(item.id!!)
    }

    override fun onClickForReject(item: SocialRequestsData, position: Int) {
        if (item.isAccepted == false) {

            AlertDialog.Builder(requireActivity())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to reject Request")
                .setPositiveButton("Yes") { _, _ ->
                    // Remove item from the adapter and update the list
                    otherInnerAdapterForRequests.rejectAccess(position)
                    otherRequestPermissionAdapter.notifyDataSetChanged()
                    // Step 4: Update backend
                    Log.e("TAG", "itemid: ${item.id}",)
                    deleteItemFromBackend(item.id!!)
                }
                .setNegativeButton("No", null)
                .show()
        }else{

            val mDialog = MaterialDialog.Builder(requireActivity())
                .setTitle("Remove")
                .setMessage("Are you sure want to remove notification?")
                .setAnimation(R.raw.technologies)
                .setCancelable(false)
                .setPositiveButton(
                    "Remove", R.drawable.delete_icon
                ) { dialogInterface, which ->
                    // Delete Operation
                    // Remove item from the adapter and update the list
                    otherInnerAdapterForRequests.rejectAccess(position)
                    otherRequestPermissionAdapter.notifyDataSetChanged()
                    otherInnerAdapterForRequests.notifyDataSetChanged()

//                    // Step 4: Update backend
//                    Log.e("TAG", "itemid: ${item.id}",)
                    deleteItemFromBackend(item.id!!)
                    dialogInterface.dismiss()
                }
                .setNegativeButton(
                    "Cancel", R.drawable.clear_icon
                ) { dialogInterface, which -> dialogInterface.dismiss() }
                .build()

            // Show Dialog
            mDialog.show()

//            AlertDialog.Builder(requireActivity())
//                .setTitle("Delete Item")
//                .setMessage("Are you sure you want to remove")
//                .setPositiveButton("Yes") { _, _ ->
//                    // Remove item from the adapter and update the list
//                    otherRequestPermissionAdapter.rejectAccess(position)
//                    otherRequestPermissionAdapter.notifyDataSetChanged()
//                    // Step 4: Update backend
//                    Log.e("TAG", "itemid: ${item.id}",)
//                    deleteItemFromBackend(item.id!!)
//                }
//                .setNegativeButton("No", null)
//                .show()
        }
    }


    private fun acceptRequest(id: Int) {
        requestViewModel.permissionStatus("$userToken", id, "grant")
        otherRequestPermissionAdapter.notifyDataSetChanged()
    }

    fun observeAcceptRequest(){
        requestViewModel.observeApprovalStatus().observe(viewLifecycleOwner){acceptRequest ->


            if (acceptRequest.status == true){
                Log.e("TAG", "observeRemoveRequest:$acceptRequest ", )
                listOfotherSocialRequest()
                OtherRequestUsers()
                otherInnerAdapterForRequests.notifyDataSetChanged()
                otherRequestPermissionAdapter.notifyDataSetChanged()
            }

            if (acceptRequest.message == "Access request granted") {

                listOfotherSocialRequest()

//                val toast = TastyToast.makeText(
//                    context,
//                    "Accepted",
//                    TastyToast.LENGTH_LONG,
//                    TastyToast.SUCCESS
//                )
//                toast.setGravity(Gravity.CENTER, 0, 0)
//                toast.show()


                otherInnerAdapterForRequests.notifyDataSetChanged()
                otherRequestPermissionAdapter.notifyDataSetChanged()

//
            }
        }
    }

    private fun deleteItemFromBackend(itemId: Int) {
        // Implement backend logic to delete the item
        // This could involve calling a ViewModel or Repository method
        // to update the data in your backend system.

        requestViewModel.permissionStatus("$userToken", itemId, "decline")
//        listOfotherSocialRequest()
//        otherRequestPermissionAdapter.notifyDataSetChanged()
//        otherInnerAdapterForRequests.notifyDataSetChanged()

    }

//    private fun observeRemoveRequest() {
//        requestViewModel.observeApprovalStatus().observe(viewLifecycleOwner){acceptRequest ->
//
//            Log.e("TAG", "observeRemoveRequest:$acceptRequest ", )
//
//
//
//            if (acceptRequest.status == true){
//
//                listOfotherSocialRequest()
//
//
//                // Handle the response from the API if needed
////                Log.d("DeleteItem", "Deleted successfully: ${deleteData?.message}")
////
////                val toast = TastyToast.makeText(context, "Removed Successfully", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)
////                toast.setGravity(Gravity.CENTER, 0, 0)
////                toast.show()
//
//                otherRequestPermissionAdapter.notifyDataSetChanged()
//                otherInnerAdapterForRequests.notifyDataSetChanged()
//
//            }
//        }
//    }


}