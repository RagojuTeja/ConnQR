package com.example.quickconnect.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.quickconnect.R
import com.example.quickconnect.model.notificationmodel.MyRequestDataList
import com.example.quickconnect.model.notificationmodel.SocialRequestsData
import com.example.quickconnect.viewmodels.NotificationViewModel
import com.squareup.picasso.Picasso


class ExpandbleMyRequestFragment : Fragment() {

    private lateinit var expandableListView: ExpandableListView
    private lateinit var mainListAdapter: MainListAdapter

    private val mainListItems: MutableList<MyRequestDataList> = mutableListOf()
    private val subListItems: MutableMap<Int, MutableList<SocialRequestsData>> = mutableMapOf()

    private lateinit var requestViewModel: NotificationViewModel
    private lateinit var userToken: String
    lateinit var userName : String

    private var lastExpandedPosition = -1

    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_expandble_my_request, container, false)
        expandableListView = rootView.findViewById(R.id.expandableMyRequestListView)

        // Initialize ViewModel and userToken
        requestViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        userToken = sharedPreferences.getString("userToken", "").toString()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Load main list items from main API
        loadMainListItems()
    }

    private fun loadMainListItems() {
        requestViewModel.myUserRequestList(userToken)
        requestViewModel.observeMyRequstData().observe(viewLifecycleOwner) { mainList ->
            mainListItems.clear()
            mainListItems.addAll(mainList)
            // Initialize and set the adapter for the main list
            mainListAdapter = MainListAdapter(requireContext(), mainListItems, subListItems)
            expandableListView.setAdapter(mainListAdapter)
            // Set OnClickListener for group views
            expandableListView.setOnGroupClickListener { _, _, groupPosition, _ ->
                if (lastExpandedPosition != -1 && lastExpandedPosition != groupPosition) {
                    expandableListView.collapseGroup(lastExpandedPosition)
                }
                expandableListView.expandGroup(groupPosition)
                loadSubListItems(groupPosition) // Load sub list items when a group is expanded
                lastExpandedPosition = groupPosition
                true
            }
        }
    }

    private fun loadSubListItems(groupPosition: Int) {
        userName = mainListItems[groupPosition].username.toString()
        requestViewModel.mySocialRequesteList(userToken, userName)
        requestViewModel.observeSocialRequestList()
            .observe(viewLifecycleOwner) { subList ->
                subListItems[mainListItems[groupPosition].id!!.toInt()] = subList.toMutableList()
                mainListAdapter.notifyDataSetChanged()
            }
    }

//    private fun acceptRequest(id: Int, groupPosition: Int, childPosition: Int) {
//        requestViewModel.permissionStatus("$userToken", id, "grant")
//        subListItems[mainListItems[groupPosition].id!!.toInt()]?.get(childPosition)?.isAccepted = true
//        mainListAdapter.notifyDataSetChanged()
//    }

//    private fun rejectRequest(id: Int, groupPosition: Int, childPosition: Int) {
//        requestViewModel.permissionStatus("$userToken", id, "decline")
//        subListItems[mainListItems[groupPosition].id!!.toInt()]?.removeAt(childPosition)
//        mainListAdapter.notifyDataSetChanged()
//    }

    private fun deleteItem(itemId: Int, groupPosition: Int, childPosition: Int) {
        // Call the backend API to reject the item
        requestViewModel.permissionStatus(userToken, itemId, "decline")

        // Remove the item from the list
        subListItems[mainListItems[groupPosition].id!!.toInt()]?.removeAt(childPosition)

        // Notify the adapter that the data set has changed
        mainListAdapter.notifyDataSetChanged()
    }

    inner class MainListAdapter(
        private val context: Context,
        private val mainListItems: List<MyRequestDataList>,
        private val subListItems: Map<Int, List<SocialRequestsData>>
    ) : BaseExpandableListAdapter() {

        override fun getGroupCount(): Int = mainListItems.size

        override fun getChildrenCount(groupPosition: Int): Int {
            val mainItemId = mainListItems[groupPosition].id
            return subListItems[mainItemId]?.size ?: 0
        }

        override fun getGroup(groupPosition: Int): Any = mainListItems[groupPosition]

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            val mainItemId = mainListItems[groupPosition].id
            val subList = subListItems[mainItemId]
            return if (subList != null && childPosition < subList.size) {
                subList[childPosition]
            } else {
                ""
            }
        }

        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

        override fun getChildId(groupPosition: Int, childPosition: Int): Long =
            childPosition.toLong()

        override fun hasStableIds(): Boolean = true

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

        override fun getGroupView(
            groupPosition: Int,
            isExpanded: Boolean,
            convertView: View?,
            parent: ViewGroup?
        ): View {
            var groupView = convertView
            if (groupView == null) {
                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                groupView = inflater.inflate(R.layout.notification_item_layout, parent, false)
            }

            val mainItem = getGroup(groupPosition) as MyRequestDataList
            val groupNameTextView = groupView!!.findViewById<TextView>(R.id.notification_rq_tv)
            val groupImageView = groupView.findViewById<ImageView>(R.id.notification_iv)

            Picasso.get()
                .load(mainItem.profilePic)
                .placeholder(R.drawable.profile_placeholder) // Placeholder image resource
                .error(R.drawable.profile_placeholder) // Error image resource
                .into(groupImageView)

            groupNameTextView.text = mainItem.fullName

            return groupView
        }

        override fun getChildView(
            groupPosition: Int,
            childPosition: Int,
            isLastChild: Boolean,
            convertView: View?,
            parent: ViewGroup?
        ): View {
            val subItem = getChild(groupPosition, childPosition) as? SocialRequestsData

            var childView = convertView
            if (childView == null) {
                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                childView = inflater.inflate(R.layout.social_request_list_item, parent, false)
            }

            val subItemNameTextView = childView!!.findViewById<TextView>(R.id.request_access_name_tv)
            val socialImageview = childView!!.findViewById<ImageView>(R.id.requestPerson_iv)
            val new = subItem?.message ?: ""
            subItemNameTextView.text = "$new"

            val imageUrl = subItem?.socialLink!!.category!!.icon



            try {

                val imageLoader = ImageLoader.Builder(context)
                    .components {
                        add(SvgDecoder.Factory())
                    }
                    .build()

                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .target(socialImageview)
                    .build()


                imageLoader.enqueue(request)

            } catch (e: NullPointerException) {
                e.printStackTrace()
            }


            val acceptButton = childView.findViewById<ImageView>(R.id.accept_iv)
            val rejectButton = childView.findViewById<ImageView>(R.id.reject_iv)
            val deleteButton = childView.findViewById<ImageView>(R.id.delete_icon)

//            if (subItem!!.isAccepted == true) {
//                // If the request is accepted, hide accept and reject buttons and show delete button
                acceptButton.visibility = View.GONE
                rejectButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
//                subItemNameTextView.text = "Accepted $new Request"
//            } else {
//                // If the request is not accepted, show accept and reject buttons and hide delete button
//                acceptButton.visibility = View.VISIBLE
//                rejectButton.visibility = View.VISIBLE
                deleteButton.visibility = View.GONE
//
////                acceptButton.setOnClickListener {
////                    acceptRequest(subItem.id!!, groupPosition, childPosition)
////                }
//            }

//            rejectButton.setOnClickListener {
//                rejectRequest(subItem.id!!, groupPosition, childPosition)
//            }

            deleteButton.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Call method to remove item from list and make API request
                        deleteItem(subItem.id!!, groupPosition, childPosition)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            return childView
        }
    }
}
