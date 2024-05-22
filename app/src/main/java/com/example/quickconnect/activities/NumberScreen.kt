package com.example.quickconnect.activities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.quickconnect.R
import com.example.quickconnect.databinding.ActivityNumberScreenBinding
import com.example.quickconnect.repository.SendOtpRepositary
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.SendOtpViewModel
import com.google.android.material.textfield.TextInputLayout
import com.sdsmdg.tastytoast.TastyToast
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import java.io.IOException


class NumberScreen : AppCompatActivity() {

    private lateinit var binding: ActivityNumberScreenBinding

    lateinit var progressBarHelper: ProgressBarHelper
    lateinit var sharedPreferences: SharedPreferences


    lateinit var mobileNum_et : EditText
    lateinit var mobileNum_Til : TextInputLayout
    lateinit var proceed_btn : AppCompatButton
    lateinit var otp_et : EditText
    lateinit var resend_btn : AppCompatButton
    lateinit var linear_otp : LinearLayout
    lateinit var userToken : String
    lateinit var numString: String
    var numberCode : Int? = null
    lateinit var editNum : String

    private val sendOtpViewModel: SendOtpViewModel by lazy {
        val sendOtpViewModelFactory = SendOtpRepositary()
        ViewModelProvider(this, sendOtpViewModelFactory)[SendOtpViewModel::class.java]
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNumberScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        userToken = sharedPreferences.getString("userToken","").toString()

        if (userToken.isNotEmpty()){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


        initialization()
        observerOtpLiveData()

    }

    private fun initialization() {
        progressBarHelper =  ProgressBarHelper(this)


        mobileNum_et = binding.mobileNumEt
        mobileNum_Til = binding.mobileNumTil
        proceed_btn = binding.proceedBtn
        otp_et = binding.otpEt
        resend_btn = binding.resendBtn
        linear_otp = binding.linearOtp

        binding.cancelTv.setOnClickListener {
            onBackPressed()
        }

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

                if (editable?.length != 4) {
//                    Toast.makeText(this@NumberScreen,"Enter 6 digit code",Toast.LENGTH_LONG).show()
                }else{
                    val intent = Intent(this@NumberScreen, SignUpScreen::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })
    }


    private fun sendOtp(){

        try {


            numString = binding.mobileNumEt.text!!.trim().toString()

            // get the phone number from edit text and append the country cde with it
            if (numString.isEmpty()) {
                TastyToast.makeText(this, "Enter Mobile Number !", TastyToast.LENGTH_LONG, TastyToast.CONFUSING)

//                Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_SHORT).show()
            }else if (numString.length != 10){
                mobileNum_et.error = "Enter valid mobile number"
            }else{
                editNum = "+91$numString"
                Log.e(ContentValues.TAG, "sendOtp: ${editNum}")

                sendOtpViewModel.sendOtp(editNum, "mobile")

                val editor = sharedPreferences.edit()
                editor.putString("Number", editNum)
                editor.apply()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun observerOtpLiveData() {
        sendOtpViewModel.observeSendOtpData().observe(this){ otpData ->

            try {
                Log.e(ContentValues.TAG, "observerOtpLiveData: $otpData")
//
                if (otpData.code == 2){
                    numberCode = otpData.code
                    val intent = Intent(this, EnterPasswordScreen::class.java)
                    startActivity(intent)
                    finish()
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
}