package com.example.quickconnect.fragments

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.quickconnect.R
import com.example.quickconnect.activities.MainActivity
import com.example.quickconnect.databinding.FragmentNumberBinding
import com.example.quickconnect.repository.SendOtpRepositary
import com.example.quickconnect.utils.NoInternetUtils
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.SendOtpViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.IOException


class NumberFragment : Fragment() {

    private lateinit var binding: FragmentNumberBinding
    lateinit var progressBarHelper: ProgressBarHelper

    lateinit var mobileNum_et : EditText
    lateinit var mobileNum_Til : TextInputLayout
    lateinit var proceed_btn : AppCompatButton
    lateinit var otp_et : EditText
    lateinit var resend_btn : AppCompatButton
    lateinit var linear_otp : LinearLayout
    lateinit var createPass_til : TextInputLayout
    lateinit var createPass_et : TextInputEditText
    lateinit var OTP_til : TextInputLayout
    lateinit var next_btn : AppCompatButton
    lateinit var reEnter_pass_et : TextInputEditText
    lateinit var reEnter_Til : TextInputLayout
    lateinit var cancelTv : TextView
    lateinit var secure_accounTV : TextView

    lateinit var sharedPreferences: SharedPreferences
    lateinit var userToken : String

    lateinit var createPassString : String
    lateinit var reEnterPassString : String
    lateinit var headName : TextView


    lateinit var numString: String

    private var gestureDetector: GestureDetector? = null


    private val sendOtpViewModel: SendOtpViewModel by lazy {
        val sendOtpViewModelFactory = SendOtpRepositary()
        ViewModelProvider(this, sendOtpViewModelFactory)[SendOtpViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SuspiciousIndentation", "ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentNumberBinding.inflate(layoutInflater)

        if (!NoInternetUtils.isInternetAvailable(requireContext())) {
            NoInternetUtils.showNoInternetDialog(requireContext())
        }

        // Initialize GestureDetector
//        gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
//            override fun onDown(e: MotionEvent): Boolean {
//                // Consume the touch event
//                return true
//            }
//
//            // Override other gesture methods if needed
//        })
//
//        // Set touch listener on the fragment's view
//        requireView().setOnTouchListener { _, event ->
//            // Pass the touch event to GestureDetector
//            gestureDetector?.onTouchEvent(event) ?: false
//        }



        initialization()
        observerOtpLiveData()

        sharedPreferences = requireContext().getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        userToken = sharedPreferences.getString("userToken","").toString()

        if (userToken.isNotEmpty()){
            startActivity(Intent(context, MainActivity::class.java))
        }


        proceed_btn.isVisible = true
        mobileNum_et.isVisible = true
        mobileNum_Til.isVisible = true


        binding.proceedBtn.setOnClickListener {
            sendOtp()
        }

        otp_et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // This method is called to notify you that characters within `charSequence` are about to be replaced with new text.
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // This method is called to notify you that somewhere within `charSequence` the characters between `i` and `i + i1` inclusive have been replaced with new text with a length of `i2`.
            }

            override fun afterTextChanged(editable: Editable) {
                // This method is called to notify you that the characters within `editable` have changed.

                if (editable?.length == 4) {
                    proceed_btn.isVisible = false
                    linear_otp.isVisible = false
                    otp_et.isVisible = false
                    resend_btn.isVisible = false
                    mobileNum_Til.isVisible = false
                    createPass_til.isVisible = true
                    next_btn.isVisible = true
                    cancelTv.isVisible = true
                    secure_accounTV.isVisible = true
                }
            }
        })
        resend_btn.setOnClickListener {


            if (otp_et.text.isEmpty()) {
                Toast.makeText(context, "Enter Otp", Toast.LENGTH_SHORT).show()
            } else if (otp_et.text.length != 4) {
                Toast.makeText(context, "Enter Valid OTP", Toast.LENGTH_SHORT).show()
            } else {

                proceed_btn.isVisible = false
                linear_otp.isVisible = false
                otp_et.isVisible = false
                resend_btn.isVisible = false
                mobileNum_Til.isVisible = false
                createPass_til.isVisible = true
                next_btn.isVisible = true
                cancelTv.isVisible = true
                secure_accounTV.isVisible = true

            }
        }


            next_btn.setOnClickListener {
                if (createPass_et.text!!.isEmpty()){
                    Toast.makeText(context, "Create Password", Toast.LENGTH_SHORT).show()
//                }else if (createPass_et.text!!.length != 4){
//                    Toast.makeText(context, "Password Must be 6 characters", Toast.LENGTH_SHORT).show()
                } else {

                    createPassString = createPass_et.text!!.trim().toString()
                    reEnterPassString = reEnter_pass_et.text!!.trim().toString()


                    val editor = sharedPreferences.edit()
                    editor.putString("CreatePassword", createPassString)
                    Log.e(TAG, "onCreateView: $createPassString")
                    editor.apply()

                    createPass_til.isVisible = false
                    reEnter_Til.isVisible = true

                    if (reEnter_pass_et.text!!.isEmpty()){
                        Toast.makeText(context, "Re-Enter Password", Toast.LENGTH_SHORT).show()
                    }else if (createPassString != reEnterPassString){
                        Toast.makeText(context, "Create and Re-Enter Password must be same", Toast.LENGTH_SHORT).show()
                    }else {
//                        val intent = Intent(context, MainActivity::class.java)
//                         startActivity(intent)
                        val editor = sharedPreferences.edit()
                        editor.putString("ReEnterPassword", reEnterPassString)
                        Log.e(TAG, "onCreateView: $reEnterPassString")
                        editor.apply()

                        findNavController().navigate(R.id.loginFragment)
                    }
                }
        }

        return binding.root
    }



    fun initialization() {

        progressBarHelper =  ProgressBarHelper(requireActivity())


         mobileNum_et = binding.mobileNumEt
         mobileNum_Til = binding.mobileNumTil
         proceed_btn = binding.proceedBtn
         otp_et = binding.otpEt
         resend_btn = binding.resendBtn
         linear_otp = binding.linearOtp
         createPass_til = binding.createPassTil
         createPass_et = binding.createPassEt
         OTP_til = binding.otpTil
         next_btn = binding.nextBtn
         reEnter_pass_et = binding.reEnterPassEt
         reEnter_Til = binding.reEnterPassTil
         cancelTv = binding.cancelTv
         secure_accounTV = binding.secureAccountTv
        headName = binding.welcomeName

    }

    private fun observerOtpLiveData() {
        sendOtpViewModel.observeSendOtpData().observe(requireActivity()){ otpData ->

            try {
            Log.e(TAG, "observerOtpLiveData: $otpData")
//
            if (otpData.code == 2){
                numberCode = otpData.code
                findNavController().navigate(R.id.loginFragment)

            }else{



                proceed_btn.isVisible = false
                linear_otp.isVisible = true
                otp_et.isVisible = true
                resend_btn.isVisible = true

            }
            }catch(e : IOException){
                e.printStackTrace()
            }catch(e : java.lang.NullPointerException) {
                e.printStackTrace()
            }

        }
    }

    private fun sendOtp(){

        try {

        numString = binding.mobileNumEt.text!!.trim().toString()

        // get the phone number from edit text and append the country cde with it
        if (numString.isEmpty()) {
            Toast.makeText(context, "Enter Mobile Number", Toast.LENGTH_SHORT).show()
        }else if (numString.length != 10){
            mobileNum_et.error = "Enter valid mobile number"
        }else{
//            Log.e(TAG, "sendOtp: $number", )
            editNum = "+91$numString"
            Log.e(TAG, "sendOtp: $editNum")

//            proceed_btn.isVisible = false
//            linear_otp.isVisible = true
//            otp_et.isVisible = true
//            resend_btn.isVisible = true

            sendOtpViewModel.sendOtp(editNum, "mobile")
            val editor = sharedPreferences.edit()
            editor.putString("Number", editNum)
            editor.apply()
//            binding.progrssbar.isVisible = true
//            binding.loginBtn.isClickable = false
        }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    companion object{
        var otp  = "0000"
        lateinit var editNum : String
         var numberCode : Int? = null
    }
}