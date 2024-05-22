package com.example.quickconnect.model.usermodel

import com.google.gson.annotations.SerializedName

data class SignUpData (

    @SerializedName("status"      ) var status     : Boolean? = null,
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("code"        ) var code       : Int?     = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : TokenData

)

data class TokenData (

    @SerializedName("token") var token : String? = null


)