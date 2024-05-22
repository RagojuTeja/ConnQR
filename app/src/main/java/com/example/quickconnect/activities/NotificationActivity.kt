package com.example.quickconnect.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TableLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.example.quickconnect.R
import com.example.quickconnect.adapters.ViewPagerAdapter
import com.example.quickconnect.databinding.ActivityNotificationBinding
import com.example.quickconnect.fragments.ExpandableFragment
import com.example.quickconnect.fragments.ExpandbleMyRequestFragment
import com.example.quickconnect.fragments.MyRequestFragment
import com.example.quickconnect.fragments.OtherRequestFragment
import com.example.quickconnect.utils.ProgressBarHelper
import com.google.android.material.tabs.TabLayout

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityNotificationBinding
    lateinit var progressBarHelper: ProgressBarHelper
    lateinit var backTv : TextView
    lateinit var viewPager : ViewPager
    lateinit var tabLayout : TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Instantiation()
        setOnListeners()

        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ExpandbleMyRequestFragment(), "My Request")
        adapter.addFragment(ExpandableFragment(), "Other Request")

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)


    }

    fun Instantiation(){

        progressBarHelper =  ProgressBarHelper(this)


        viewPager = binding.viewPager
        tabLayout = binding.tabLayout
        backTv = binding.onBackTv

    }

    fun setOnListeners(){

        backTv.setOnClickListener {
            onBackPressed()
        }

    }
}