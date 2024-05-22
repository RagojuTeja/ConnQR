package com.example.quickconnect.fragments

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.quickconnect.R
import com.example.quickconnect.activities.MainActivity
import com.example.quickconnect.databinding.FragmentLoginBinding
import com.example.quickconnect.repository.LoginRepositary
import com.example.quickconnect.repository.SignUpRepositary
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.utils.generateQRCode
import com.example.quickconnect.viewmodels.LoginViewModel
import com.example.quickconnect.viewmodels.SignUpViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.IOException


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    lateinit var progressBarHelper: ProgressBarHelper


    lateinit var userName_et : TextInputEditText
    lateinit var userName_Til : TextInputLayout
    lateinit var enterPass_et : TextInputEditText
    lateinit var enterPass_til : TextInputLayout
    lateinit var signUp_btn : AppCompatButton
    lateinit var headName : TextView


    lateinit var sharedPreferences : SharedPreferences

    lateinit var createPassword : String
    lateinit var reEnterPassword : String
    lateinit var useerNameeString : String
    lateinit var mobileNumber : String

    lateinit var cancelTv : TextView
    lateinit var userNameTv : TextView

    private val signUpViewModel: SignUpViewModel by lazy {
        val twilioViewModelFactory = SignUpRepositary()
        ViewModelProvider(this, twilioViewModelFactory )[SignUpViewModel::class.java]
    }

    private val loginViewModel: LoginViewModel by lazy {
        val loginViewModelFactory = LoginRepositary()
        ViewModelProvider(this, loginViewModelFactory )[LoginViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentLoginBinding.inflate(layoutInflater)

        initialization()
        observerUserTokenLiveData()
        observerLoginTokenLiveData()

        sharedPreferences = requireContext().getSharedPreferences("UserData" , Context.MODE_PRIVATE)

        createPassword = sharedPreferences.getString("CreatePassword","").toString()
        reEnterPassword = sharedPreferences.getString("ReEnterPassword", "").toString()
        mobileNumber = sharedPreferences.getString("Number", "").toString()

        Log.e(TAG, "loginFragParams: $createPassword + $reEnterPassword + $mobileNumber" )


        Log.e(TAG, "onCreateView: ${NumberFragment.numberCode}")


        if (NumberFragment.numberCode == 2){
            userName_Til.isVisible = false
            enterPass_til.isVisible = true
            signUp_btn.text = "Login"
            userNameTv.text = "Enter Password"
        }




        signUp_btn.setOnClickListener {

            if (signUp_btn.text == "Sign Up"){
                createPassword()
            }else {
                Log.e(TAG, "login: ", )
                userLogin()
            }
        }

        return binding.root

    }



    private fun initialization() {

        progressBarHelper =  ProgressBarHelper(requireActivity())


        userName_et   = binding.userNameEt
        userName_Til  = binding.userNameTil
        enterPass_et  = binding.enterPassEt
        enterPass_til = binding.enterPassTil
        signUp_btn    = binding.signUpBtn
        cancelTv = binding.cancelTv
        userNameTv = binding.userNameEt
        headName = binding.welcomeName



        userName_Til.isVisible = true
        cancelTv.isVisible = true
        userNameTv.isVisible = true

    }

    private fun userLogin() {

        try {

            if (enterPass_et.text!!.isEmpty()){
                Toast.makeText(context, "Enter Password", Toast.LENGTH_SHORT).show()
            }else{
//                    useerNameeString = userName_et.text!!.trim().toString()

//                    Log.e(TAG, "useerNameeString: $useerNameeString", )

                loginViewModel.Login("mobile", "$mobileNumber","${enterPass_et.text}")
//                    Log.e(TAG, "createPassword: $createPassword +$useerNameeString + $createPassword + $reEnterPassword", )
            }

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun observerLoginTokenLiveData(){

        loginViewModel.observeLoginData().observe(requireActivity()
        ) { tokenData ->

            try {
                if (tokenData.status == true) {
                    val userToken = tokenData.data.token
                    Log.e(TAG, "observerUserTokenLiveData: $userToken", )

                    val editor = sharedPreferences.edit()
                    editor.putString("userToken", userToken)
                    editor.apply()

                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finishAffinity()

//                  startActivity(Intent(context, MainActivity::class.java))


                }

            }catch(e : IOException){
                e.printStackTrace()
            }catch(e : java.lang.NullPointerException) {
                e.printStackTrace()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

    }



    private fun createPassword(){


        try {


            // get the phone number from edit text and append the country cde with it
            if (userName_et.text!!.isEmpty()){
                Toast.makeText(context, "Enter User Name", Toast.LENGTH_SHORT).show()
            }else{
//            userName_Til.isVisible = false
//            enterPass_til.isVisible = true
//            signUp_btn.text = "Login"
                useerNameeString = userName_et.text!!.trim().toString()

                Log.e(TAG, "useerNameeString: $useerNameeString", )

                signUpViewModel.signUp("$mobileNumber", useerNameeString,createPassword,createPassword)
                Log.e(TAG, "createPassword: $createPassword +$useerNameeString + $createPassword + $reEnterPassword", )

            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun observerUserTokenLiveData() {

        signUpViewModel.observeSignUpData().observe(requireActivity()
        ) { tokenData ->

            try {
                if (tokenData.status == true) {
                    val signUpuserToken = tokenData.data.token
                    Log.e(TAG, "observerUserTokenLiveData: $signUpuserToken", )
                    val editor = sharedPreferences.edit()
                    editor.putString("signUpuserToken", signUpuserToken)
                    editor.apply()

                userName_Til.isVisible = false
                enterPass_til.isVisible = true
                signUp_btn.text = "Login"
                userNameTv.text = "Enter Password"
                    headName.setText("Enter Password")

                }

            }catch(e : IOException){
                e.printStackTrace()
            }catch(e : java.lang.NullPointerException) {
                e.printStackTrace()
            }
        }
    }






}
