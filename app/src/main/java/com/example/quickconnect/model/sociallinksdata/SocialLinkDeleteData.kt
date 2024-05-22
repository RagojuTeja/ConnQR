package com.example.quickconnect.model.sociallinksdata

import com.google.gson.annotations.SerializedName

data class SocialLinkDeleteData (

    @SerializedName("status"      ) var status     : Boolean? = null,
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("code"        ) var code       : Int?     = null,
    @SerializedName("message"     ) var message    : String?  = null

)