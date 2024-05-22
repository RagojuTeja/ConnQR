package com.example.quickconnect.model.usermodel

import com.google.gson.annotations.SerializedName

data class SendOtpData (

    @SerializedName("status"      ) var status     : Boolean? = null,
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("code"        ) var code       : Int?     = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data

)
data class Data (
    @SerializedName("mobile") var mobile : String? = null
)