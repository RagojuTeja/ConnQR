package com.example.quickconnect.activities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.ViewModelProvider
import com.example.quickconnect.R
import com.example.quickconnect.databinding.ActivityEnterPasswordScreenBinding
import com.example.quickconnect.repository.LoginRepositary
import com.example.quickconnect.repository.SignUpRepositary
import com.example.quickconnect.utils.Alerts
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.LoginViewModel
import com.example.quickconnect.viewmodels.SignUpViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.sdsmdg.tastytoast.TastyToast
import java.io.IOException

class EnterPasswordScreen : AppCompatActivity() {

    private lateinit var binding: ActivityEnterPasswordScreenBinding

    lateinit var progressBarHelper: ProgressBarHelper
    lateinit var sharedPreferences : SharedPreferences

    lateinit var enterPass_et : TextInputEditText
    lateinit var enterPass_til : TextInputLayout
    lateinit var signUp_btn : AppCompatButton
    lateinit var mobileNumber : String
    lateinit var cancelTv : TextView
    lateinit var userNameTv : TextView


    private val loginViewModel: LoginViewModel by lazy {
        val loginViewModelFactory = LoginRepositary()
        ViewModelProvider(this, loginViewModelFactory )[LoginViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterPasswordScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        mobileNumber = sharedPreferences.getString("Number","").toString()



        initialization()
        observerLoginTokenLiveData()


    }

    private fun initialization() {
        progressBarHelper =  ProgressBarHelper(this)

        enterPass_et  = binding.enterPassEt
        enterPass_til = binding.enterPassTil
        signUp_btn    = binding.signUpBtn
        cancelTv = binding.cancelTv

        enterPass_et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD



        signUp_btn.setOnClickListener {
                userLogin()
        }

    }

    private fun userLogin() {

        try {

            if (enterPass_et.text!!.isEmpty()){
                TastyToast.makeText(this, "Enter Password!", TastyToast.LENGTH_LONG, TastyToast.CONFUSING)
//                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
            }else{
                loginViewModel.Login("mobile", "$mobileNumber","${enterPass_et.text}")
            }

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun observerLoginTokenLiveData(){

        loginViewModel.observeLoginData().observe(this) { tokenData ->

            try {
                if (tokenData.code == 0) {

                    val builder = AlertDialog.Builder(this)
                        .setTitle("Alert")
                        .setMessage("Please enter valid password")
                        .setPositiveButton("OK") { dialog, id ->
                            dialog.dismiss()
                        }
            // Create the AlertDialog
                    val dialog = builder.create()
            // Get the Window of the AlertDialog
                    val window: Window? = dialog.window
            // Set the gravity of the AlertDialog (e.g., Gravity.BOTTOM)
                    window?.setGravity(Gravity.DISPLAY_CLIP_HORIZONTAL)
           // Show the AlertDialog
                    dialog.show()

                }else{
                    val userToken = tokenData.data.token
                    Log.e(ContentValues.TAG, "observerUserTokenLiveData: $userToken", )

                    val editor = sharedPreferences.edit()
                    editor.putString("userToken", userToken)
                    editor.apply()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
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
}