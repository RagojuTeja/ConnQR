package com.example.quickconnect.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.R
import com.example.quickconnect.adapters.CallToActionAdapter
import com.example.quickconnect.adapters.SocialGetListAdapter
import com.example.quickconnect.databinding.ActivityMainBinding
import com.example.quickconnect.fragments.CallToActionFragment
import com.example.quickconnect.fragments.EditProfileBottomSheetFragment
import com.example.quickconnect.fragments.SocialProfileFragment
import com.example.quickconnect.model.CallToActionModel.ActionList
import com.example.quickconnect.model.sociallinksdata.CategoryData
import com.example.quickconnect.model.usermodel.QrData
import com.example.quickconnect.repository.CallToActionRepositary
import com.example.quickconnect.repository.EditProfileRepositary
import com.example.quickconnect.repository.QrCodeRepositary
import com.example.quickconnect.repository.SocialLinksRepositary
import com.example.quickconnect.utils.Alerts
import com.example.quickconnect.utils.NoInternetUtils
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.CallToActionViewModel
import com.example.quickconnect.viewmodels.EditProfileViewModel
import com.example.quickconnect.viewmodels.QrCodeViewModel
import com.example.quickconnect.viewmodels.SocialLinksViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sdsmdg.tastytoast.TastyToast
import com.squareup.picasso.Picasso
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

class MainActivity : AppCompatActivity(), CallToActionAdapter.OnMessageLongClickListener,
    SocialGetListAdapter.OnMessageLongClickListener,CallToActionFragment.MainActivityListener,EditProfileBottomSheetFragment.MainActivityListener {


    private lateinit var binding: ActivityMainBinding
    lateinit var bottomSheetDialog: BottomSheetDialog
    lateinit var progressBarHelper: ProgressBarHelper

    lateinit var socialGetRV: RecyclerView
    lateinit var socialGetAdapter: SocialGetListAdapter

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userSharedPreferences : SharedPreferences
    lateinit var userToken : String

    lateinit var fullNameTv: TextView
    lateinit var workAtTv: TextView
    lateinit var descriptionTv: TextView
     var profile_pic: ImageView? = null

    lateinit var getFullName: String
    lateinit var getWorkAt: String
    lateinit var getDesc: String
    lateinit var getProfile_pic: String

    private val READ_REQUEST_CODE = 42
    private val CAMERA_REQUEST_CODE = 43

    private lateinit var photoUri: Uri
    var Gallery = 1
    var Camera = 2

    private var userCamPermissionFlag: String = ""
    private var userMedPermissionFlag: String = ""


    private val GALLERY = 1
    private val CAMERA = 2
    private val IMAGE_DIRECTORY = "/demonuts"

    lateinit var permissionsharedPreferences: SharedPreferences
    lateinit var callToActionAdapter: CallToActionAdapter

    lateinit var callToActionList: MutableList<ActionList>

    private var encodedImage: String = ""  // Initialize with a default value

     var profileR : String = ""

    private val CALL_LIST_KEY = "call_list"



    private val socialDataViewModel: SocialLinksViewModel by lazy {
        val appDataViewModelFactory = SocialLinksRepositary()
        ViewModelProvider(this, appDataViewModelFactory)[SocialLinksViewModel::class.java]
    }

    private val callToActionViewModel: CallToActionViewModel by lazy {
        val callToActiionViewModelFactory = CallToActionRepositary()
        ViewModelProvider(this, callToActiionViewModelFactory)[CallToActionViewModel::class.java]
    }

    private val editProfileViewModel: EditProfileViewModel by lazy {
        val editProfileViewModelFactory = EditProfileRepositary()
        ViewModelProvider(this, editProfileViewModelFactory)[EditProfileViewModel::class.java]
    }

    private val qrCodeViewModel: QrCodeViewModel by lazy {
        val qrCodeViewModelFactory = QrCodeRepositary()
        ViewModelProvider(this, qrCodeViewModelFactory)[QrCodeViewModel::class.java]
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!NoInternetUtils.isInternetAvailable(this)) {
            NoInternetUtils.showNoInternetDialog(this)
            binding.noListImg.isVisible = true
            binding.noListSocialImg.isVisible = true
            progressBarHelper.hideProgressDialog()
        }

        userSharedPreferences = getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        userToken = userSharedPreferences.getString("userToken","").toString()
        Log.e("TAG", "onCreate: $userToken")

        permissionsharedPreferences = getSharedPreferences("PermissionFlag", Context.MODE_PRIVATE)
        userCamPermissionFlag = permissionsharedPreferences.getString("camFlag", "").toString()
        userMedPermissionFlag = permissionsharedPreferences.getString("medFlag", "").toString()


        sharedPreferences = getSharedPreferences("ProfileData", Context.MODE_PRIVATE)

        onLoggedInWithNewUser()


        Initilization()
//        photoUpdate()
        setProfile ()


        editScreen()
        callToAction()
        observerSocialListData()
        callActionList()
        observerCallToListData()

        observePhotoData()
        getProfileData()
        observProfileData()
        observableSocialLinkDelete()
        observableCallLinkDelete()



        binding.cardView2.setOnClickListener {
            val intent = Intent(this, ProfilesActivity::class.java)
            intent.putExtra("Logo", "Logo") // Pass "Logo" as extra
            startActivity(intent)
        }

        binding.notification.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            intent.putExtra("Notification", "Notification") // Pass "Notification" as extra
            startActivity(intent)

            // Example usage
//            val instagramProfileUrl = "https://www.instagram.com/nivi__0702/"
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramProfileUrl.toString()))
//            startActivity(intent)


        }

        binding.socialProfileCd.setOnClickListener {
            startActivity(Intent(this, SocialProfileActivity::class.java))
        }

        binding.addListTv.setOnClickListener {
                startActivity(Intent(this, SocialProfileActivity::class.java))
        }

        socialDataViewModel.socialLinkList("$userToken")

        binding.addPic.setOnClickListener {
            showPictureDialog()
        }

    }

    // Call this function after logging in with a new user
    private fun onLoggedInWithNewUser() {
        clearSharedPreferences()
        setNewUserData()
    }

    // Function to clear shared preferences
    private fun clearSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    // Function to set new user data
    private fun setNewUserData() {
        // Set the new user data here
        // For example, you can set userToken, fullName, workAt, desc, profile, etc.
        // Call setProfile to update the UI with the new data
        setProfile()
    }



    // Function to open a URL in a browser
    fun openUrl(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)

        // Check if there's an app to handle the intent
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Handle the case where there's no app to handle the intent
            // You can display a message or use a fallback mechanism
        }
    }





    private fun Initilization() {
        progressBarHelper = ProgressBarHelper(this)

        fullNameTv = binding.fullnameTv
        workAtTv = binding.workAtTv
        descriptionTv = binding.descTv
        profile_pic = binding.profileIv

    }


    fun setProfile(){

        try {


            getFullName = sharedPreferences.getString("fullName", "").toString()
        getWorkAt = sharedPreferences.getString("workAt", "").toString()
        getDesc = sharedPreferences.getString("desc", "").toString()
        getProfile_pic = sharedPreferences.getString("profile", "").toString()
//        imageUrl = sharedPreferences.getString("profile", "") ?: ""
//

        Log.e("TAG", "getFullName: $getFullName",)

//        runOnUiThread {

            if (!getFullName.isNullOrEmpty()) {
                fullNameTv.setText(getFullName)
                workAtTv.setText(getWorkAt)
                descriptionTv.setText(getDesc)
            }

        Log.e("TAG", "phto:$getProfile_pic ")

        Log.e("TAG", "Initilization:$getProfile_pic",)

            if (!getProfile_pic.isNullOrEmpty()) {
                loadImageServer(getProfile_pic)
                // Use coroutines to perform the get() operation in the background
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        // Check if the image URL is not null or empty
                        if (!getProfile_pic.isNullOrEmpty()) {
                            // Check if the image URL is already available in the cache
                            val cachedImage = Picasso.get().load(getProfile_pic).get()

                            // Switch back to the main thread to update the UI
                            launch(Dispatchers.Main) {
                                // If the image is cached, load it directly without a placeholder
                                if (cachedImage != null) {
                                    profile_pic?.setImageBitmap(cachedImage)
                                    Log.e("TAG", "catchedimage: ", )
                                } else {
                                    // If not cached, load the image with a placeholder
                                    Picasso.get()
                                        .load(getProfile_pic)
                                        .error(R.drawable.profile_placeholder)
                                        .placeholder(R.drawable.profile_placeholder)
                                        .into(profile_pic)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }


        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }




    private fun callToAction() {
        binding.addMoreTv.setOnClickListener {
            val callToActionFragment = CallToActionFragment()
            callToActionFragment.show(supportFragmentManager, callToActionFragment.tag)
        }

//        binding.socialProfileCd.setOnClickListener {
////            val socialProfileFragment = SocialProfileFragment()
////            socialProfileFragment.show(supportFragmentManager, socialProfileFragment.tag)
//
//            startActivity(Intent(this, SocialProfileActivity::class.java))
////            startActivity(Intent(this, SignUpActivity::class.java))
//        }
    }

//    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
//        val dialogRect = Rect()
//        bottomSheetDialog.window?.decorView?.getHitRect(dialogRect)
//
//        if (!dialogRect.contains(ev.x.toInt(), ev.y.toInt())) {
//            // Touch event is outside of the dialog, handle it as needed.
//            // You can also dismiss the dialog if needed: bottomSheetDialog.dismiss()
//        }
//
//        // Always pass the event to the underlying views.
//        return super.dispatchTouchEvent(ev)
//    }


    private fun editScreen() {
        bottomSheetDialog = BottomSheetDialog(this)

        binding.editContactBtn.setOnClickListener {

            val bottomSheetFragment = EditProfileBottomSheetFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

            val window = bottomSheetDialog.window
            window?.decorView?.setBackgroundResource(android.R.color.transparent) // Set transparent background
            window?.setGravity(Gravity.BOTTOM) // Align the dialog to the bottom of the screen
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

            // Apply blur effect
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.5f) // Set to desired dim amount, 0.0f for no dim, 1.0f for full dim

            // Set the background color of the BottomSheetDialog to transparent
            bottomSheetDialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.setBackgroundResource(android.R.color.transparent)


        }
    }



    @SuppressLint("ResourceAsColor", "SuspiciousIndentation")
    private fun observerSocialListData() {
        progressBarHelper.showProgressDialog()
        socialDataViewModel.observbleSocialLinkList().observe(this) { ListData ->

            try {

                progressBarHelper.hideProgressDialog()

                if(ListData.isEmpty()){
                binding.noListSocialImg.isVisible = true
//                binding.viewAllTv.isVisible = false

            }else {
                binding.viewAllTv.isVisible = true
                 binding.noListSocialImg.isVisible = false
                }

                // Initially show the first three items
                val initialItems = ListData.toMutableList().take(4).toMutableList()
                addTADataToAdapter(initialItems, ListData)

                socialGetAdapter.notifyDataSetChanged()


            } catch (e: Exception) {
                e.printStackTrace()
                TastyToast.makeText(this, "There is a problem with the server. Please try again!!", TastyToast.LENGTH_LONG, TastyToast.ERROR)

//                Toast.makeText(this, "There is a problem with the server. Please try again!!", Toast.LENGTH_SHORT).show()

            }finally {
//                Toast.makeText(this, "There is a problem with server please try again!!",Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun addTADataToAdapter(initialItems: MutableList<CategoryData>, allItems: MutableList<CategoryData>) {


        try {

            socialGetRV = binding.socialGetListRv

            socialGetAdapter = SocialGetListAdapter(this, initialItems) { selectedItem ->
                // Handle item click as needed
                // For example, you can open a detailed view or perform some action
            }

            socialGetAdapter.onItemClick = { url ->

                try {

                    val url = url.links?.link

                    // Handle item click by opening the web browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                    if (e is ActivityNotFoundException) {
                        // Display a Toast message
                        TastyToast.makeText(this, "Please check URL", TastyToast.LENGTH_LONG, TastyToast.INFO)

//                        Toast.makeText(this, "Please check URL", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            socialGetRV.layoutManager = GridLayoutManager(this, 2)
//            socialGetAdapter = SocialGetListAdapter(this,initialItems)

            socialGetRV.adapter = socialGetAdapter
            socialGetAdapter.onMessageLongClickListener = this


            binding.viewAllTv.setOnClickListener {
                // When "View All" is clicked, show all items in a bottom sheet
                showBottomSheet(allItems)
            }
//            socialGetAdapter.itemClickListener = this
            socialGetAdapter.notifyDataSetChanged()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMessageLongClick(item: CategoryData, position: Int) {
        showDeleteConfirmationDialog(item, position)
    }

    private fun showDeleteConfirmationDialog(item: CategoryData, position: Int) {

        val mDialog = MaterialDialog.Builder(this)
            .setTitle("Delete?")
            .setMessage("Are you sure want to delete this file?")
            .setAnimation(R.raw.technologies)
            .setCancelable(false)
            .setPositiveButton(
                "Delete", com.example.quickconnect.R.drawable.delete_icon
            ) { dialogInterface, which ->
                // Delete Operation
                // Remove item from the adapter and update the list
                socialGetAdapter.removeItem(position)
                socialGetAdapter.notifyDataSetChanged()
//                // Step 4: Update backend
                deleteSocialItemFromBackend(item.links!!.id!!)
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                "Cancel", com.example.quickconnect.R.drawable.clear_icon
            ) { dialogInterface, which -> dialogInterface.dismiss() }
            .build()

        // Show Dialog
        mDialog.show()

//        AlertDialog.Builder(this, R.style.AlertDialogStyle)
//            .setTitle("Delete Item")
//            .setMessage("Are you sure you want to delete this item?")
//            .setPositiveButton("Yes") { _, _ ->
//                // Remove item from the adapter and update the list
//                socialGetAdapter.removeItem(position)
//                socialGetAdapter.notifyDataSetChanged()
//                // Step 4: Update backend
//                deleteSocialItemFromBackend(item.links!!.id!!)
//            }
//            .setNegativeButton("No", null)
//            .show()
    }

    private fun deleteSocialItemFromBackend(itemId: Int) {

        socialDataViewModel.socialLinkDelete("$userToken", itemId)
//        socialDataViewModel.socialLinkList("$userToken")
//        observerSocialListData()
    }

    @SuppressLint("ResourceAsColor", "SuspiciousIndentation")
    private fun observableSocialLinkDelete() {
        socialDataViewModel.observableSocialLinkDelete().observe(this) { deleteData ->
            try {
                // Handle the response from the API if needed
                Log.d("DeleteItem", "Deleted successfully: ${deleteData?.message}")

                val toast = TastyToast.makeText(this, "Removed Successfully", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()

                socialDataViewModel.socialLinkList("$userToken")


//                Toast.makeText(this, "Removed Successfully", Toast.LENGTH_SHORT).show()

                socialGetAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun showBottomSheet(allItems: MutableList<CategoryData>) {
        val bottomSheetFragment = SocialProfileFragment(allItems)
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }


    private fun photoUpdate() {
        editProfileViewModel.profileChange(
            "$userToken",
            "data:image/png;base64,$encodedImage")
    }

    fun observePhotoData(){

        editProfileViewModel.observePhotoUpdate().observe(this){photoData ->

            try {

//            if (photoData.status == true){
            Log.e("TAG", "observePhotoData: $photoData", )

               val imageData = "${RetrofitClient.PROD_URL}${photoData.data!!.profilePic}"

                getProfileData()
//            profileR = imageData

            Log.e("TAG", "profileR:$profileR ", )


//            val editor = sharedPreferences.edit()
//            editor.putString("profile", profileR)
//            editor.apply()
//            }

            setProfile()

            }catch (e: Exception){
                e.printStackTrace()
            }

        }


    }

    fun getProfileData() {
        qrCodeViewModel.qrCode("$userToken")
    }

    fun observProfileData() {
//        progressBarHelper.showProgressDialog()
        qrCodeViewModel.observeQrData().observe(this) { getProfileData ->

            try {

//            progressBarHelper.hideProgressDialog()
            Log.e(ContentValues.TAG, "observerQrData: $getProfileData",)

//            updateQrDataUI(getProfileData)
//

            val fullNameR = getProfileData.data.fullName
            val EmailR = getProfileData.data.email
            val workAtR = getProfileData.data.workAt
            val descR = getProfileData.data.description
                profileR = getProfileData.data.profilePic.toString()

            val editor = sharedPreferences.edit()
            editor.putString("fullName", fullNameR)
            editor.putString("workAt", workAtR)
            editor.putString("desc", descR)
            editor.putString("profile", profileR)
             editor.apply()

            setProfile()

            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    private fun updateQrDataUI(qrData: QrData) {
        // Assuming you have methods to update UI components
        fullNameTv.text = qrData.data.fullName
        workAtTv.text = qrData.data.workAt
        descriptionTv.text = qrData.data.description
    }


    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this, R.style.AlertDialogStyle)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems =
            arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> setupMediaPermissions()
                1 -> setupPermissions()
            }
        }
        pictureDialog.show()

    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {

            if (userCamPermissionFlag == "") {
                Log.e("TAG", "setupMediaPermissions: $userCamPermissionFlag")
                val builder = AlertDialog.Builder(this)
                builder.setMessage(Alerts.camPermission)
                    .setTitle("Permission Required")
                    .setPositiveButton("OK") { dialog, id ->
                        makeRequest()
                    }
                builder.setNegativeButton("Not now") { dialog, id -> }

                val dialog = builder.create()
                dialog.show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(Alerts.camPermission)
                    .setTitle("Permission Required")
                    .setPositiveButton("Go to Settings") { dialog, id ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }

                builder.setNegativeButton("Not now") { dialog, id -> }

                val dialog = builder.create()
                dialog.show()
            }
        } else {

            takePhotoFromCamera()
        }
    }

    private fun setupMediaPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {

            if (userMedPermissionFlag == "") {
                Log.e("TAG", "setupMediaPermissions: $userMedPermissionFlag",)
                val builder = AlertDialog.Builder(this, R.style.AlertDialogStyle)
                builder.setMessage(Alerts.medPermission)
                    .setTitle("Permission Required")
                    .setPositiveButton("OK") { dialog, id ->
                        makeMediaRequest()
                    }
                builder.setNegativeButton("Not now") { dialog, id -> }

                val dialog = builder.create()
                dialog.show()
            } else {
                val builder = AlertDialog.Builder(this, R.style.AlertDialogStyle)
                builder.setMessage(Alerts.medPermission)
                    .setTitle("Permission Required")
                    .setPositiveButton("Go to Settings") { dialog, id ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)

                    }
                builder.setNegativeButton("Not now") { dialog, id -> }

                val dialog = builder.create()
                dialog.show()
            }
        } else {
            choosePhotoFromGallary()
        }
    }

    fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    fun makeMediaRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_REQUEST_CODE
        )
    }

    private fun takePhotoFromCamera() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            // Create the File where the photo should go
            photoUri = createImageFile()!!

            Log.e("TAG", "takePhotoFromCamera: $photoUri",)


            Log.e("TAG", "takePhotoFromCamera: checkpoint5")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, Camera)

        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun createImageFile(): Uri? {
        val image = File(applicationContext.filesDir, "camera_photo.png")

        Log.e("TAG", "createImageFile: $image",)
        return FileProvider.getUriForFile(
            applicationContext,
            "com.example.quickconnect.fileProvider",
            image
        )
    }

    private fun choosePhotoFromGallary() {
        try {

            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(galleryIntent, Gallery)

        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val wallpaperDirectory = File(
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.

        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }

        try {

            val f = File(
                wallpaperDirectory, ((Calendar.getInstance()
                    .getTimeInMillis()).toString() + ".jpg")
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                this,
                arrayOf(f.getPath()),
                arrayOf("image/jpeg"), null
            )
            fo.close()


            return f.getAbsolutePath()
        } catch (e1: IOException) {
            e1.printStackTrace()

        }

        return ""
    }




    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("ResourceAsColor")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)



        if (requestCode == GALLERY)
        {
            if (data != null)
            {

                val contentURI = data.data

                contentURI?.let {
                    loadImage(contentURI)
                } ?: run {
                    // Handle the case when selectedImageUri is null
                    Toast.makeText(this, "Selected image is null", Toast.LENGTH_SHORT).show()
                }


                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentURI)
                val imagePath = getRealPathFromURI(contentURI!!)
                val rotatedBitmap = handleImageOrientation(bitmap, imagePath)



                Log.e("TAG", "onActivityResult: $contentURI", )


                val path = saveImage(rotatedBitmap)

                if(contentURI.toString().endsWith(".gif")|| contentURI.toString().endsWith(".bmv")
                    || contentURI.toString().endsWith(".raw")){

                    Toast.makeText(this, "Format not supported", Toast.LENGTH_SHORT).show()

                }
                else
                {
                    try {

                        val imageStream =
                            contentURI.let { contentResolver.openInputStream(Uri.parse(contentURI.toString())) }
                        val camImageBitmap = BitmapFactory.decodeStream(imageStream)

                        encodedImage = encodeImage(rotatedBitmap).toString()

                        val editor = sharedPreferences.edit()
                        editor.putString("encodeImage", encodedImage)
                        editor.apply()

                        photoUpdate()
                        getProfileData()


                        Log.e("TAG", "onActivityResultencode: $encodedImage", )

                        Toast.makeText(
                            this,
                            "Image Saved!",
                            Toast.LENGTH_SHORT
                        ).show()

                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            "Failed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }catch (e: UninitializedPropertyAccessException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            "Failed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


        else if (requestCode == CAMERA) {
            try {

//                val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
//                val imagePath = getRealPathFromURI(photoUri)
//                val rotatedBitmap = handleImageOrientation(imageBitmap, imagePath)
//                Log.e("TAG", "onActivityResultcam: $imageBitmap", )
//                val thumbnail = resizeImage(rotatedBitmap)
//                val camImageBitmap = thumbnail.toString()


                val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
                val imagePath = getRealPathFromURI(photoUri)

                // Determine orientation and rotate if necessary
                val rotatedBitmap = handleImageOrientation(imageBitmap, imagePath)

                // Rotate again if necessary to ensure portrait mode
                val portraitBitmap = if (rotatedBitmap.width > rotatedBitmap.height) {
                    rotateImage(rotatedBitmap, 90f)
                } else {
                    rotatedBitmap
                }


            val thumbnail = resizeImage(portraitBitmap)


//                binding.initialsCardView.setImageBitmap(thumbnail)
                encodedImage = encodeImage(thumbnail).toString()

                val editor = sharedPreferences.edit()
                editor.putString("encodeImage", encodedImage)
                editor.apply()

                photoUpdate()
                getProfileData()
                Log.e("TAG", "onActivityResultencodecam: $encodedImage", )
                saveImage(thumbnail)
//                if (encodedImage.isNotEmpty()){
//                    binding.addPic.setText("UpdateProfile")
//                }
                Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT)
//                    .show()
            }catch (e: NullPointerException) {
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "There has been a problem please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }catch (e: UninitializedPropertyAccessException) {
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "There has been a problem please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun handleImageOrientation(bitmap: Bitmap, imagePath: String): Bitmap {
        try {
            if (imagePath.isNotEmpty()) {
                val file = File(imagePath)
                if (file.exists()) {
                    val ei = ExifInterface(file)
                    val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                    return when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                        else -> bitmap
                    }
                } else {
                    Log.e("TAG", "File not found at path: $imagePath")
                }
            } else {
                Log.e("TAG", "Empty file path provided")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("TAG", "IOException occurred while handling image orientation")
        }
        // If any exception occurs or file doesn't exist, return the original bitmap
        return bitmap
    }


//    private fun handleImageOrientation(bitmap: Bitmap, imagePath: String): Bitmap {
//        val ei = ExifInterface(imagePath)
//        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
//        val rotatedBitmap: Bitmap
//        rotatedBitmap = when (orientation) {
//            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
//            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
//            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
//            else -> bitmap
//        }
//        return rotatedBitmap
//    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun getRealPathFromURI(uri: Uri): String {
        var filePath = ""
        try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    filePath = it.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return filePath
    }


//    private fun getRealPathFromURI(uri: Uri): String {
//        val cursor = contentResolver.query(uri, null, null, null, null)
//        cursor?.moveToFirst()
//        val columnIndex = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
//        val filePath = cursor?.getString(columnIndex!!)
//        cursor?.close()
//        return filePath ?: ""
//    }


    private fun loadImage(imageUri: Uri) {
        // Clear the existing image in the profile_pic ImageView
        profile_pic!!.setImageResource(android.R.color.transparent)

        Picasso.get()
            .load(imageUri)
            .error(R.drawable.profile_placeholder)
            .placeholder(R.drawable.profile_placeholder)
            .into(profile_pic)

        // Request layout to ensure the ImageView is properly laid out
        profile_pic!!.requestLayout()
        profile_pic!!.invalidate()

        // Post a delayed action to retry loading the image after a short delay
        profile_pic!!.postDelayed({
            // Get the drawable from the ImageView
            val drawable = profile_pic!!.drawable

            if (drawable != null && drawable is BitmapDrawable) {
                // Get the bitmap from the BitmapDrawable
                val bitmap = drawable.bitmap

                // Check if the bitmap is not null
                if (bitmap != null) {
                    // Calculate the bottom 20% of the image
                    val bottomBitmap = Bitmap.createBitmap(bitmap, 0,
                        (bitmap.height * 0.8).toInt(), bitmap.width, (bitmap.height * 0.2).toInt()
                    )

                    // Use Palette to extract the dominant color from the bottomBitmap
                    Palette.from(bottomBitmap).generate { palette ->
                        val backgroundColor = palette?.getDominantColor(
                            ContextCompat.getColor(this, R.color.black)
                        ) ?: ContextCompat.getColor(this, R.color.black)

                        // Set the background color for your ScrollView
                        findViewById<LinearLayout>(R.id.bg_layout_linear).setBackgroundColor(backgroundColor)
                    }

                    // Upload the image to the server (replace this with your actual upload logic)
//                    uploadImageToServer(bitmap)
                } else {
                    // Handle the case when the bitmap is null
                    Toast.makeText(this, "Error loading image: Bitmap is null", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle the case when the drawable is not a BitmapDrawable
                Toast.makeText(this, "Error loading image: Not a BitmapDrawable", Toast.LENGTH_SHORT).show()
            }
        }, 500) // Delay for 500 milliseconds before retrying
    }



//    private fun loadImage(imageUri: android.net.Uri) {
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
//



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


    private fun loadImageServer(imageUri: String) {
        Picasso.get()
            .load(imageUri)
            .error(R.drawable.profile_placeholder)
            .placeholder(R.drawable.profile_placeholder)
            .into(profile_pic)

        profile_pic!!.postDelayed({
            val drawable = profile_pic!!.drawable

            if (drawable != null && drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap

                if (bitmap != null) {
                    val bottomBitmap = Bitmap.createBitmap(bitmap, 0,
                        (bitmap.height * 0.8).toInt(), bitmap.width, (bitmap.height * 0.2).toInt()
                    )

//                if (bitmap != null) {
//                    val bottomBitmap = Bitmap.createBitmap(bitmap, 0,
//                        (bitmap.height * 0.9).toInt(), bitmap.width, (bitmap.height * 0.1).toInt()
//                    )

                    Palette.from(bottomBitmap).generate { palette ->
                        val backgroundColor = palette?.getDominantColor(
                            ContextCompat.getColor(this, R.color.black)
                        ) ?: ContextCompat.getColor(this, R.color.black)

                        // Apply gradient to the background
//                        findViewById<LinearLayout>(R.id.linear_bg).setBackgroundColor(backgroundColor)
                        applyGradientBackground(backgroundColor)
//                        applyGradientBackground2(backgroundColor)
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

        findViewById<LinearLayout>(R.id.bg_layout_linear).background = gradientDrawable
//        findViewById<LinearLayout>(R.id.text_bg_linear).background = gradientDrawable
//        findViewById<LinearLayout>(R.id.linear_bg).background = gradientDrawable


        // Calculate the brightness of the background color
        val backgroundBrightness = calculateBrightness(color)

        // Choose a text color that is bright and distinct from the background
        val textColor = if (backgroundBrightness < 128) {
            // Use a bright color for dark backgrounds
            ContextCompat.getColor(this, R.color.white)
        } else {
            // Use a bright color for light backgrounds
            ContextCompat.getColor(this, R.color.black)
        }


//        // Dynamically set text color based on the contrast with the background color
//        val textColor = if (calculateBrightness(color)) {
//            ContextCompat.getColor(this, R.color.black) // Use black text for dark background
//        } else {
//            ContextCompat.getColor(this, R.color.white) // Use white text for light background
//        }

//        // Set text color for TextViews
        findViewById<TextView>(R.id.fullname_tv).setTextColor(textColor)
        findViewById<TextView>(R.id.work_at_tv).setTextColor(textColor)
        findViewById<TextView>(R.id.desc_tv).setTextColor(textColor)
//        // Set text color for other TextViews as needed
//        findViewById<Button>(R.id.editContact_btn).setTextColor(textColor)
//        findViewById<TextView>(R.id.add_more_tv).setTextColor(textColor)
//        findViewById<TextView>(R.id.view_all_tv).setTextColor(textColor)
    }


//    private fun applyGradientBackground2(color: Int) {
//        val gradientDrawable = GradientDrawable(
//            GradientDrawable.Orientation.BOTTOM_TOP,
//            intArrayOf(color, ContextCompat.getColor(this, R.color.black))
//        )
//
////        findViewById<LinearLayout>(R.id.bg_layout_linear).background = gradientDrawable
////        findViewById<LinearLayout>(R.id.text_bg_linear).background = gradientDrawable
//        findViewById<LinearLayout>(R.id.linear_bg).background = gradientDrawable
//
//
//        // Dynamically set text color based on the contrast with the background color
//        val textColor = if (isColorDark(color)) {
//            ContextCompat.getColor(this, R.color.black) // Use black text for dark background
//        } else {
//            ContextCompat.getColor(this, R.color.white) // Use white text for light background
//        }
//
//        // Set text color for TextViews
//        findViewById<TextView>(R.id.fullname_tv).setTextColor(textColor)
//        findViewById<TextView>(R.id.work_at_tv).setTextColor(textColor)
//        findViewById<TextView>(R.id.desc_tv).setTextColor(textColor)
////        // Set text color for other TextViews as needed
////        findViewById<Button>(R.id.editContact_btn).setTextColor(textColor)
////        findViewById<TextView>(R.id.add_more_tv).setTextColor(textColor)
////        findViewById<TextView>(R.id.view_all_tv).setTextColor(textColor)
//    }
private fun calculateBrightness(color: Int): Double {
    // Calculate brightness using the formula (0.299*R + 0.587*G + 0.114*B)
    return 0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)
}

//    private fun isColorBright(color: Int): Boolean {
//        // Calculate brightness using the formula (0.299*R + 0.587*G + 0.114*B)
//        val brightness =
//            0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)
//        return brightness > 128 // You can adjust this threshold as needed
//    }

//
//    private fun isColorDark(color: Int): Boolean {
//        // Calculate darkness based on the perceived luminance of the color
//        val luminance = 0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)
//        return luminance < 128
//    }

//    private fun isColorDark(color: Int): Boolean {
//        val darkness =
//            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
//        return darkness >= 0.5
//    }


    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }

    fun resizeImage(yourBitmap: Bitmap) : Bitmap {
        val resized = Bitmap.createScaledBitmap(yourBitmap, 1000, 700, true)

        return resized
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    TastyToast.makeText(this, "Permission denied", TastyToast.LENGTH_LONG, TastyToast.WARNING)

//                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    userCamPermissionFlag = "yes"
                    val editor = permissionsharedPreferences.edit()
                    editor.putString("camFlag", userCamPermissionFlag)
                    editor.apply()
                } else {
                    takePhotoFromCamera()
                    TastyToast.makeText(this, "Permission granted", TastyToast.LENGTH_LONG, TastyToast.DEFAULT)

//                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                }
            }

            READ_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    userMedPermissionFlag = "yes"
                    val editor = permissionsharedPreferences.edit()
                    editor.putString("medFlag", userMedPermissionFlag)
                    editor.apply()
                } else {
                    choosePhotoFromGallary()
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun callActionList() {

        callToActionViewModel.callToActionList("$userToken")
    }

    private fun observerCallToListData() {

        // Check if there is cached data in SharedPreferences
//        val cachedSocialListJson = sharedPreferences.getString(CALL_LIST_KEY, null)
//        if (cachedSocialListJson != null) {
//            val cachedSocialList = Gson().fromJson<List<CategoryData>>(
//                cachedSocialListJson,
//                object : TypeToken<List<CategoryData>>() {}.type
//            )
//            addTADataToAdapter(cachedSocialList)
//        }
        callToActionViewModel.observbleCallActionList().observe(this) { actionList ->
            Log.e("TAG", "observerCallToListData:$actionList")

            try {


            if (actionList.isEmpty()){
                binding.noListImg.isVisible = true
            }else {
                addTADataToAdapter(actionList)
                binding.noListImg.isVisible = false

                callToActionAdapter.notifyDataSetChanged()
            }

            callToActionAdapter.onItemClick = { url ->

                try {

                    val url = url.link

                    // Handle item click by opening the web browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
                    startActivity(intent)
                }catch (e:ActivityNotFoundException){
                    e.printStackTrace()
                    // Check if the caught exception is an ActivityNotFoundException
                    if (e is ActivityNotFoundException) {
                        // Display a Toast message
                        TastyToast.makeText(this, "Please check URL", TastyToast.LENGTH_LONG, TastyToast.INFO)

//                        Toast.makeText(this, "Please check URL", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Save the latest data to SharedPreferences for caching
            val json = Gson().toJson(actionList)
            sharedPreferences.edit().putString(CALL_LIST_KEY, json).apply()


        }catch (e: Exception){
            e.printStackTrace()
            }
        }
    }

    fun addTADataToAdapter(actionListListAll: MutableList<ActionList>) {
        try {
            // Initialize the adapter if it's null
//            if (callToActionAdapter == null) {
                callToActionAdapter = CallToActionAdapter(actionListListAll, this)
                binding.callToactionRv.layoutManager = GridLayoutManager(this, 2)
                binding.callToactionRv.adapter = callToActionAdapter
                callToActionAdapter.onMessageLongClickListener = this

//            } else {
                // Update the existing list in the adapter
                callToActionAdapter.actionCallList = actionListListAll
                callToActionAdapter.notifyDataSetChanged()
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onMessageLongClick(item: ActionList, position: Int) {
        showDeleteConfirmationDialog(item, position)
    }

    private fun showDeleteConfirmationDialog(item: ActionList, position: Int) {

        val mDialog = MaterialDialog.Builder(this)
            .setTitle("Delete?")
            .setMessage("Are you sure want to delete this file?")
            .setAnimation(R.raw.technologies)
            .setCancelable(false)
            .setPositiveButton(
                "Delete", R.drawable.delete_icon
            ) { dialogInterface, which ->
                // Delete Operation
                // Remove item from the adapter and update the list
                callToActionAdapter.removeItem(position)
                callToActionAdapter.notifyDataSetChanged()
                // Step 4: Update backend
                deleteItemFromBackend(item.id!!)
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                "Cancel", R.drawable.clear_icon
            ) { dialogInterface, which -> dialogInterface.dismiss() }
            .build()

        // Show Dialog
        mDialog.show()

//        AlertDialog.Builder(this, R.style.AlertDialogStyle)
//            .setTitle("Delete Item")
//            .setMessage("Are you sure you want to delete this item?")
//            .setPositiveButton("Yes") { _, _ ->
//                // Remove item from the adapter and update the list
//                callToActionAdapter.removeItem(position)
//                callToActionAdapter.notifyDataSetChanged()
//                // Step 4: Update backend
//                deleteItemFromBackend(item.id!!)
//            }
//            .setNegativeButton("No", null)
//            .show()
    }

    private fun deleteItemFromBackend(itemId: Int) {
        // Implement backend logic to delete the item
        // This could involve calling a ViewModel or Repository method
        // to update the data in your backend system.
        callToActionViewModel.deleteAction("$userToken", itemId)
        callToActionAdapter.notifyDataSetChanged()
//        callToActionViewModel.callToActionList("$userToken")
    }

    @SuppressLint("ResourceAsColor", "SuspiciousIndentation")
    private fun observableCallLinkDelete() {
        callToActionViewModel.observbleDeleteAction().observe(this) { deleteData ->
            try {
                // Handle the response from the API if needed
                Log.d("DeleteItem", "Deleted successfully: ${deleteData?.message}")

                val toast = TastyToast.makeText(this, "Removed Successfully", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()
                        callToActionViewModel.callToActionList("$userToken")

//                binding.noListImg.isVisible = true

//                Toast.makeText(this, "Removed Successfully", Toast.LENGTH_SHORT).show()

                socialGetAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


//    override fun onProfileUpdated() {
//        // Refresh the data in MainActivity or perform any necessary actions
//        getProfileData()
//        observProfileData()
//        setProfile()
//
//        Log.e("TAG", "onProfileUpdated:", )
//    }


    override fun onResume() {
        super.onResume()
        socialDataViewModel.socialLinkList("$userToken")
        callActionList()
//        callToActionViewModel.callToActionList("$userToken")

//        observerSocialListData()
//        binding.noListSocialImg.isVisible = true
//
    }

    override fun onApiCallCompleted() {
        callActionList()
    }

    override fun onApiCallCompleted2() {

        Log.e("TAG", "onApiCallCompleted2: ", )
        getProfileData()
       observProfileData()
        setProfile()
    }

}

