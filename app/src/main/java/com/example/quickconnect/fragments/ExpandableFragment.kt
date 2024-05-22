package com.example.quickconnect.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.quickconnect.R
import com.example.quickconnect.model.notificationmodel.OtherData
import com.example.quickconnect.model.notificationmodel.SocialRequestsData
import com.example.quickconnect.viewmodels.NotificationViewModel
import com.squareup.picasso.Picasso


class ExpandableFragment : Fragment() {

    private lateinit var expandableListView: ExpandableListView
    private lateinit var mainListAdapter: MainListAdapter

    private val mainListItems: MutableList<OtherData> = mutableListOf()
    private val subListItems: MutableMap<Int, MutableList<SocialRequestsData>> = mutableMapOf()

    private lateinit var requestViewModel: NotificationViewModel
    private lateinit var userToken: String

    private var lastExpandedPosition = -1

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_expandable, container, false)
        expandableListView = rootView.findViewById(R.id.expandableListView)

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
        requestViewModel.otherUserRequestList(userToken)
        requestViewModel.observeOtherRequstUserData().observe(viewLifecycleOwner) { mainList ->
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
        val userName = mainListItems[groupPosition].username.toString()
        requestViewModel.otherSocialRequesteList(userToken, userName)
        requestViewModel.observeOtherSocialRequestList()
            .observe(viewLifecycleOwner) { subList ->
                subListItems[mainListItems[groupPosition].id!!.toInt()] = subList.toMutableList()
                mainListAdapter.notifyDataSetChanged()
            }
    }

    private fun acceptRequest(id: Int, groupPosition: Int, childPosition: Int) {
        requestViewModel.permissionStatus("$userToken", id, "grant")
        subListItems[mainListItems[groupPosition].id!!.toInt()]?.get(childPosition)?.isAccepted = true
        mainListAdapter.notifyDataSetChanged()
    }

    private fun rejectRequest(id: Int, groupPosition: Int, childPosition: Int) {
        requestViewModel.permissionStatus("$userToken", id, "decline")
        subListItems[mainListItems[groupPosition].id!!.toInt()]?.removeAt(childPosition)
        mainListAdapter.notifyDataSetChanged()
    }

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
        private val mainListItems: List<OtherData>,
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

            val mainItem = getGroup(groupPosition) as OtherData
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
            val new = subItem?.socialLink?.name ?: ""
            subItemNameTextView.text = "Requested $new"

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

            if (subItem!!.isAccepted == true) {
                // If the request is accepted, hide accept and reject buttons and show delete button
                acceptButton.visibility = View.GONE
                rejectButton.visibility = View.GONE
                deleteButton.visibility = View.VISIBLE
                subItemNameTextView.text = "Accepted $new Request"
            } else {
                // If the request is not accepted, show accept and reject buttons and hide delete button
                acceptButton.visibility = View.VISIBLE
                rejectButton.visibility = View.VISIBLE
                deleteButton.visibility = View.GONE

                acceptButton.setOnClickListener {
                    acceptRequest(subItem.id!!, groupPosition, childPosition)
                }
            }

            rejectButton.setOnClickListener {
                rejectRequest(subItem.id!!, groupPosition, childPosition)
            }

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












//class ExpandableFragment : Fragment() {
//
//    private lateinit var expandableListView: ExpandableListView
//    private lateinit var mainListAdapter: MainListAdapter
//
//    private val mainListItems: MutableList<OtherData> = mutableListOf()
//    private val subListItems: MutableMap<Int, MutableList<SocialRequestsData>> = mutableMapOf()
//
//    private lateinit var requestViewModel: NotificationViewModel
//    private lateinit var userToken: String
//    lateinit var userName : String
//
//    private var lastExpandedPosition = -1
//
//    private lateinit var sharedPreferences: SharedPreferences
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val rootView = inflater.inflate(R.layout.fragment_expandable, container, false)
//        expandableListView = rootView.findViewById(R.id.expandableListView)
//
//        // Initialize ViewModel and userToken
//        requestViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
//        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
//        userToken = sharedPreferences.getString("userToken", "").toString()
//
//        return rootView
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        // Load main list items from main API
//        loadMainListItems()
//    }
//
//    private fun loadMainListItems() {
//        requestViewModel.otherUserRequestList(userToken)
//        requestViewModel.observeOtherRequstUserData().observe(viewLifecycleOwner) { mainList ->
//            mainListItems.clear()
//            mainListItems.addAll(mainList)
//            // Initialize and set the adapter for the main list
//            mainListAdapter = MainListAdapter(requireContext(), mainListItems, subListItems)
//            expandableListView.setAdapter(mainListAdapter)
//            // Set OnClickListener for group views
//            expandableListView.setOnGroupClickListener { _, _, groupPosition, _ ->
//                if (expandableListView.isGroupExpanded(groupPosition)) {
//                    expandableListView.collapseGroup(groupPosition)
//                } else {
//                    if (lastExpandedPosition != -1 && lastExpandedPosition != groupPosition) {
//                        expandableListView.collapseGroup(lastExpandedPosition)
//                    }
//                    expandableListView.expandGroup(groupPosition)
//                    userName = mainListItems[groupPosition].username.toString()
//                    loadSubListItems()
//                }
//                lastExpandedPosition = groupPosition
//                true
//            }
//        }
//    }
//
//    private fun loadSubListItems() {
//        subListItems.clear()
//        mainListItems.forEach { mainItem ->
//            requestViewModel.otherSocialRequesteList(userToken, userName)
//            requestViewModel.observeOtherSocialRequestList()
//                .observe(viewLifecycleOwner) { subList ->
//                    subListItems[mainItem.id!!.toInt()] = subList.toMutableList()
//                    mainListAdapter.notifyDataSetChanged()
//                }
//        }
//    }
//
//    private fun acceptRequest(id: Int) {
//        requestViewModel.permissionStatus("$userToken", id, "grant")
//        mainListAdapter.notifyDataSetChanged()
//    }
//
//    private fun deleteItemFromBackend(itemId: Int) {
//        requestViewModel.permissionStatus("$userToken", itemId, "decline")
//    }
//
//    private fun deleteItem(itemId: Int, groupPosition: Int, childPosition: Int) {
//        // Call the backend API to reject the item
//        requestViewModel.permissionStatus(userToken, itemId, "decline")
//
//        // Remove the item from the list
//        subListItems[groupPosition]?.removeAt(childPosition)
//
//        loadSubListItems()
//
//        // Notify the adapter that the data set has changed
//        mainListAdapter.notifyDataSetChanged()
//    }
//
//
//
//
//
//
//
//
//
//
//    inner class MainListAdapter(
//        private val context: Context,
//        private val mainListItems: List<OtherData>,
//        private val subListItems: Map<Int, List<SocialRequestsData>>
//    ) : BaseExpandableListAdapter() {
//
//        override fun getGroupCount(): Int = mainListItems.size
//
//        override fun getChildrenCount(groupPosition: Int): Int {
//            val mainItemId = mainListItems[groupPosition].id
//            return subListItems[mainItemId]?.size ?: 0
//        }
//
//        override fun getGroup(groupPosition: Int): Any = mainListItems[groupPosition]
//
//        override fun getChild(groupPosition: Int, childPosition: Int): Any {
//            val mainItemId = mainListItems[groupPosition].id
//            val subList = subListItems[mainItemId]
//            return if (subList != null && childPosition < subList.size) {
//                subList[childPosition]
//            } else {
//                ""
//            }
//        }
//
//        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
//
//        override fun getChildId(groupPosition: Int, childPosition: Int): Long =
//            childPosition.toLong()
//
//        override fun hasStableIds(): Boolean = true
//
//        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
//
//        override fun getGroupView(
//            groupPosition: Int,
//            isExpanded: Boolean,
//            convertView: View?,
//            parent: ViewGroup?
//        ): View {
//            var groupView = convertView
//            if (groupView == null) {
//                val inflater =
//                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//                groupView = inflater.inflate(R.layout.notification_item_layout, parent, false)
//            }
//
//            val mainItem = getGroup(groupPosition) as OtherData
//            val groupNameTextView = groupView!!.findViewById<TextView>(R.id.notification_rq_tv)
//            groupNameTextView.text = mainItem.fullName
//
//            return groupView
//        }
//
//        override fun getChildView(
//            groupPosition: Int,
//            childPosition: Int,
//            isLastChild: Boolean,
//            convertView: View?,
//            parent: ViewGroup?
//        ): View {
//            val subItem = getChild(groupPosition, childPosition) as? SocialRequestsData
//
//            var childView = convertView
//            if (childView == null) {
//                val inflater =
//                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//                childView = inflater.inflate(R.layout.social_request_list_item, parent, false)
//            }
//
//            val subItemNameTextView =
//                childView!!.findViewById<TextView>(R.id.request_access_name_tv)
//            val new = subItem?.socialLink?.name ?: ""
//            subItemNameTextView.text = "Requested $new"
//
//            val acceptButton = childView.findViewById<ImageView>(R.id.accept_iv)
//            val rejectButton = childView.findViewById<ImageView>(R.id.reject_iv)
//            val deleteButton = childView.findViewById<ImageView>(R.id.delete_icon)
//
//            if (subItem!!.isAccepted == true) {
//                // If the request is accepted, hide accept and reject buttons and show delete button
//                acceptButton.visibility = View.GONE
//                rejectButton.visibility = View.GONE
//                deleteButton.visibility = View.VISIBLE
//                subItemNameTextView.text = "Accepted $new Request"
//            } else {
//                // If the request is not accepted, show accept and reject buttons and hide delete button
//                acceptButton.visibility = View.VISIBLE
//                rejectButton.visibility = View.VISIBLE
//                deleteButton.visibility = View.GONE
//
//                acceptButton.setOnClickListener {
//                    acceptRequest(subItem!!.id!!)
//                    loadSubListItems()
//                    // Update the text when accept button is clicked
//                    // Hide accept and reject buttons and show delete button
////            acceptButton.visibility = View.GONE
////            rejectButton.visibility = View.GONE
////            deleteButton.visibility = View.VISIBLE
//                    notifyDataSetChanged()
//                }
//            }
//
//
//            rejectButton.setOnClickListener {
//                AlertDialog.Builder(context)
//                    .setTitle("Reject Item")
//                    .setMessage("Are you sure you want to Reject this Profile Request?")
//                    .setPositiveButton("Yes") { _, _ ->
//
//                        deleteItem(subItem!!.id!!, groupPosition, childPosition)
//                        loadSubListItems()
//                        notifyDataSetChanged()
//                        // Show the delete button and update the UI
////                        acceptButton.visibility = View.GONE
////                        rejectButton.visibility = View.GONE
////                        deleteButton.visibility = View.VISIBLE
////                        subItemNameTextView.text = "Rejected $new Request"
//                    }
//                    .setNegativeButton("No", null)
//                    .show()
//            }
//
//            deleteButton.setOnClickListener {
//                AlertDialog.Builder(context)
//                    .setTitle("Delete Item")
//                    .setMessage("Are you sure you want to delete?")
//                    .setPositiveButton("Yes") { _, _ ->
//                        // Call method to remove item from list and make API request
//                        deleteItem(subItem!!.id!!, groupPosition, childPosition)
//                        loadSubListItems()
//                        notifyDataSetChanged()
//                    }
//                    .setNegativeButton("No", null)
//                    .show()
//
//                notifyDataSetChanged()
//            }
//
//
//
//
//            return childView
//        }
//
//
//        fun rejectAccess(groupPosition: Int, childPosition: Int) {
//            // Remove item from the list
//            // You may need to implement this method according to your list structure
//            // For example:
//            // subListItems[groupPosition]?.removeAt(childPosition)
//        }
//    }
//}











//class ExpandableFragment : Fragment() {
//
//    private lateinit var expandableListView: ExpandableListView
//    private lateinit var mainListAdapter: MainListAdapter
//
//    private val mainListItems: MutableList<OtherData> = mutableListOf()
//    private val subListItems: MutableMap<Int, MutableList<SocialRequestsData>> = mutableMapOf()
//
//    private lateinit var requestViewModel: NotificationViewModel
//    private lateinit var userToken: String
//
//    private var lastExpandedPosition = -1
//
//
//    //    lateinit var userName : String
//    lateinit var sharedPreferences: SharedPreferences
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val rootView = inflater.inflate(R.layout.fragment_expandable, container, false)
//        expandableListView = rootView.findViewById(R.id.expandableListView)
//
//        // Initialize ViewModel and userToken
//        requestViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
//        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
//        userToken = sharedPreferences.getString("userToken", "").toString()
//
////        Log.e("TAG", "userName: $userName", )
//        Log.e("TAG", "userToken: $userToken",)
//
//        return rootView
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        // Load main list items from main API
//        loadMainListItems()
//
//    }
//
//    private fun loadMainListItems() {
//        requestViewModel.otherUserRequestList(userToken)
//        requestViewModel.observeOtherRequstUserData().observe(viewLifecycleOwner) { mainList ->
//            mainListItems.clear()
//            mainListItems.addAll(mainList)
//            // Initialize and set the adapter for the main list
//            mainListAdapter = MainListAdapter(requireContext(), mainListItems, subListItems)
//            expandableListView.setAdapter(mainListAdapter)
//            // Set OnClickListener for group views
//            expandableListView.setOnGroupClickListener { _, _, groupPosition, _ ->
//                if (expandableListView.isGroupExpanded(groupPosition)) {
//                    expandableListView.collapseGroup(groupPosition)
//                } else {
//                    // Collapse the last expanded group if it's not the same as the newly expanded one
//                    if (lastExpandedPosition != -1 && lastExpandedPosition != groupPosition) {
//                        expandableListView.collapseGroup(lastExpandedPosition)
//                    }
//                    expandableListView.expandGroup(groupPosition)
//                    val userName = mainListItems[groupPosition].username
//                    loadSubListItems(userName!!)
//                }
//                // Update the last expanded position
//                lastExpandedPosition = groupPosition
//                true // Return true to consume the click event
//            }
//        }
//    }
//
//
//    private fun loadSubListItems(userName: String) {
//        // Clear the subListItems map before loading new sub-items
//        subListItems.clear()
//
//        // Iterate through each main item to load its corresponding sub items
//        mainListItems.forEach { mainItem ->
//            requestViewModel.otherSocialRequesteList(userToken, userName)
//            requestViewModel.observeOtherSocialRequestList()
//                .observe(viewLifecycleOwner) { subList ->
//                    // Add the sub list items to the map with the main item's ID as the key
//                    subListItems[mainItem.id!!.toInt()] = subList.toMutableList()
//                    // Notify the adapter that the data set has changed
//                    mainListAdapter.notifyDataSetChanged()
//                }
//        }
//    }
//
//
//
//    inner class MainListAdapter(
//        private val context: Context,
//        private val mainListItems: List<OtherData>,
//        private val subListItems: Map<Int, List<SocialRequestsData>>
//    ) : BaseExpandableListAdapter() {
//
//        override fun getGroupCount(): Int = mainListItems.size
//
//        override fun getChildrenCount(groupPosition: Int): Int {
//            val mainItemId = mainListItems[groupPosition].id
//            return subListItems[mainItemId]?.size ?: 0
//        }
//
//        override fun getGroup(groupPosition: Int): Any = mainListItems[groupPosition]
//
//        override fun getChild(groupPosition: Int, childPosition: Int): Any {
//            val mainItemId = mainListItems[groupPosition].id
//            val subList = subListItems[mainItemId]
//            return if (subList != null && childPosition < subList.size) {
//                subList[childPosition]
//            } else {
//                "" // Return an empty string or handle the case when subList is null or childPosition is out of bounds
//            }
//        }
//
//        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
//
//        override fun getChildId(groupPosition: Int, childPosition: Int): Long =
//            childPosition.toLong()
//
//        override fun hasStableIds(): Boolean = true
//
//        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
//
//        override fun getGroupView(
//            groupPosition: Int,
//            isExpanded: Boolean,
//            convertView: View?,
//            parent: ViewGroup?
//        ): View {
//            var groupView = convertView
//            if (groupView == null) {
//                val inflater =
//                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//                groupView = inflater.inflate(R.layout.notification_item_layout, parent, false)
//            }
//
//            val mainItem = getGroup(groupPosition) as OtherData
//
//            // Customize the group view
//            val groupNameTextView = groupView!!.findViewById<TextView>(R.id.notification_rq_tv)
//            groupNameTextView.text = mainItem.fullName
//
//            return groupView
//        }
//
//        override fun getChildView(
//            groupPosition: Int,
//            childPosition: Int,
//            isLastChild: Boolean,
//            convertView: View?,
//            parent: ViewGroup?
//        ): View {
//            val subItem = getChild(groupPosition, childPosition) as? SocialRequestsData
//
//            var childView = convertView
//            if (childView == null) {
//                val inflater =
//                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//                childView = inflater.inflate(R.layout.social_request_list_item, parent, false)
//            }
//
//            // Customize the child view
//            val subItemNameTextView =
//                childView!!.findViewById<TextView>(R.id.request_access_name_tv)
//            subItemNameTextView.text = subItem?.socialLink?.name ?: ""
//
//            return childView
//        }
//    }
//}

//class ExpandableFragment : Fragment() {
//
//    private lateinit var expandableListView: ExpandableListView
//    private lateinit var adapter: MyExpandableListAdapter
//    private lateinit var expandableListDetail: HashMap<String, List<Pair<String, String>>>
//
//    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var userToken: String
//    private lateinit var userName: String
//
//    private val requestViewModel: NotificationViewModel by lazy {
//        val requestViewModelFactory = NotificationRepositary()
//        ViewModelProvider(this, requestViewModelFactory)[NotificationViewModel::class.java]
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_expandable, container, false)
//
//        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
//        userToken = sharedPreferences.getString("userToken", "").toString()
//        userName = sharedPreferences.getString("userName", "").toString()
//
//        expandableListView = view.findViewById(R.id.expandableListView)
//
//        // Initialize the expandable list detail
//        expandableListDetail = HashMap()
//
//        // Fetch and observe group items
//        observeGroupItems()
//
//        // Set up the OnGroupClickListener
//        expandableListView.setOnGroupClickListener { parent, v, groupPosition, id ->
//            if (expandableListView.isGroupExpanded(groupPosition)) {
//                // Collapse the group if it's already expanded
//                expandableListView.collapseGroup(groupPosition)
//            } else {
//                // Expand the group if it's collapsed
//                expandableListView.expandGroup(groupPosition)
//            }
//            // Return true to indicate that the click event has been consumed
//            true
//        }
//
//        return view
//    }
//
//    private fun observeGroupItems() {
//        // Fetch group items (OtherRequestUsers)
//        requestViewModel.otherUserRequestList(userToken)
//
//        // Observe group items
//        requestViewModel.observeOtherRequstUserData().observe(viewLifecycleOwner) { otheruserRequestList ->
//            try {
//                // Convert API response to ExpandableListView data format
//                val convertedListDetail = convertToExpandableList(otheruserRequestList)
//
//                // Update the expandable list detail with the converted data
//                expandableListDetail.putAll(convertedListDetail)
//
//                // Set up adapter with the converted data if it's not initialized yet
//                if (!::adapter.isInitialized) {
//                    adapter = MyExpandableListAdapter(requireContext(), expandableListDetail)
//                    expandableListView.setAdapter(adapter)
//                } else {
//                    // Update adapter data if it's already initialized
//                    adapter.updateData(expandableListDetail)
//                }
//
//                // Fetch and observe child items for each group
//                for (item in otheruserRequestList) {
//                    fetchAndObserveChildItems()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private fun fetchAndObserveChildItems() {
//        // Fetch child items using the API method otherSocialRequesteList for the specific groupId
//        requestViewModel.otherSocialRequesteList(userToken, userName)
//
//        // Observe the API response using the observeOtherSocialRequestList method
//        requestViewModel.observeOtherSocialRequestList().observe(viewLifecycleOwner) { socialRequestData ->
//            try {
//                // Update the adapter with the fetched child items
//                adapter.updateChildItems(socialRequestData)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private fun convertToExpandableList(otheruserRequestList: List<OtherData>): HashMap<String, List<Pair<String, String>>> {
//        val convertedListDetail = HashMap<String, List<Pair<String, String>>>()
//
//        // Iterate through each group item
//        for (item in otheruserRequestList) {
//            val group = item.username ?: "" // Using username as group name, replace it with the actual property you want to use
//
//            // Add the group with empty child items initially
//            convertedListDetail[group] = emptyList()
//        }
//
//        return convertedListDetail
//    }
//}


//    private fun fetchAndObserveChildItems(groupId: Int?) {
//        // Fetch child items using the API method otherSocialRequesteList
//        requestViewModel.otherSocialRequesteList(userToken, userName)
//
//        // Observe the API response using the observeOtherSocialRequestList method
//        requestViewModel.observeOtherSocialRequestList().observe(viewLifecycleOwner) { socialRequestData ->
//            try {
//                // Update the adapter with the fetched child items
//                adapter.updateChildItems(groupId, socialRequestData)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

//}


//class ExpandableFragment : Fragment() {
//
//    private lateinit var expandableListView: ExpandableListView
//    private lateinit var adapter: MyExpandableListAdapter
//
//    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var userToken: String
//    private lateinit var userName: String
//
//    private val requestViewModel: NotificationViewModel by lazy {
//        val requestViewModelFactory = NotificationRepositary()
//        ViewModelProvider(this, requestViewModelFactory)[NotificationViewModel::class.java]
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_expandable, container, false)
//
//        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
//        userToken = sharedPreferences.getString("userToken", "").toString()
//        userName = sharedPreferences.getString("userName", "").toString()
//
//        expandableListView = view.findViewById(R.id.expandableListView)
//
//        // Fetch and observe group items
//        observeGroupItems()
//
//        return view
//    }
//
//    private fun observeGroupItems() {
//        // Fetch group items (OtherRequestUsers)
//        requestViewModel.otherUserRequestList(userToken)
//
//        // Observe group items
//        requestViewModel.observeOtherRequstUserData().observe(viewLifecycleOwner) { otheruserRequestList ->
//            try {
//                // Convert API response to ExpandableListView data format
//                val expandableListDetail = convertToExpandableList(otheruserRequestList)
//
//                // Set up adapter with the converted data
//                adapter = MyExpandableListAdapter(requireContext(), expandableListDetail)
//                expandableListView.setAdapter(adapter)
//
//                // Fetch and observe child items for each group
//                for (item in otheruserRequestList) {
//                    fetchAndObserveChildItems(item.id)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private fun convertToExpandableList(otheruserRequestList: List<OtherData>): HashMap<String, List<Pair<String, String>>> {
//        val expandableListDetail = HashMap<String, List<Pair<String, String>>>()
//
//        // Iterate through each group item
//        for (item in otheruserRequestList) {
//            val group = item.username ?: "" // Using username as group name, replace it with the actual property you want to use
//
//            // Add the group with empty child items initially
//            expandableListDetail[group] = emptyList()
//        }
//
//        return expandableListDetail
//    }
//
//    private fun fetchAndObserveChildItems(groupId: Int?) {
//        // Fetch child items using the API method otherSocialRequesteList
//        requestViewModel.otherSocialRequesteList(userToken, userName)
//
//        // Observe the API response using the observeOtherSocialRequestList method
//        requestViewModel.observeOtherSocialRequestList().observe(viewLifecycleOwner) { socialRequestData ->
//            try {
//                // Update the adapter with the fetched child items
//                adapter.updateChildItems(groupId, socialRequestData)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//}





//class ExpandableFragment : Fragment() {
//
//
//    private lateinit var expandableListView: ExpandableListView
//    private lateinit var adapter: MyExpandableListAdapter
//
//    private lateinit var binding: FragmentOtherRequestBinding
//    private lateinit var progressBarHelper: ProgressBarHelper
//    private lateinit var otherRequestPermissionAdapter: OtherRequestPermissionAdapter
//    private lateinit var otherInnerAdapterForRequests: OtherInnerAdapterForRequests
//    private lateinit var otherRequestRv: RecyclerView
//    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var userToken: String
//    private lateinit var socialName: String
//    private lateinit var userName: String
//
//    private val requestViewModel: NotificationViewModel by lazy {
//        val requestViewModelFactory = NotificationRepositary()
//        ViewModelProvider(this, requestViewModelFactory)[NotificationViewModel::class.java]
//    }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_expandable, container, false)
//
//        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
//        userToken = sharedPreferences.getString("userToken", "").toString()
//
//        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
//        userName = sharedPreferences.getString("userName", "").toString()
//
//        OtherRequestUsers()
//        observeApiData()
//        observerOthersRequestData()
//        listOfotherSocialRequest()
////        observerOtherSocialRequsetData()
////        observeAcceptRequest()
////
//        expandableListView = view.findViewById(R.id.expandableListView)
////        adapter = MyExpandableListAdapter(requireContext(), initData())
////        expandableListView.setAdapter(adapter)
//
//        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
//            val selectedItem = adapter.getChild(groupPosition, childPosition) as String
//            Toast.makeText(requireContext(), "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
//            false
//        }
//
//        return view
//    }
//
//
//    private fun OtherRequestUsers() {
//        requestViewModel.otherUserRequestList("$userToken")
//    }
//
//
//    fun listOfotherSocialRequest(): MutableList<SocialRequestsData> {
//        requestViewModel.otherSocialRequesteList("$userToken", "$userName")
//        return mutableListOf()
//    }
//
//    private fun observeApiData() {
//        // Observe your API response using ViewModel
//        requestViewModel.observeOtherRequstUserData()
//            .observe(viewLifecycleOwner) { otheruserRequestList ->
//                try {
//                    // Convert API response to ExpandableListView data format
//                    val expandableListDetail = convertToExpandableList(otheruserRequestList)
//
//                    // Set up adapter with the converted data
//                    adapter = MyExpandableListAdapter(requireContext(), expandableListDetail,expandableListView)
//                    expandableListView.setAdapter(adapter)
//
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//    }
//
//
//    fun observerOthersRequestData() {
//        requestViewModel.observeOtherRequstUserData()
//            .observe(viewLifecycleOwner) { otheruserRequestList ->
//                try {
//
//                    // Convert API response to ExpandableListView data format
//                    val expandableListDetail = convertToExpandableList(otheruserRequestList)
//
//                    // Set up adapter with the converted data
//                    adapter = MyExpandableListAdapter(requireContext(), expandableListDetail, expandableListView)
//                    expandableListView.setAdapter(adapter)
//
//
////                otherUserRequestListAddToAdapter(otheruserRequestList)
////                otherRequestPermissionAdapter.onItemClick = { list ->
////                    userName = list.username.toString()
////
////                    // Update innerRecyclerView data
////                    otherInnerAdapterForRequests.innerList = listOfotherSocialRequest()
////                    otherInnerAdapterForRequests.notifyDataSetChanged()
////
////                    // Update visibility of innerRecyclerView
////                    for (item in otheruserRequestList) {
////                        item.isInnerVisible = (item == list)
////                    }
////                    otherRequestPermissionAdapter.notifyDataSetChanged()
////                }
//
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//    }
//
//
//    private fun convertToExpandableList(otheruserRequestList: List<OtherData>): HashMap<String, List<Pair<String, String>>> {
//        val expandableListDetail = HashMap<String, List<Pair<String, String>>>()
//
//        // Iterate through each group item
//        for (item in otheruserRequestList) {
//            val group = item.username ?: "" // Using username as group name, replace it with the actual property you want to use
//
//            // Fetch child items for the current group (using a separate API call or method)
//            val childItems = fetchChildItems(item.id) // Assuming you have a method to fetch child items based on the group id
//
//            // Add the child items to the expandable list detail
//            expandableListDetail[group] = childItems
//        }
//
//        return expandableListDetail
//    }
//
//
//    private fun fetchChildItems(groupId: Int?): List<Pair<String, String>> {
//        val childItems = mutableListOf<Pair<String, String>>()
//
//        // Fetch child items using the API method otherSocialRequesteList
//        requestViewModel.otherSocialRequesteList("$userToken", "$userName")
//
//        // Observe the API response using the observeOtherSocialRequestList method
//        requestViewModel.observeOtherSocialRequestList().observe(viewLifecycleOwner) { socialRequestData ->
//            try {
//                // Assuming socialRequestData contains the list of SocialRequestsData
//                // Iterate through the fetched data and convert each item to a Pair<String, String>
//                for (socialRequest in socialRequestData) {
//                    // Assuming the first element of the pair represents the child item's identifier (e.g., id)
//                    val childId = socialRequest.id?.toString() ?: ""
//                    // Assuming the second element of the pair represents the child item's display value (e.g., message)
//                    val childValue = socialRequest.message ?: "" // Replace this with the actual property you want to use
//
//                    // Add the Pair<String, String> to the childItems list
//                    childItems.add(Pair(childId, childValue))
//                }
//
//                // Update the inner adapter with the fetched child items
//                otherInnerAdapterForRequests.innerList = socialRequestData
//                otherInnerAdapterForRequests.notifyDataSetChanged()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        // Return an empty list initially (the actual child items will be updated asynchronously)
//        return childItems
//    }
//
//}
//
//
//

//class ExpandableFragment : Fragment() {
//
//    private lateinit var expandableListView: ExpandableListView
//    private lateinit var adapter: MyExpandableListAdapter
//    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var userToken: String
//    private lateinit var userName: String
//    private val requestViewModel: NotificationViewModel by lazy {
//        val requestViewModelFactory = NotificationRepositary()
//        ViewModelProvider(this, requestViewModelFactory)[NotificationViewModel::class.java]
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_expandable, container, false)
//        expandableListView = view.findViewById(R.id.expandableListView)
//
//        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
//        userToken = sharedPreferences.getString("userToken", "").toString()
//        userName = sharedPreferences.getString("userName", "").toString()
//
//        OtherRequestUsers()
//        listOfotherSocialRequest()
//        observeApiData()
//
//        expandableListView.setOnGroupExpandListener { groupPosition ->
//            val group = adapter.getGroup(groupPosition)
//            fetchSocialRequestsData(group)
//        }
//
//        return view
//    }
//
//    private fun OtherRequestUsers() {
//        requestViewModel.otherUserRequestList("$userToken")
//    }
//
//    fun listOfotherSocialRequest(): MutableList<SocialRequestsData> {
//        requestViewModel.otherSocialRequesteList("$userToken", "$userName")
//        return mutableListOf()
//    }
//
//    private fun observeApiData() {
//        requestViewModel.observeOtherRequstUserData().observe(viewLifecycleOwner) { otheruserRequestList ->
//            try {
//                val expandableListDetail = convertToExpandableList(otheruserRequestList)
//                adapter = MyExpandableListAdapter(requireContext(), expandableListDetail)
//                expandableListView.setAdapter(adapter)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private fun fetchSocialRequestsData(group: String) {
//        requestViewModel.observeOtherSocialRequestList().observe(viewLifecycleOwner) { socialRequestsList ->
//            val childList = socialRequestsList.filter { it.socialLink!!.name == group }
//            adapter.updateChildData(group, childList)
//        }
//    }
//
//    private fun convertToExpandableList(groupList: List<OtherData>): HashMap<String, List<Pair<String, String>>> {
//        val expandableListDetail = HashMap<String, List<Pair<String, String>>>()
//        for (groupItem in groupList) {
//            val group = groupItem.username ?: ""
//            expandableListDetail[group] = emptyList() // Initialize with empty list
//        }
//        return expandableListDetail
//    }
//}

//class ExpandableFragment : Fragment() {
//
//
//    private lateinit var expandableListView: ExpandableListView
//    private lateinit var adapter: MyExpandableListAdapter
//
//    private lateinit var binding: FragmentOtherRequestBinding
//    private lateinit var progressBarHelper: ProgressBarHelper
//    private lateinit var otherRequestPermissionAdapter: OtherRequestPermissionAdapter
//    private lateinit var otherInnerAdapterForRequests: OtherInnerAdapterForRequests
//    private lateinit var otherRequestRv: RecyclerView
//    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var userToken: String
//    private lateinit var socialName: String
//    private lateinit var userName: String
//
//    private val requestViewModel: NotificationViewModel by lazy {
//        val requestViewModelFactory = NotificationRepositary()
//        ViewModelProvider(this, requestViewModelFactory)[NotificationViewModel::class.java]
//    }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_expandable, container, false)
//
//        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
//        userToken = sharedPreferences.getString("userToken", "").toString()
//
//        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
//        userName = sharedPreferences.getString("userName", "").toString()
//
//        OtherRequestUsers()
//        observeApiData()
//        observerOthersRequestData()
//        listOfotherSocialRequest()
////        observerOtherSocialRequsetData()
////        observeAcceptRequest()
////
//        expandableListView = view.findViewById(R.id.expandableListView)
////        adapter = MyExpandableListAdapter(requireContext(), initData())
////        expandableListView.setAdapter(adapter)
//
//        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
//            val selectedItem = adapter.getChild(groupPosition, childPosition) as String
//            Toast.makeText(requireContext(), "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
//            false
//        }
//
//        return view
//    }
//
//    private fun OtherRequestUsers() {
//        requestViewModel.otherUserRequestList("$userToken")
//    }
//
//
//    fun listOfotherSocialRequest(): MutableList<SocialRequestsData> {
//        requestViewModel.otherSocialRequesteList("$userToken", "$userName")
//        return mutableListOf()
//    }
//
//    private fun observeApiData() {
//        // Observe your API response using ViewModel
//        requestViewModel.observeOtherRequstUserData().observe(viewLifecycleOwner) { otheruserRequestList ->
//            try {
//                // Convert API response to ExpandableListView data format
//                val expandableListDetail = convertToExpandableList(otheruserRequestList)
//
//                // Set up adapter with the converted data
//                adapter = MyExpandableListAdapter(requireContext(), expandableListDetail)
//                expandableListView.setAdapter(adapter)
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//
//    fun observerOthersRequestData() {
//        requestViewModel.observeOtherRequstUserData().observe(viewLifecycleOwner) { otheruserRequestList ->
//            try {
//
//                // Convert API response to ExpandableListView data format
//                val expandableListDetail = convertToExpandableList(otheruserRequestList)
//
//                // Set up adapter with the converted data
//                adapter = MyExpandableListAdapter(requireContext(), expandableListDetail)
//                expandableListView.setAdapter(adapter)
//
//
//
////                otherUserRequestListAddToAdapter(otheruserRequestList)
////                otherRequestPermissionAdapter.onItemClick = { list ->
////                    userName = list.username.toString()
////
////                    // Update innerRecyclerView data
////                    otherInnerAdapterForRequests.innerList = listOfotherSocialRequest()
////                    otherInnerAdapterForRequests.notifyDataSetChanged()
////
////                    // Update visibility of innerRecyclerView
////                    for (item in otheruserRequestList) {
////                        item.isInnerVisible = (item == list)
////                    }
////                    otherRequestPermissionAdapter.notifyDataSetChanged()
////                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

//
//    private fun convertToExpandableList(otheruserRequestList: MutableList<OtherData>): HashMap<String, List<Pair<String, String>>> {
//        val expandableListDetail = HashMap<String, List<Pair<String, String>>>()
//
//        for (item in otheruserRequestList) {
//            val group = item.username ?: "" // Using username as group name, replace it with the actual property you want to use
//            val groupImageUrl = item.profilePic ?: "" // Replace groupImageUrl with the actual property for the image URL
//
//            // Initialize an empty list to hold child items
//            val childItems = mutableListOf<Pair<String, String>>()
//
//            // Add all child items for the current group
//            // Here, you can add multiple child items based on your data structure
//            // For example, if you have a list of URLs for each group, you can add them all here
//            // Make sure to replace "item.fullName" and "item.profilePic" with the actual properties you want to use
//            childItems.add(Pair(item.fullName ?: "", item.profilePic ?: ""))
//
//            // Add the child items to the expandable list detail
//            expandableListDetail[group] = childItems
//        }
//
//        return expandableListDetail
//    }
//


//    private fun convertToExpandableList(otheruserRequestList: MutableList<OtherData>): HashMap<String, List<Pair<String, String>>> {
//        val expandableListDetail = HashMap<String, List<Pair<String, String>>>()
//
//        // Iterate through your API response and map it to ExpandableListView data format
//        for (item in otheruserRequestList) {
//            val group = item.username ?: "" // Using username as group name, replace it with the actual property you want to use
//            val groupImageUrl = item.profilePic ?: "" // Replace groupImageUrl with the actual property for the image URL
//
//            // Initialize an empty list to hold child items
//            val childItems = mutableListOf<Pair<String, String>>()
//
//            // Add all child items for the current group
//            // Here, you can add multiple child items based on your data structure
//            // For example, if you have a list of URLs for each group, you can add them all here
//            // Make sure to replace "item.fullName" and "item.profilePic" with the actual properties you want to use
//            childItems.add(Pair(item.fullName ?: "", item.profilePic ?: ""))
//
//            // Add the child items to the expandable list detail
//            expandableListDetail[group] = childItems
//        }
//
//        return expandableListDetail
//    }
//



//    private fun convertToExpandableList(otheruserRequestList: MutableList<OtherData>): HashMap<String, List<Pair<String, String>>> {
//        val expandableListDetail = HashMap<String, List<Pair<String, String>>>()
//
//        // Iterate through your API response and map it to ExpandableListView data format
//        for (item in otheruserRequestList) {
//            val group = item.username ?: "" // Using username as group name, replace it with the actual property you want to use
//            val groupImageUrl = item.profilePic ?: "" // Replace groupImageUrl with the actual property for the image URL
//
//            val childItems = listOf(Pair(item.fullName ?: "", groupImageUrl)) // Using fullName and profilePic as child items
//            expandableListDetail[group] = childItems
//        }
//
//        return expandableListDetail
//    }


//    private fun convertToExpandableList(otheruserRequestList: MutableList<OtherData>): HashMap<String, List<Pair<String, String>>> {
//        val expandableListDetail = HashMap<String, List<Pair<String, String>>>()
//
//        // Iterate through your API response and map it to ExpandableListView data format
//        for (item in otheruserRequestList) {
//            val group = item.username ?: "" // Using username as group name, replace it with the actual property you want to use
//            val groupImageUrl = item.profilePic ?: "" // Replace groupImageUrl with the actual property for the image URL
//
//            val childItems = listOf(Pair(item.fullName ?: "", groupImageUrl)) // Using fullName and profilePic as child items
//            expandableListDetail[group] = childItems
//        }
//
//        return expandableListDetail
//    }



//    private fun convertToExpandableList(otheruserRequestList: MutableList<OtherData>): HashMap<String, List<Pair<String, String>>> {
//        val expandableListDetail = HashMap<String, List<Pair<String, String>>>()
//
//
//
//        // Iterate through your API response and map it to ExpandableListView data format
//        for (item in otheruserRequestList) {
//            val group = item.username ?: "" // Using username as group name, replace it with the actual property you want to use
//            val groupImageUrl = item.profilePic ?: "" // Replace groupImageUrl with the actual property for the image URL
//            val childItems = listOf(Pair(item.fullName ?: "", item.profilePic ?: "")) // Using fullName and profilePic as child items
//            expandableListDetail[group] = Pair(groupImageUrl, childItems)
//        }
//
//        // Iterate through your API response and map it to ExpandableListView data format
////        for (item in otheruserRequestList) {
////            val group = item.username ?: "" // Using username as group name, replace it with the actual property you want to use
////            val childItems = listOf(Pair(item.fullName ?: "", item.profilePic ?: "")) // Using fullName and profilePic as child items
////            expandableListDetail[group] = childItems
////        }
//
//        return expandableListDetail
//    }




//    private fun convertToExpandableList(otheruserRequestList: MutableList<OtherData>): HashMap<String, List<String>> {
//        val expandableListDetail = HashMap<String, List<String>>()
//
//        // Iterate through your API response and map it to ExpandableListView data format
//        for (item in otheruserRequestList) {
//            val group = item.username ?: "" // Using username as group name, replace it with the actual property you want to use
//            val childItems = listOf(item.fullName ?: "") // Using fullName as child item, replace it with the actual property you want to use
//            expandableListDetail[group] = childItems
//        }
//
//        return expandableListDetail
//    }


//    private fun initData(): HashMap<String, List<String>> {
//        val expandableListDetail = HashMap<String, List<String>>()
//
//        val group1 = "Group 1"
//        val group2 = "Group 2"
//
//        val list1 = arrayListOf("Item 1", "Item 2", "Item 3")
//        val list2 = arrayListOf("Item 4", "Item 5", "Item 6")
//
//        expandableListDetail[group1] = list1
//        expandableListDetail[group2] = list2
//
//        return expandableListDetail
//
//    }
//}
















//class ExpandableFragment : Fragment() {
//
//
//    private lateinit var expandableListView: ExpandableListView
//    private lateinit var adapter: MyExpandableListAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_expandable, container, false)
//
//        expandableListView = view.findViewById(R.id.expandableListView)
//        adapter = MyExpandableListAdapter(requireContext(), initData())
//        expandableListView.setAdapter(adapter)
//
//        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
//            val selectedItem = adapter.getChild(groupPosition, childPosition) as String
//            Toast.makeText(requireContext(), "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
//            false
//        }
//
//        return view
//    }
//
//
//
//    private fun initData(): HashMap<String, List<String>> {
//        val expandableListDetail = HashMap<String, List<String>>()
//
//        val group1 = "Group 1"
//        val group2 = "Group 2"
//
//        val list1 = arrayListOf("Item 1", "Item 2", "Item 3")
//        val list2 = arrayListOf("Item 4", "Item 5", "Item 6")
//
//        expandableListDetail[group1] = list1
//        expandableListDetail[group2] = list2
//
//        return expandableListDetail
//
//    }
//}