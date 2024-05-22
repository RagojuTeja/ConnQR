package com.example.quickconnect.activities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.quickconnect.R
import com.example.quickconnect.databinding.ActivitySignUpScreenBinding
import com.example.quickconnect.repository.SignUpRepositary
import com.example.quickconnect.utils.Alerts
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.SignUpViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.sdsmdg.tastytoast.TastyToast
import java.io.IOException

class SignUpScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpScreenBinding

    lateinit var progressBarHelper: ProgressBarHelper
    lateinit var sharedPreferences: SharedPreferences

    lateinit var createPass_et : TextInputEditText
    lateinit var reEnter_pass_et : TextInputEditText
    lateinit var userName_et : TextInputEditText
    lateinit var userName_Til : TextInputLayout
    lateinit var signUp_btn : AppCompatButton
    lateinit var cancelTv : TextView

//    lateinit var createPassword : String
//    lateinit var reEnterPassword : String
    lateinit var mobileNumber : String


    lateinit var userNameString: String
    lateinit var createPassString : String
    lateinit var reEnterPassString : String

    lateinit var userToken : String


    private val signUpViewModel: SignUpViewModel by lazy {
        val twilioViewModelFactory = SignUpRepositary()
        ViewModelProvider(this, twilioViewModelFactory )[SignUpViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        mobileNumber = sharedPreferences.getString("Number","").toString()

        initialization()
        observerUserTokenLiveData()

    }

    private fun initialization() {
        progressBarHelper =  ProgressBarHelper(this)

        userName_et   = binding.userNameEt
        createPass_et = binding.createPassEt
        reEnter_pass_et = binding.reEnterPassEt
        userName_Til  = binding.userNameTil
        signUp_btn    = binding.signUpBtn
        cancelTv = binding.cancelTv


        // Set inputType to textPassword for password fields
        createPass_et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        reEnter_pass_et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD


        binding.cancelTv.setOnClickListener {
            onBackPressed()
        }

        binding.signUpBtn.setOnClickListener {
            signUp()
        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun signUp() {

        userNameString = userName_et.text!!.trim().toString()
        createPassString = createPass_et.text!!.trim().toString()
        reEnterPassString = reEnter_pass_et.text!!.trim().toString()

        if (userNameString.isNullOrEmpty()){
            userName_et.error = "Enter Username"
//            Toast.makeText(this, "Enter Username",Toast.LENGTH_LONG).show()
        }else if (createPassString.isEmpty()) {
            createPass_et.error = "Enter Password"
//            Toast.makeText(this, "Enter Password",Toast.LENGTH_LONG).show()
        }else if ( reEnterPassString.isEmpty()){
            reEnter_pass_et.error = "Re-Enter Password"
//            Toast.makeText(this, "Re-Enter Password",Toast.LENGTH_LONG).show()
        }else if (createPassString != reEnterPassString){
            val toast = TastyToast.makeText(this, "Create and Re-Enter Password must be same", TastyToast.LENGTH_LONG, TastyToast.CONFUSING)
                toast.setGravity(Gravity.TOP, 0, 0) // Set the gravity of the toast
                toast.show()
//            Toast.makeText(this, "Enter and Re-Enter Password must be same",Toast.LENGTH_LONG).show()
        }else{
            createPassword()
        }

    }

    private fun createPassword(){

        try {
                signUpViewModel.signUp("$mobileNumber", userNameString,createPassString,reEnterPassString)
                Log.e(ContentValues.TAG, "createPassword: $mobileNumber +$userNameString + $createPassString + $reEnterPassString", )

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun observerUserTokenLiveData() {

        signUpViewModel.observeSignUpData().observe(this) { tokenData ->

            try {
                if (tokenData.code == 2) {
                    val builder = AlertDialog.Builder(this)
                        .setTitle("Alert")
                        .setMessage("Username already exstited in the system. add new Username!")
                        .setPositiveButton("OK") { dialog, id ->
                            dialog.dismiss()
                        }
//                    builder.setNegativeButton("Not now") { dialog, id -> }

                    val dialog = builder.create()
                    dialog.show()
                }else{
                    val signUpuserToken = tokenData.data.token
                    Log.e(ContentValues.TAG, "observerUserTokenLiveData: $signUpuserToken", )
                    val editor = sharedPreferences.edit()
                    editor.putString("userToken", signUpuserToken)
                    editor.apply()

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

            }catch(e : IOException){
                e.printStackTrace()
            }catch(e : java.lang.NullPointerException) {
                e.printStackTrace()
            }
        }
    }
}