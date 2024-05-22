package com.example.quickconnect.activities

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.R
import com.example.quickconnect.adapters.UserViewSocialLinkAdapter
import com.example.quickconnect.databinding.ActivityViewersBinding
import com.example.quickconnect.model.viewersmodel.SocialLinks
import com.example.quickconnect.repository.FavouriteViewRepositary
import com.example.quickconnect.repository.NotificationRepositary
import com.example.quickconnect.repository.UserViewRepositary
import com.example.quickconnect.utils.Alerts
import com.example.quickconnect.utils.NoInternetUtils
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.FavouriteViewModel
import com.example.quickconnect.viewmodels.NotificationViewModel
import com.example.quickconnect.viewmodels.UserViewViewModel
import com.sdsmdg.tastytoast.TastyToast
import com.squareup.picasso.Picasso

class UserViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewersBinding
    lateinit var progressBarHelper: ProgressBarHelper

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userViewSharedPreferences: SharedPreferences
    lateinit var userToken: String
    lateinit var userName: String
    lateinit var userNameBk : String
    lateinit var bookmark : String
     var requestId : Int? = null

    lateinit var fullnameTv: TextView
    lateinit var workAtTv: TextView
    lateinit var descTV: TextView
    lateinit var profilePicIv: ImageView
    lateinit var addFavouriteTv : TextView

    lateinit var fullNameString: String
    lateinit var workAtString: String
    lateinit var descString: String
    lateinit var profilePicString: String
    lateinit var mobileNumber : String
    lateinit var userViewName : String
    lateinit var userWorkAt : String
    lateinit var userViewSocialLinkAdapter: UserViewSocialLinkAdapter
    lateinit var userViewList: List<SocialLinks>
    lateinit var socialListRv : RecyclerView

    lateinit var imageUrl : String

    private val CONTACTS_PERMISSION_REQUEST_CODE = 123


    var social_link_id : Int? = null

    private val UserViewModel: UserViewViewModel by lazy {
        val userViewModelFactory = UserViewRepositary()
        ViewModelProvider(this, userViewModelFactory)[UserViewViewModel::class.java]
    }
    private val notificationViewModel: NotificationViewModel by lazy {
        val notificationViewModelFactory = NotificationRepositary()
        ViewModelProvider(this, notificationViewModelFactory)[NotificationViewModel::class.java]
    }

    private val addFavouriteViewModel: FavouriteViewModel by lazy {
        val addFavouriteViewModelFactory = FavouriteViewRepositary()
        ViewModelProvider(this, addFavouriteViewModelFactory)[FavouriteViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!NoInternetUtils.isInternetAvailable(this)) {
            NoInternetUtils.showNoInternetDialog(this)
        }

        initialization ()
        observeUserData()



        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        userToken = sharedPreferences.getString("userToken", "").toString()
        Log.e("TAG", "userTokenOncreate: $userToken", )
//        userName = sharedPreferences.getString("userName", "").toString()
        userName = intent.getStringExtra("userName").toString()
        userNameBk = intent.getStringExtra("BookmarkUserName").toString()
        bookmark = intent.getStringExtra("bookmark").toString()
        Log.e("TAG", "userName: $userName")


        userViewSharedPreferences = getSharedPreferences("UserViewData", Context.MODE_PRIVATE)
        fullNameString = userViewSharedPreferences.getString("userViewName", "").toString()
        workAtString = userViewSharedPreferences.getString("userWorkAt", "").toString()
        descString = userViewSharedPreferences.getString("userDesc", "").toString()
        profilePicString = userViewSharedPreferences.getString("userProfilePic", "").toString()

        sharedPreferences = getSharedPreferences("ProfileData", Context.MODE_PRIVATE)
        mobileNumber = sharedPreferences.getString("phoneNumberR","").toString()

        Log.e("TAG", "data: $fullNameString + $workAtString + $descString + $profilePicString", )


        Log.e("TAG", "onCreate: $userName",)


        if (bookmark == "bookmark"){
            bookmarkView()
        }else {
            viewCall()
        }

//        setData()
        addFavourite()
        observeAddFavourite()
        observeSendRequest()

        requestContactsPermission()


        // Get data from intent or wherever you are getting it
        userViewName = "" // Example full name
        mobileNumber = "" // Example phone number
        userWorkAt = ""
        userViewList = listOf<SocialLinks>()


        // Add to contacts button click listener
        binding.addContactBtn.setOnClickListener {
            val socialLinks = userViewList.mapNotNull { it.links!!.link }
            addToContacts(userViewName, mobileNumber, userWorkAt, socialLinks)
        }
//
//        val addToContactsButton: AppCompatButton = findViewById(R.id.add_contact_btn)
//        addToContactsButton.setOnClickListener {
//            if (fullNameString != null || workAtString != null ||
//                descString != null || !userViewList.isNullOrEmpty() || !mobileNumber.isNullOrEmpty()
//            ) {
//                addToContacts(fullNameString, workAtString, descString, userViewList,mobileNumber)
//            } else {
//                TastyToast.makeText(this, "No data available", TastyToast.LENGTH_LONG, TastyToast.DEFAULT)
//            }
//        }


        // Set click listener for "Add to Contacts" button
//        val addToContactsButton: AppCompatButton = findViewById(R.id.add_contact_btn)
//        addToContactsButton.setOnClickListener {
//            // Check if the required data is available
//            if (::userName.isInitialized && ::fullNameString.isInitialized &&
//                ::workAtString.isInitialized && ::descString.isInitialized &&
//                !userViewList.isNullOrEmpty() &&
//                !mobileNumber.isNullOrEmpty()
//            ) {
//                addToContacts(userName, fullNameString, workAtString, descString, userViewList)
//            } else {
//                TastyToast.makeText(this, "User details not available", TastyToast.LENGTH_LONG, TastyToast.DEFAULT)
//
////                Toast.makeText(this, "User details not available", Toast.LENGTH_SHORT).show()
//            }
//        }


    }

    fun initialization (){

        progressBarHelper =  ProgressBarHelper(this)


        fullnameTv = binding.fullnameTv
        workAtTv = binding.workAtTv
        descTV = binding.descTv
        profilePicIv = binding.profileIv
        addFavouriteTv = binding.starOuter

        socialListRv = binding.socialGetListRv


        binding.backTv.setOnClickListener {
            onBackPressed()
        }
    }

    fun setData() {

        try {

        fullnameTv.setText(fullNameString)


        if (workAtString.isEmpty() || descString.isEmpty()) {
            workAtTv.isVisible = false
            descTV.isVisible = false
        } else {
            workAtTv.setText(workAtString)
            descTV.setText(descString)
        }


        Log.e("TAG", "setData: $profilePicString", )


        if (profilePicString.isNotEmpty()) {

            val imageUrl = profilePicString

            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.profile_placeholder) // Placeholder image resource
                .error(R.drawable.profile_placeholder) // Error image resource
                .into(profilePicIv)
        }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun viewCall() {
        UserViewModel.UserView(userToken, "$userName")
        Log.e("TAG", "viewCall: $userName", )
//        observeUserData()
    }

    private fun bookmarkView() {
        UserViewModel.UserView(userToken, "$userNameBk")
        Log.e("TAG", "viewCall: $userName", )
//        observeUserData()
    }


    fun observeUserData() {

        progressBarHelper.showProgressDialog()

        UserViewModel.observableUserViewData().observe(this) { userData ->

            try {

                addTADataToAdapter(userData.data!!.socialLinks)
                progressBarHelper.hideProgressDialog()

                if (userData.status == true) {
                    userViewName = userData.data!!.fullName.toString()
                    userWorkAt = userData.data!!.workAt.toString()
                    val userDesc = userData.data!!.description
                    val userProfilePic = userData.data!!.profilePic

                    val userList = userData.data!!.socialLinks

                    userViewList = userData.data!!.socialLinks
                    mobileNumber = userData.data!!.primaryPhoneNumber.toString()

                    if (userData.data!!.isFavorite == true){
                        addFavouriteTv.isVisible = false
                        binding.starFill.isVisible = true
                    }


                    if (!userViewName.isNullOrEmpty()) {

                        fullnameTv.setText(userViewName)


                        if (userWorkAt.isEmpty()) {
                            workAtTv.isVisible = false
                            descTV.isVisible = false
                        } else {
                            workAtTv.isVisible = true
                            descTV.isVisible = true
                            workAtTv.setText(userWorkAt)
                            descTV.setText(userDesc)
                        }
                    }


                    Log.e("TAG", "setData: $profilePicString",)


                    if (userProfilePic!!.isNotEmpty()) {

                         imageUrl = userProfilePic

                        loadImageServer(imageUrl)

                    }

//
//                    val addToContactsButton: AppCompatButton = findViewById(R.id.add_contact_btn)
//                    addToContactsButton.setOnClickListener {
//                        addToContacts(userViewName.toString(), mobileNumber)
//                    }




                    val editor = userViewSharedPreferences.edit()
                editor.putString("userViewName", userViewName)
                editor.putString("userWorkAt", userWorkAt)
                editor.putString("userDesc", userDesc)
                editor.putString("userProfilePic", userProfilePic)
                editor.apply()

                }

            }catch (e: Exception){
                e.printStackTrace()
            }

        }

    }


    private fun addTADataToAdapter(userList: MutableList<SocialLinks>) {
        try {
            // Update the existing adapter or create a new one if it's null
            if (::userViewSocialLinkAdapter.isInitialized) {
                userViewSocialLinkAdapter.updateData(userList)
            } else {
                userViewSocialLinkAdapter = UserViewSocialLinkAdapter(userList, this)
                socialListRv.layoutManager = GridLayoutManager(this, 4)
                socialListRv.adapter = userViewSocialLinkAdapter
                userViewSocialLinkAdapter.onItemClick = { socialData ->
                    // Your onItemClick logic...

                    social_link_id = socialData.links?.id ?: 0

                    if (socialData.links?.isLocked == true && socialData.links?.link == null) {
                        showRequestDialog()
                    } else {
                        try {
                            val url = socialData.links?.link
                            if (!url.isNullOrBlank()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            } else {
                                TastyToast.makeText(this, "Invalid URL", TastyToast.LENGTH_LONG, TastyToast.CONFUSING)

//                                Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            TastyToast.makeText(this, "URL not valid", TastyToast.LENGTH_LONG, TastyToast.INFO)

//                            Toast.makeText(this, "URL not valid", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    private fun showRequestDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(Alerts.requestPermission)
            .setTitle("Request")
            .setPositiveButton("OK") { dialog, id ->
                sendRequest()
            }
        builder.setNegativeButton("Not now") { dialog, id -> }

        val dialog = builder.create()
        dialog.show()

    }

    private fun sendRequest() {
        Log.e("TAG", "link_id: $social_link_id", )
        Log.e("TAG", "userToken: $userToken", )
        notificationViewModel.sendRequest(userToken, social_link_id!!)



    }

    fun observeSendRequest() {
        notificationViewModel.observeSendRequest().observe(this){sendForAccess ->

            try {

            Log.e("TAG", "observeSendRequest: $sendForAccess", )

            if (sendForAccess.data != null) {

                requestId = sendForAccess.data!!.id
                val reg = sendForAccess.data!!.message
                Log.e("TAG", "requestId:$requestId + $reg ", )


                TastyToast.makeText(this, "Request Sent", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)


//                Toast.makeText(this, "Request Sent",Toast.LENGTH_LONG).show()

                val editor = userViewSharedPreferences.edit()
                editor.putInt("requestId", requestId!!)
                editor.apply()
            } else {
                // Handle the case where sendForAccess.data is null
                Log.e("TAG", "sendForAccess.data is null", )
            }
            }catch(e : Exception) {
                e.printStackTrace()
            }
        }

    }

   fun addFavourite(){
       addFavouriteTv.isVisible = true
       addFavouriteTv.setOnClickListener {
           addFavouriteViewModel.addFavourite("$userToken","$userName")
       }
   }

    fun observeAddFavourite(){
        addFavouriteViewModel.observeaddFavourite().observe(this){ favData ->
            try {

            Log.e("TAG", "successAddFavourite: $favData", )
            if (favData.status == true){



                addFavouriteTv.isVisible = false
                binding.starFill.isVisible = true

                TastyToast.makeText(this, "Added to favourite", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)

//                Toast.makeText(this, "Added to favourite", Toast.LENGTH_SHORT).show()
            }
            }catch(e : Exception) {
                e.printStackTrace()
            }
        }
    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            CONTACTS_PERMISSION_REQUEST_CODE -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission granted, you can add contacts here
//                    addToContacts(fullNameString, mobileNumber)
//                } else {
//                    // Permission denied, show a message or take appropriate action
//                    TastyToast.makeText(
//                        this,
//                        "Contacts permission denied. Cannot add contact.",
//                        TastyToast.LENGTH_LONG,
//                        TastyToast.ERROR
//                    )
//                }
//            }
//            // Handle other permissions if needed
//        }
//    }




    private fun requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_CONTACTS),
                CONTACTS_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted, perform the contact-related operation
            // You can add your logic here or call addToContacts directly
        }
    }

        private fun addToContacts(fullName: String, phoneNumber: String, workAt: String, socialLinks: List<String>) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.WRITE_CONTACTS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.WRITE_CONTACTS),
                        CONTACTS_PERMISSION_REQUEST_CODE
                    )
                    return
                }
            }

            val displayName = fullName

            val contentValues = ContentValues().apply {
                put(ContactsContract.RawContacts.ACCOUNT_TYPE, null as String?)
                put(ContactsContract.RawContacts.ACCOUNT_NAME, null as String?)
            }

            val rawContactUri =
                contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues)
            val rawContactId = rawContactUri?.lastPathSegment

            val nameValues = ContentValues().apply {
                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
            }

            contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)

            val phoneValues = ContentValues().apply {
                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                put(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
            }

            contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneValues)

            if (workAt.isNotEmpty()) {
                val organizationValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Organization.COMPANY, workAt)
                }

                contentResolver.insert(ContactsContract.Data.CONTENT_URI, organizationValues)
            }

            socialLinks.forEach { link ->
                val socialValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Website.URL, link)
                }

                contentResolver.insert(ContactsContract.Data.CONTENT_URI, socialValues)
            }

            TastyToast.makeText(
                this,
                "Contact added successfully.",
                TastyToast.LENGTH_LONG,
                TastyToast.SUCCESS
            )
        }


//
//    private fun addToContacts(fullName: String, phoneNumber: String, workAt: String) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    android.Manifest.permission.WRITE_CONTACTS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(android.Manifest.permission.WRITE_CONTACTS),
//                    CONTACTS_PERMISSION_REQUEST_CODE
//                )
//                return
//            }
//        }
//
//        val displayName = fullName
//
//        val contentValues = ContentValues().apply {
//            put(ContactsContract.RawContacts.ACCOUNT_TYPE, null as String?)
//            put(ContactsContract.RawContacts.ACCOUNT_NAME, null as String?)
//        }
//
//        val rawContactUri =
//            contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues)
//        val rawContactId = rawContactUri?.lastPathSegment
//
//        val nameValues = ContentValues().apply {
//            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
//            put(
//                ContactsContract.Data.MIMETYPE,
//                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
//            )
//            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
//        }
//
//        contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)
//
//        val phoneValues = ContentValues().apply {
//            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
//            put(
//                ContactsContract.Data.MIMETYPE,
//                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
//            )
//            put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
//            put(
//                ContactsContract.CommonDataKinds.Phone.TYPE,
//                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
//            )
//        }
//
//        contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneValues)
//
//        if (workAt.isNotEmpty()) {
//            val organizationValues = ContentValues().apply {
//                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
//                put(
//                    ContactsContract.Data.MIMETYPE,
//                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
//                )
//                put(ContactsContract.CommonDataKinds.Organization.COMPANY, workAt)
//            }
//
//            contentResolver.insert(ContactsContract.Data.CONTENT_URI, organizationValues)
//        }
//
//        TastyToast.makeText(
//            this,
//            "Contact added successfully.",
//            TastyToast.LENGTH_LONG,
//            TastyToast.SUCCESS
//        )
//    }


//
//    private fun addToContacts(fullName: String, mobileNumber: String) {
//        try {
//            val ops = ArrayList<ContentProviderOperation>()
//
//            // Add contact display name
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
//                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
//                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
//                    .build()
//            )
//
//            // Add contact name
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                    .withValue(
//                        ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
//                    )
//                    .withValue(
//                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
//                        fullName
//                    )
//                    .build()
//            )
//
//            // Add mobile number
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                    .withValue(
//                        ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
//                    )
//                    .withValue(
//                        ContactsContract.CommonDataKinds.Phone.NUMBER,
//                        mobileNumber
//                    )
//                    .withValue(
//                        ContactsContract.CommonDataKinds.Phone.TYPE,
//                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
//                    )
//                    .build()
//            )
//
//            // Apply the batch of operations
//            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
//
//            Toast.makeText(
//                this,
//                "Contact added successfully",
//                Toast.LENGTH_LONG
//            ).show()
//
//        } catch (e: RemoteException) {
//            e.printStackTrace()
//            Log.e("TAG", "addToContacts: RemoteException - ${e.message}")
//            Toast.makeText(
//                this,
//                "Failed to add contact: RemoteException - ${e.message}",
//                Toast.LENGTH_LONG
//            ).show()
//        } catch (e: SecurityException) {
//            e.printStackTrace()
//            Log.e("TAG", "addToContacts: SecurityException - ${e.message}")
//            Toast.makeText(
//                this,
//                "Failed to add contact: SecurityException - ${e.message}",
//                Toast.LENGTH_LONG
//            ).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e("TAG", "addToContacts: Exception - ${e.message}")
//            Toast.makeText(
//                this,
//                "Failed to add contact: Exception - ${e.message}",
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }



//    fun addToContacts(
//        fullName: String,
//        workAt: String,
//        desc: String,
//        mobileNumber: String
//    ) {
//        try {
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    android.Manifest.permission.WRITE_CONTACTS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // Handle case where permission is not granted
//                // You can show a message or request permission again
//                return
//            }
//
//            val ops = ArrayList<ContentProviderOperation>()
//
//            // Add contact display name
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
//                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
//                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
//                    .build()
//            )
//
//            // Add contact name
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                    .withValue(
//                        ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
//                    )
//                    .withValue(
//                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
//                        fullName
//                    )
//                    .build()
//            )
//
//            // Add mobile number
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                    .withValue(
//                        ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
//                    )
//                    .withValue(
//                        ContactsContract.CommonDataKinds.Phone.NUMBER,
//                        mobileNumber
//                    )
//                    .withValue(
//                        ContactsContract.CommonDataKinds.Phone.TYPE,
//                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
//                    )
//                    .build()
//            )
//
//            // Add work and description
//            if (workAt.isNotEmpty()) {
//                ops.add(
//                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                        .withValue(
//                            ContactsContract.Data.MIMETYPE,
//                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
//                        )
//                        .withValue(
//                            ContactsContract.CommonDataKinds.Organization.COMPANY,
//                            workAt
//                        )
//                        .withValue(
//                            ContactsContract.CommonDataKinds.Organization.TYPE,
//                            ContactsContract.CommonDataKinds.Organization.TYPE_WORK
//                        )
//                        .build()
//                )
//            }
//            if (desc.isNotEmpty()) {
//                ops.add(
//                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                        .withValue(
//                            ContactsContract.Data.MIMETYPE,
//                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
//                        )
//                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, desc)
//                        .build()
//                )
//            }
//
//            // Apply the batch of operations
//            val results = contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
//
//            // Check the results for success
//            for (result in results) {
//                if (result.uri == null) {
//                    // Operation failed
//                    TastyToast.makeText(
//                        this,
//                        "Failed to add contact",
//                        TastyToast.LENGTH_LONG,
//                        TastyToast.ERROR
//                    )
//                    return
//                }
//            }
//
//            // All operations succeeded
//            TastyToast.makeText(
//                this,
//                "Contact added successfully",
//                TastyToast.LENGTH_LONG,
//                TastyToast.SUCCESS
//            )
//
//        } catch (e: RemoteException) {
//            e.printStackTrace()
//            Log.e("TAG", "addToContacts: RemoteException - ${e.message}")
//            TastyToast.makeText(
//                this,
//                "Failed to add contact",
//                TastyToast.LENGTH_LONG,
//                TastyToast.ERROR
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e("TAG", "addToContacts: Exception - ${e.message}")
//            TastyToast.makeText(
//                this,
//                "Failed to add contact",
//                TastyToast.LENGTH_LONG,
//                TastyToast.ERROR
//            )
//        }
//    }
//


//    fun addToContacts(
//        fullName: String,
//        workAt: String,
//        desc: String,
//        socialLinks: List<SocialLinks>,
//        mobileNumber: String
//    ) {
//        try {
//            val ops = ArrayList<ContentProviderOperation>()
//
//            // Add contact display name
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
//                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
//                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
//                    .build()
//            )
//
//            // Add contact name
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                    .withValue(
//                        ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
//                    )
//                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullName)
//                    .build()
//            )
//
//            // Add mobile number
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                    .withValue(
//                        ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
//                    )
//                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
//                    .withValue(
//                        ContactsContract.CommonDataKinds.Phone.TYPE,
//                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
//                    )
//                    .build()
//            )
//
//            // Add work and description
//            if (workAt.isNotEmpty()) {
//                ops.add(
//                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                        .withValue(
//                            ContactsContract.Data.MIMETYPE,
//                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
//                        )
//                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, workAt)
//                        .withValue(
//                            ContactsContract.CommonDataKinds.Organization.TYPE,
//                            ContactsContract.CommonDataKinds.Organization.TYPE_WORK
//                        )
//                        .build()
//                )
//            }
//            if (desc.isNotEmpty()) {
//                ops.add(
//                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                        .withValue(
//                            ContactsContract.Data.MIMETYPE,
//                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
//                        )
//                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, desc)
//                        .build()
//                )
//            }
//
//            // Add social links
//            for (socialLink in socialLinks) {
//                if (socialLink.links?.link != null) {
//                    ops.add(
//                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                            .withValue(
//                                ContactsContract.Data.MIMETYPE,
//                                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
//                            )
//                            .withValue(ContactsContract.CommonDataKinds.Website.URL, socialLink.links?.link)
//                            .withValue(
//                                ContactsContract.CommonDataKinds.Website.TYPE,
//                                ContactsContract.CommonDataKinds.Website.TYPE_PROFILE
//                            )
//                            .build()
//                    )
//                }
//            }
//
//            // Apply the batch of operations
//            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
//
//            TastyToast.makeText(
//                this,
//                "Contact added successfully",
//                TastyToast.LENGTH_LONG,
//                TastyToast.SUCCESS
//            )
//
//        } catch (e: RemoteException) {
//            e.printStackTrace()
//            Log.e("TAG", "addToContacts: RemoteException - ${e.message}")
//            Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e("TAG", "addToContacts: Exception - ${e.message}")
//            Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
//        }
//    }


//    fun addToContacts(
//        fullName: String,
//        workAt: String,
//        desc: String,
//        socialLinks: List<SocialLinks>,
//        mobileNumber: String
//    ) {
//        try {
//            val ops = ArrayList<ContentProviderOperation>()
//
//            // Add contact display name
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
//                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
//                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
//                    .build()
//            )
//
//            // Add contact name
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                    .withValue(
//                        ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
//                    )
//                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullName)
//                    .build()
//            )
//
//            // Add mobile number
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                    .withValue(
//                        ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
//                    )
//                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
//                    .withValue(
//                        ContactsContract.CommonDataKinds.Phone.TYPE,
//                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
//                    )
//                    .build()
//            )
//
//            // Add work and description
//            if (workAt.isNotEmpty()) {
//                ops.add(
//                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                        .withValue(
//                            ContactsContract.Data.MIMETYPE,
//                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
//                        )
//                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, workAt)
//                        .withValue(
//                            ContactsContract.CommonDataKinds.Organization.TYPE,
//                            ContactsContract.CommonDataKinds.Organization.TYPE_WORK
//                        )
//                        .build()
//                )
//            }
//            if (desc.isNotEmpty()) {
//                ops.add(
//                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                        .withValue(
//                            ContactsContract.Data.MIMETYPE,
//                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
//                        )
//                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, desc)
//                        .build()
//                )
//            }
//
//            // Add social links
//            for (socialLink in socialLinks) {
//                if (socialLink.links?.link != null) {
//                    ops.add(
//                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                            .withValue(
//                                ContactsContract.Data.MIMETYPE,
//                                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
//                            )
//                            .withValue(ContactsContract.CommonDataKinds.Website.URL, socialLink.links?.link)
//                            .withValue(
//                                ContactsContract.CommonDataKinds.Website.TYPE,
//                                ContactsContract.CommonDataKinds.Website.TYPE_PROFILE
//                            )
//                            .build()
//                    )
//                }
//            }
//
//            // Add image if imageUrl is not empty
//            if (imageUrl.isNotEmpty()) {
//                ops.add(
//                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                        .withValue(
//                            ContactsContract.Data.MIMETYPE,
//                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
//                        )
//                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageUrl.toByteArray())
//                        .build()
//                )
//            }
//
//            // Apply the batch of operations
//            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
//
//            TastyToast.makeText(
//                this,
//                "Contact added successfully",
//                TastyToast.LENGTH_LONG,
//                TastyToast.SUCCESS
//            )
//
//        } catch (e: RemoteException) {
//            e.printStackTrace()
//            Log.e("TAG", "addToContacts: RemoteException - ${e.message}")
//            Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e("TAG", "addToContacts: Exception - ${e.message}")
//            Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
//        }
//    }



//    fun addToContacts(
//        fullName: String,
//        workAt: String,
//        desc: String,
//        socialLinks: List<SocialLinks>,
//        mobileNumber: String) {
//        try {
//            val ops = ArrayList<ContentProviderOperation>()
//
//            // Add contact display name
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
//                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
//                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
//                    .build()
//            )
//
//            // Add contact name
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
//                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullName)
//                    .build()
//            )
//
//            // Add mobile number
//            ops.add(
//                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
//                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
//                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
//                    .build()
//            )
//
//            // Add work and description
//            if (workAt.isNotEmpty()) {
//                ops.add(
//                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
//                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, workAt)
//                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
//                        .build()
//                )
//            }
//            if (desc.isNotEmpty()) {
//                ops.add(
//                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
//                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, desc)
//                        .build()
//                )
//            }
//
//            // Add social links
//            for (socialLink in socialLinks) {
//                if (socialLink.links?.link != null) {
//                    ops.add(
//                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
//                            .withValue(ContactsContract.CommonDataKinds.Website.URL, socialLink.links?.link)
//                            .withValue(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_PROFILE)
//                            .build()
//                    )
//                }
//            }
//
//            // Add image if imageUrl is not empty
//            if (imageUrl.isNotEmpty()) {
//                ops.add(
//                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
//                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageUrl.toByteArray())
//                        .build()
//                )
//            }
//
//            // Apply the batch of operations
//            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
//
//            TastyToast.makeText(this, "Contact added successfully", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)
//
//        } catch (e: RemoteException) {
//            e.printStackTrace()
//            Log.e("TAG", "addToContacts: RemoteException - ${e.message}")
//            Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e("TAG", "addToContacts: Exception - ${e.message}")
//            Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
//        }
//    }



    private fun loadImageServer(imageUri: String) {
        Picasso.get()
            .load(imageUri)
            .error(R.drawable.profile_placeholder)
            .placeholder(R.drawable.profile_placeholder)
            .into(profilePicIv)

        profilePicIv!!.postDelayed({
            val drawable = profilePicIv!!.drawable

            if (drawable != null && drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap

                if (bitmap != null) {
                    val bottomBitmap = Bitmap.createBitmap(bitmap, 0,
                        (bitmap.height * 0.8).toInt(), bitmap.width, (bitmap.height * 0.2).toInt()
                    )

                    Palette.from(bottomBitmap).generate { palette ->
                        val backgroundColor = palette?.getDominantColor(
                            ContextCompat.getColor(this, R.color.black)
                        ) ?: ContextCompat.getColor(this, R.color.black)

                        // Apply gradient to the background
                        applyGradientBackground(backgroundColor)
                    }
                } else {
                    Toast.makeText(this, "Error loading image: Bitmap is null", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error loading image: Not a BitmapDrawable", Toast.LENGTH_SHORT).show()
            }
        }, 500)
    }

    private fun applyGradientBackground(color: Int) {
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(color, ContextCompat.getColor(this, R.color.black))
        )

        findViewById<LinearLayout>(R.id.userView_linear).background = gradientDrawable
    }

}


