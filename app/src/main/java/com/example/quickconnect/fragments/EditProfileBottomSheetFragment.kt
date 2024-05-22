package com.example.quickconnect.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.quickconnect.R
import com.example.quickconnect.activities.MainActivity
import com.example.quickconnect.databinding.EditContactDetailsBottomdialogBinding
import com.example.quickconnect.repository.EditProfileRepositary
import com.example.quickconnect.repository.QrCodeRepositary
import com.example.quickconnect.utils.NoInternetUtils
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.EditProfileViewModel
import com.example.quickconnect.viewmodels.QrCodeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class EditProfileBottomSheetFragment : BottomSheetDialogFragment() {


    private lateinit var binding: EditContactDetailsBottomdialogBinding
    lateinit var progressBarHelper: ProgressBarHelper
    lateinit var sharedPreferences: SharedPreferences
    lateinit var permissionsharedPreferences: SharedPreferences
    //    private var editProfileListener: EditProfileListener? = null
    private var mainActivityListener: MainActivityListener? = null
    var encodeIImager: String = ""
    private var userCamPermissionFlag: String = ""
    private var userMedPermissionFlag: String = ""
    lateinit var fullName: EditText
    lateinit var workAt: EditText
    lateinit var mobileNumber: EditText
    lateinit var SeconderyMobileNum: EditText
    lateinit var Email: EditText
    lateinit var Description: EditText
    lateinit var doneBtn: TextView
    lateinit var userSharedPreferences: SharedPreferences
    lateinit var userToken: String


    private val editProfileViewModel: EditProfileViewModel by lazy {
        val editProfileViewModelFactory = EditProfileRepositary()
        ViewModelProvider(this, editProfileViewModelFactory)[EditProfileViewModel::class.java]
    }

    private val qrCodeViewModel: QrCodeViewModel by lazy {
        val qrCodeViewModelFactory = QrCodeRepositary()
        ViewModelProvider(this, qrCodeViewModelFactory)[QrCodeViewModel::class.java]
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), theme)
        bottomSheetDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                dialog.setCancelable(true)

                // Set the background color
                val backgroundColor = ContextCompat.getColor(requireContext(), R.color.light_ashtr)
                sheet.setBackgroundColor(backgroundColor)


                // Apply blur effect to the background with semi-transparent overlay
                applyBlurWithOverlay(sheet)
            }
        }
        return bottomSheetDialog
    }

    private fun applyBlurWithOverlay(view: View) {
        // Create a bitmap of the view's content
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        // Create a semi-transparent overlay
        val overlayColor = Color.parseColor("#99CCCCC8") // Adjust transparency as needed
        val overlayPaint = Paint()
        overlayPaint.color = overlayColor
        canvas.drawRect(0f, 0f, view.width.toFloat(), view.height.toFloat(), overlayPaint)

        // Apply blur effect to the bitmap
        val blurredBitmap = blurBitmap(bitmap, 25f) // Adjust blur radius as needed

        // Set the blurred bitmap as the background
        val drawable = BitmapDrawable(resources, blurredBitmap)
        view.background = drawable
    }

    private fun blurBitmap(bitmap: Bitmap, radius: Float): Bitmap {
        val rsContext = RenderScript.create(requireContext())
        val blurScript = ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext))

        val input = Allocation.createFromBitmap(rsContext, bitmap)
        val output = Allocation.createTyped(rsContext, input.type)

        blurScript.setRadius(radius)
        blurScript.setInput(input)
        blurScript.forEach(output)

        output.copyTo(bitmap)

        rsContext.destroy()

        return bitmap
    }


    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        binding = EditContactDetailsBottomdialogBinding.inflate(layoutInflater)

        if (!NoInternetUtils.isInternetAvailable(requireContext())) {
            NoInternetUtils.showNoInternetDialog(requireContext())
        }

        userSharedPreferences =
            requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        userToken = userSharedPreferences.getString("userToken", "").toString()

        sharedPreferences =
            requireContext().getSharedPreferences("ProfileData", Context.MODE_PRIVATE)
        encodeIImager = sharedPreferences.getString("encodeImage", "").toString()

        permissionsharedPreferences =
            requireContext().getSharedPreferences("PermissionFlag", Context.MODE_PRIVATE)
        userCamPermissionFlag = permissionsharedPreferences.getString("camFlag", "").toString()
        userMedPermissionFlag = permissionsharedPreferences.getString("medFlag", "").toString()

        Log.e("TAG", "onCreateView: $userCamPermissionFlag + $userMedPermissionFlag")

        Initilization()

        doneBtn.setOnClickListener {
            updateProfile()
        }

        observeEditProfileData()
        getProfileData()
        observProfileData()
        getData()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getProfileData()
        observeEditProfileData()
        observProfileData()
        getData()

    }


    private fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailRegex.matches(email)
    }

    private fun updateProfile() {


        val fullNameString = fullName.text.trim().toString()
        val emailString = Email.text.trim().toString()
        val workAtString = workAt.text.trim().toString()
        val mobileString = mobileNumber.text.trim().toString()
        val secondaryNumString = SeconderyMobileNum.text.trim().toString()
        val descString = Description.text.trim().toString()

        if (emailString.isNotEmpty() && !isEmailValid(emailString)) {
            // Show an alert because the email is not valid
            showAlert("Enter valid Email")
            return
        }

        try {


            editProfileViewModel.editProfile(
                "$userToken",
                fullNameString,
                emailString,
                mobileString,
                secondaryNumString,
                workAtString,
                descString
            )

            Log.e(
                "TAG",
                "updateProfile:$userToken + $fullNameString + " + "$emailString + $mobileString + $secondaryNumString + $workAtString + $descString",
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Alert")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
            // Handle the OK button click if needed
        }
        builder.show()
    }

    private fun Initilization() {

        progressBarHelper = ProgressBarHelper(requireActivity())


        fullName = binding.fullnameEt
        Email = binding.emailEt
        workAt = binding.workAtEt
        mobileNumber = binding.phoneNumEt
        SeconderyMobileNum = binding.secondryNumEt
        Description = binding.descEt
        doneBtn = binding.doneTv
//        prfile_pic = requireActivity().findViewById(R.id.profile_Iv)


        binding.terminate.setOnClickListener {
            dialog!!.dismiss()
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissAllowingStateLoss()
        // Handle any actions needed when the bottom sheet is dismissed
    }



    fun getData() {


        val name = sharedPreferences.getString("fullNameR", "").toString()
        val email = sharedPreferences.getString("email", "").toString()
        val workat = sharedPreferences.getString("workAtR", "").toString()
        val mobile = sharedPreferences.getString("phoneNumberR", "").toString()
        val secmobile = sharedPreferences.getString("seconderyNumR", "").toString()
        val desc = sharedPreferences.getString("descR", "").toString()


//        if (name.isNotEmpty()) {

//            fullName.setText(name)
//            Email.setText(email)
//            workAt.setText(workat)
//            mobileNumber.setText(mobile)
//            SeconderyMobileNum.setText(secmobile)
//            Description.setText(desc)
//        }

    }

    @SuppressLint("ResourceAsColor")
    fun observeEditProfileData() {
        editProfileViewModel.observeEditProfileData().observe(this) { editProfileData ->
            try {

                // Handle the response from the API if needed
                Log.d("DeleteItem", "Deleted successfully: ${editProfileData}")

//                if (editProfileData.data!!.profilePic!!.isNotEmpty()) {
//                    binding.upLoadPhtotoBtn.setBackgroundColor(R.color.green)
//                }

                mainActivityListener?.onApiCallCompleted2()



                dismiss()


//
//                val fullNameR = editProfileData.data!!.fullName
//                val workAtR = editProfileData.data!!.workAt
//                val phoneNumberR = editProfileData.data!!.primaryPhoneNumber
//                val seconderyNumR = editProfileData.data!!.email
//                val descR = editProfileData.data!!.description
//
//                val editor = sharedPreferences.edit()
//                editor.putString("fullNameR", fullNameR)
//                editor.putString("workAtR", workAtR)
//                editor.putString("phoneNumberR", phoneNumberR)
//                editor.putString("seconderyNumR", seconderyNumR)
//                editor.putString("descR", descR)
//                editor.apply()

//                socialGetAdapter.submitList(items.toMutableList())

//                socialGetAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getProfileData() {

        qrCodeViewModel.qrCode("$userToken")
        mainActivityListener?.onApiCallCompleted2()

    }

    @SuppressLint("SuspiciousIndentation")
    fun observProfileData() {
        qrCodeViewModel.observeQrData().observe(this) { getProfileData ->

            try {

                Log.e(ContentValues.TAG, "observerQrData: $getProfileData")

                val fullNameR = getProfileData.data.fullName
                val EmailR = getProfileData.data.email
                val workAtR = getProfileData.data.workAt
                val phoneNumberR = getProfileData.data.primaryPhoneNumber
                val seconderyNumR = getProfileData.data.secondaryPhoneNumber
                val descR = getProfileData.data.description

                mainActivityListener?.onApiCallCompleted2()

                Log.e("TAG", "observProfileData: $fullNameR")

                fullName.setText(fullNameR)
                Email.setText(EmailR)
                workAt.setText(workAtR)
                mobileNumber.setText(phoneNumberR)
                SeconderyMobileNum.setText(seconderyNumR)
                Description.setText(descR)


                val editor = sharedPreferences.edit()
                editor.putString("fullNameR", fullNameR)
                editor.putString("email", EmailR)
                editor.putString("workAtR", workAtR)
                editor.putString("phoneNumberR", phoneNumberR)
                editor.putString("seconderyNumR", seconderyNumR)
                editor.putString("descR", descR)
//            editor.putString("profileR", profileR)
                editor.apply()
                getData()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun onDoneButtonClick() {
        // Perform your data update logic...
        // Call the setProfile() directly in the main activity
        (activity as? MainActivity)?.setProfile()
    }


    interface MainActivityListener {
        fun onApiCallCompleted2()
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is MainActivityListener) {
//            mainActivityListener = context
//        } else {
//            throw ClassCastException("$context must implement MainActivityListener")
//        }
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        mainActivityListener = null
//    }
}
