package com.example.quickconnect.activities

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.quickconnect.R
import com.example.quickconnect.adapters.InnerAdapterForRequests
import com.example.quickconnect.adapters.RequestPermissionAdapter
import com.example.quickconnect.databinding.ActivityProfilesBinding
import com.example.quickconnect.fragments.BookmarkBottomSheetFragment
import com.example.quickconnect.model.notificationmodel.MyRequestDataList
import com.example.quickconnect.model.notificationmodel.RequestUserData
import com.example.quickconnect.model.notificationmodel.SocialRequestsData
import com.example.quickconnect.repository.NotificationRepositary
import com.example.quickconnect.repository.QrCodeRepositary
import com.example.quickconnect.repository.SocialLinksRepositary
import com.example.quickconnect.utils.NoInternetUtils
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.NotificationViewModel
import com.example.quickconnect.viewmodels.QrCodeViewModel
import com.example.quickconnect.viewmodels.SocialLinksViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.camera.CameraSettings
import com.sdsmdg.tastytoast.TastyToast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.regex.Pattern


class ProfilesActivity : AppCompatActivity(), InnerAdapterForRequests.OnItemClickListener,
    InnerAdapterForRequests.OnItemClickListenerForReject,
    InnerAdapterForRequests.OnAcceptAllClickListener{

    private lateinit var binding: ActivityProfilesBinding
    lateinit var progressBarHelper: ProgressBarHelper


    private val CAMERA_PERMISSION_REQUEST = 101

    private lateinit var scannerView: DecoratedBarcodeView

    lateinit var logo: String

     var qrLink : String? = null

    lateinit var requestRv : RecyclerView

    lateinit var innerAdapterForRequest : InnerAdapterForRequests

    lateinit var qrCodeIv: ImageView
    lateinit var nameTv : TextView
    lateinit var backTv : TextView
    lateinit var logoTv : TextView

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userViewSharedPreferences : SharedPreferences
    private var userCamPermissionFlag: String = ""
        lateinit var userToken : String
        lateinit var userNameMy : String
         var requestId : Int? = null


    private  val WRITE_EXTERNAL_STORAGE_REQUEST = 1


    private val qrCodeViewModel: QrCodeViewModel by lazy {
        val qrCodeViewModelFactory = QrCodeRepositary()
        ViewModelProvider(this, qrCodeViewModelFactory )[QrCodeViewModel::class.java]
    }

    private val requestViewModel: NotificationViewModel by lazy {
        val requestViewModelFactory = NotificationRepositary()
        ViewModelProvider(this, requestViewModelFactory )[NotificationViewModel::class.java]
    }

    private val socialDataViewModel: SocialLinksViewModel by lazy {
        val appDataViewModelFactory = SocialLinksRepositary()
        ViewModelProvider(this, appDataViewModelFactory)[SocialLinksViewModel::class.java]
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!NoInternetUtils.isInternetAvailable(this)) {
            NoInternetUtils.showNoInternetDialog(this)
        }

        sharedPreferences = getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        userNameMy = sharedPreferences.getString("userNameMy","").toString()
        userToken = sharedPreferences.getString("userToken","").toString()
        Log.e("TAG", "qrCodeToken:$userToken ", )


        userViewSharedPreferences = getSharedPreferences("UserViewData", Context.MODE_PRIVATE)
        requestId = userViewSharedPreferences.getInt("requestId",0)

        sharedPreferences = getSharedPreferences("PermissionFlag", Context.MODE_PRIVATE)
        userCamPermissionFlag = sharedPreferences.getString("camFlag", "").toString()



        intilization()
        qrCodeData()
        observerQrCodeLiveData()
        observeAcceptRequest()

        if (hasCameraPermission()){
            scannerView.isVisible
        }else{
            requestCameraPermission()
        }



        logo = intent.getStringExtra("Logo").toString()
        Log.e("TAG", "onCreate: $logo")
//        val notofication = intent.getStringExtra("Notification")

//        if (logo == "Logo") {
            profile()
//        }

        qrCode()


    }


    fun intilization() {

        progressBarHelper =  ProgressBarHelper(this)

        qrCodeIv = binding.qrCodeIv
        scannerView = binding.barcodeScanner
        backTv = binding.backTv
        nameTv = binding.name
        logoTv = binding.logoTv

        nameTv.setText(userNameMy)

        backTv.setOnClickListener {
            onBackPressed()
        }

        logoTv.setOnClickListener {
            val profileFragment = BookmarkBottomSheetFragment()
            profileFragment.show(supportFragmentManager, profileFragment.tag)
        }
    }


    private fun observerQrCodeLiveData(){

        qrCodeViewModel.observeQrData().observe(this
        ) { qrData ->

            try {
                if (qrData.status == true) {
                     qrLink = qrData.data.qrcode.toString()
                    Log.e(ContentValues.TAG, "observerUserTokenLiveData: $qrLink", )
                    val userName = qrData.data.username

                    Log.e("TAG", "userName:$userName ", )

                    val editor = sharedPreferences.edit()
                    editor.putString("userNameMy", userName)
                    editor.apply()

                    Log.e("TAG", "userNameMy:$userName ", )

                }

            }catch(e : IOException){
                e.printStackTrace()
            }catch(e : Exception) {
                e.printStackTrace()
            }
        }

    }

    fun qrCodeData(){


        qrCodeViewModel.qrCode("${userToken}")
    }

    fun observeQrData (){
        qrCodeViewModel.observeQrData().observe(this) { qrData ->

//            try {
            Log.e(ContentValues.TAG, "observerQrData: $qrData",)
        }


    }

    fun qrCode() {
        qrCodeIv.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.TransparentBottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.bottom_dialog_qrcode, null)

            bottomSheetDialog.setContentView(view)

            // Set the background color of the BottomSheetDialog to transparent
            bottomSheetDialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.setBackgroundResource(android.R.color.transparent)

            val qrCodeImageView = view.findViewById<ImageView>(R.id.qrCodeImageView)

            val download = bottomSheetDialog.findViewById<AppCompatButton>(R.id.download_qr_btn)


            val uriString: String? = qrLink
            val uri: Uri? = uriString?.let { Uri.parse(it) }

            Log.e("TAG", "uri: $uri", )

            try {
                val imageLoader = ImageLoader.Builder(this)
                    .components {
                        add(SvgDecoder.Factory())
                    }
                    .build()

                uri?.let {
                    val request = ImageRequest.Builder(this)
                        .data(it)
                        .crossfade(true)
                        .error(R.drawable.qr_iv)
                        .target(qrCodeImageView)
                        .build()

                    Log.d("ImageLoader", "Loading image from URI: $it")

                    imageLoader.enqueue(request)

                    download?.setOnClickListener {
                        lifecycleScope.launchWhenCreated {
                            try {
                                val result = (imageLoader.execute(request) as SuccessResult)
                                val bitmap = result.drawable.toBitmap()
                                // Now you can use the bitmap or save it to storage
                                // For example, save it to external storage
                                saveBitmapToStorage(bitmap, "DownloadedImage.png")
                            } catch (e: Exception) {
                                Log.e("ImageLoader", "Error loading image", e)
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("ImageLoader", "Error loading image", e)
            }

            val content = qrLink
            Log.e("TAG", "qrCode: $content")

            bottomSheetDialog.show()
        }
    }

    private fun saveBitmapToStorage(bitmap: Bitmap, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Runtime permission check for devices running Android 6.0 or higher
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission if it's not granted
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_REQUEST)
                return
            }
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }

        val resolver = contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        try {
            if (imageUri != null) {
                val outputStream = resolver.openOutputStream(imageUri)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
                outputStream?.flush()
                outputStream?.close()
                val toast = TastyToast.makeText(this, "QR saved to gallery", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)
                toast.setGravity(Gravity.FILL_VERTICAL,0,0)
                toast.show()

//                Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("ImageLoader", "Error creating image file in MediaStore")
                TastyToast.makeText(this, "Error saving image", TastyToast.LENGTH_LONG, TastyToast.ERROR)

//                Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("ImageLoader", "Error saving image to MediaStore", e)
            TastyToast.makeText(this, "Error saving image", TastyToast.LENGTH_LONG, TastyToast.ERROR)
//            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show()
        }
    }

    fun extractNameFromUrls(url: String, urlsArray: Array<String>): String? {
        for (patternUrl in urlsArray) {
            val pattern = Pattern.compile("$patternUrl([^/]+)/")
            val matcher = pattern.matcher(url)

            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return null
    }


    fun extractNameFromUrl(url: String): String? {
        // Define the pattern to match the name in the URL
        val pattern = Pattern.compile("http://tapni.s3-website.ap-south-1.amazonaws.com/([^/]+)/")
        val matcher = pattern.matcher(url)

        // Check if the pattern is found in the URL
        return if (matcher.find()) {
            // Extract and return the matched name
            matcher.group(1)
        } else {
            null
        }
    }


    fun saveQRCodeToStorage(qrCodeBitmap: Bitmap, fileName: String) {
        // Check if external storage is available
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.WIDTH, qrCodeBitmap.width)
                put(MediaStore.Images.Media.HEIGHT, qrCodeBitmap.height)
            }

            val contentResolver = contentResolver
            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            try {
                val outputStream = uri?.let { contentResolver.openOutputStream(it) }
                qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
                outputStream?.flush()
                outputStream?.close()

                // Notify the user that the QR code has been saved
                TastyToast.makeText(this, "QR Code saved to Pictures", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)
//                Toast.makeText(this, "QR Code saved to Pictures", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle any exceptions that may occur during the file save process
                TastyToast.makeText(this, "Error saving QR Code", TastyToast.LENGTH_LONG, TastyToast.ERROR)

//                Toast.makeText(this, "Error saving QR Code", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle the case where external storage is not available
            TastyToast.makeText(this, "External storage not available", TastyToast.LENGTH_LONG, TastyToast.ERROR)

//            Toast.makeText(this, "External storage not available", Toast.LENGTH_SHORT).show()
        }
    }


    fun profile() {

        try {

        scannerView.isVisible = true
        binding.linearQrName.isVisible = true

        val profileFragment = BookmarkBottomSheetFragment()
        profileFragment.show(supportFragmentManager, profileFragment.tag)
//        profileFragment.isCancelable = false


        // Set up the scanner
        val formats = listOf(BarcodeFormat.QR_CODE)
        scannerView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        scannerView.barcodeView.cameraSettings = CameraSettings().apply {
            requestedCameraId = 0  // Set the desired camera (front or back)
        }
        scannerView.setStatusText("")  // Hide status text (optional)

        // Set up the onDecoded callback
        scannerView.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {
                    // Handle the scanned QR code value
                    val scannedValue = it.result

//                    val extractedName = extractNameFromUrl(scannedValue.toString())

                    // Pass the array of URLs to extractNameFromUrl
                    val extractedName = extractNameFromUrls(scannedValue.toString(), arrayOf(
                        "http://192.168.0.104:8001/",
                        "http://tapni.s3-website.ap-south-1.amazonaws.com/",
                        "http://172.174.177.149/",
                        "http://192.168.0.104:8001/",

                        // Add more URLs as needed
                    ))

//                    val editor = sharedPreferences.edit()
//                    editor.putString("userName",extractedName)
//                    editor.commit()


                    Log.e("TAG", "Scanned QR Code Value: $scannedValue")
                    val intent = Intent(this@ProfilesActivity, UserViewActivity::class.java)
                    intent.putExtra("userName",extractedName)
                    startActivity(intent)

                    Log.e("TAG", "userNamePROFILE:$extractedName ")


                    // Add your logic to process the scanned value as needed
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                // Handle possible result points if needed
            }
        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        )
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scannerView.resume()
                } else {

                    TastyToast.makeText(this, "Camera permission denied", TastyToast.LENGTH_LONG, TastyToast.WARNING)

//                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
            scannerView.pause()
    }

    override fun onResume() {
        super.onResume()

            scannerView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()

            scannerView.barcodeView.decoderFactory =
                DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
    }

    override fun onClickForAccept(item: SocialRequestsData, position: Int) {

        // Remove item from the adapter and update the list
        innerAdapterForRequest.notifyDataSetChanged()


        acceptRequest(item.id!!)

    }

    private fun acceptRequest(id: Int) {
        requestViewModel.permissionStatus("$userToken", id, "grant")
    }

    fun observeAcceptRequest(){
        requestViewModel.observeApprovalStatus().observe(this){acceptRequest ->
        }
    }

    override fun onClickForReject(item: SocialRequestsData, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to reject Request")
            .setPositiveButton("Yes") { _, _ ->
                // Remove item from the adapter and update the list
                innerAdapterForRequest.rejectAccess(position)
                innerAdapterForRequest.notifyDataSetChanged()
                // Step 4: Update backend
                Log.e("TAG", "itemid: ${item.id}", )
                deleteItemFromBackend(item.id!!)
            }
            .setNegativeButton("No", null)
            .show()

    }

    private fun deleteItemFromBackend(itemId: Int) {
        // Implement backend logic to delete the item
        // This could involve calling a ViewModel or Repository method
        // to update the data in your backend system.

        requestViewModel.permissionStatus("$userToken", itemId, "decline")
        innerAdapterForRequest.notifyDataSetChanged()


    }

    override fun onAcceptAllClick(item: SocialRequestsData, position: Int) {
        // Call your ViewModel or Repository method to accept the request
        acceptRequest(item.id!!)
    }
}