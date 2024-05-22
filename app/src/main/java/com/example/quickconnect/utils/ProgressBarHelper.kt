package com.example.quickconnect.utils

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import androidx.core.content.ContextCompat
import com.example.quickconnect.R

class ProgressBarHelper(private val activity: Activity) {
    private var progressDialog: Dialog? = null

    fun showProgressDialog() {
        progressDialog = Dialog(activity)
        progressDialog?.let {
            it.setContentView(R.layout.custom_progress_bar)
            it.setCancelable(true)
            it.window?.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            it.window?.setBackgroundDrawable(
                ContextCompat.getDrawable(activity, R.color.ash)
            )
            it.window?.setGravity(Gravity.CENTER)
            it.show()
        }
    }

    fun hideProgressDialog() {
        progressDialog?.dismiss()
    }
}