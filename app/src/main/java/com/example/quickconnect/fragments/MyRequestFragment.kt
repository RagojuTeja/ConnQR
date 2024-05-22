package com.example.quickconnect.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.R
import com.example.quickconnect.adapters.InnerAdapterForRequests
import com.example.quickconnect.adapters.RequestPermissionAdapter
import com.example.quickconnect.databinding.FragmentMyRequestBinding
import com.example.quickconnect.model.notificationmodel.MyRequestDataList
import com.example.quickconnect.repository.NotificationRepositary
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.NotificationViewModel


class MyRequestFragment : Fragment() {

    private lateinit var binding : FragmentMyRequestBinding
    lateinit var progressBarHelper: ProgressBarHelper

    lateinit var requestPermissionAdapter : RequestPermissionAdapter
    lateinit var requestRv : RecyclerView

    lateinit var innerAdapterForRequest : InnerAdapterForRequests


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
        binding = FragmentMyRequestBinding.inflate(layoutInflater)

        Initialization()

        sharedPreferences = requireContext().getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        userToken = sharedPreferences.getString("userToken","").toString()
//        userNameMy = sharedPreferences.getString("userNameMy","").toString()
//        userName = sharedPreferences.getString("userName", "").toString()

        MyrequestUsers()
        observerMyRequestData()
//        listOfSocialRequest ()
        observerSocialRequsetData()
//        observerSocialRequsetData()

        return binding.root
    }

    fun Initialization(){

        progressBarHelper =  ProgressBarHelper(requireActivity())


        requestRv = binding.notificationRv
    }

    private fun MyrequestUsers() {
        requestViewModel.myUserRequestList("$userToken")
    }

    fun observerMyRequestData(){
        requestViewModel.observeMyRequstData().observe(viewLifecycleOwner){userRequestList ->

            try {

            userRequestListAddToAdapter(userRequestList)

            Log.e("TAG", "observerMyData: ${userRequestList}", )

            requestPermissionAdapter.onItemClick = {list ->

                userName = list.username.toString()

                // Toggle the visibility of the linearInnerRv layout
                binding.linearInnerRv.isVisible = !binding.linearInnerRv.isVisible

                if (binding.linearInnerRv.isVisible) {
                    // If the linearInnerRv is now visible, fetch and display the inner RecyclerView data
                    listOfSocialRequest()
                }
            }

            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    fun userRequestListAddToAdapter(userRequestList : MutableList<MyRequestDataList>) {
        try {
            requestPermissionAdapter = RequestPermissionAdapter(userRequestList, requireContext())
            requestRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)  // Set layout manager here
            requestRv.adapter = requestPermissionAdapter
//            requestPermissionAdapter.onAcceptClickListener = this
            // Update the existing list in the adapter
            requestPermissionAdapter.requestUserList = userRequestList
            requestPermissionAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun listOfSocialRequest (){
        requestViewModel.mySocialRequesteList("$userToken", "$userName")
    }

    fun observerSocialRequsetData(){
        requestViewModel.observeSocialRequestList().observe(viewLifecycleOwner) { socialRequestData ->
            try {
                Log.e("TAG", "observerSocialRequsetDataMy: $socialRequestData")

                socialName = socialRequestData.last().socialLink!!.name.toString()

                innerAdapterForRequest = InnerAdapterForRequests(socialRequestData, requireContext())
               binding.innerRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)  // Set layout manager for innerRequestRv
                binding.innerRecyclerView.adapter = innerAdapterForRequest

                binding.linearInnerRv.isVisible = true

//                innerAdapterForRequest.onItemClickListener = this
//                innerAdapterForRequest.onItemClickListenerForReject = this

                val notificationCountTv = requireView().findViewById<TextView>(R.id.notification_rq_tv)


//                val uniqueItemIdsCount = innerAdapterForRequest.getUniqueItemIdsCountForUser(userName)

//                notificationCountTv.text = "Requested $uniqueItemIdsCount Profile"


                val requestCount = innerAdapterForRequest.innerList.count()

                notificationCountTv.setText("Requested $requestCount Profile")

                // Update the existing list in the adapter
                innerAdapterForRequest.innerList = socialRequestData
                innerAdapterForRequest.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}