package com.example.quickconnect.model.CallToActionModel

import com.google.gson.annotations.SerializedName

data class ActionsDeleteData (

    @SerializedName("status"      ) var status     : Boolean? = null,
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("code"        ) var code       : Int?     = null,
    @SerializedName("message"     ) var message    : String?  = null

)
