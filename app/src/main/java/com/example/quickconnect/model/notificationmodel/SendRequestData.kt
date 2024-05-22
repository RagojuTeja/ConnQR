package com.example.quickconnect.model.notificationmodel

import com.google.gson.annotations.SerializedName


data class SendRequestData (

    @SerializedName("status"      ) var status     : Boolean? = null,
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("code"        ) var code       : Int?     = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()

)


data class Data (

    @SerializedName("id"          ) var id         : Int?     = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("is_accepted" ) var isAccepted : Boolean? = null,
    @SerializedName("requester"   ) var requester  : Int?     = null,
    @SerializedName("social_link" ) var socialLink : Int?     = null

)
