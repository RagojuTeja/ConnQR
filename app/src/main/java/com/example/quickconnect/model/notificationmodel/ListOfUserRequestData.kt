package com.example.quickconnect.model.notificationmodel

import com.google.gson.annotations.SerializedName

data class ListOfUserRequestData (

    @SerializedName("status"      ) var status     : Boolean?        = null,
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("code"        ) var code       : Int?            = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<RequestUserData> = arrayListOf()

)

data class RequestUserData (

    @SerializedName("id"            ) var id           : Int?     = null,
    @SerializedName("username"      ) var username     : String?  = null,
    @SerializedName("full_name"     ) var fullName     : String?  = null,
    @SerializedName("email"         ) var email        : String?  = null,
    @SerializedName("mobile_number" ) var mobileNumber : String?  = null,
    @SerializedName("profile_pic"   ) var profilePic   : String?  = null,
    @SerializedName("is_active"     ) var isActive     : Boolean? = null

)