package com.example.quickconnect.model.favouritesmodel

import com.google.gson.annotations.SerializedName


data class AddFavouriteData (

    @SerializedName("status"      ) var status     : Boolean? = null,
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("code"        ) var code       : Int?     = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()

)

data class Data (

    @SerializedName("username" ) var username : String? = null

)
