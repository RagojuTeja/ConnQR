package com.example.quickconnect.activities

import android.R
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.quickconnect.databinding.ActivitySignUpBinding
import com.example.quickconnect.utils.NoInternetUtils


class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    lateinit var sharedPreferences : SharedPreferences
    lateinit var userToken : String
    lateinit var signUpuserToken : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!NoInternetUtils.isInternetAvailable(this)) {
            NoInternetUtils.showNoInternetDialog(this)
        }

        sharedPreferences = getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        userToken = sharedPreferences.getString("userToken","").toString()
        signUpuserToken = sharedPreferences.getString("signUpuserToken","").toString()


        if (userToken.isNotEmpty()){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        if (signUpuserToken.isEmpty()){
//            findNavController().navi
        }


        val navHostFragment =
            supportFragmentManager.findFragmentById(androidx.navigation.fragment.R.id.nav_host_fragment_container) as NavHostFragment?
        val navController = navHostFragment?.navController


    }
}