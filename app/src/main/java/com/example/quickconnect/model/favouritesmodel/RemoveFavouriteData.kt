package com.example.quickconnect.model.favouritesmodel

import com.google.gson.annotations.SerializedName

data class RemoveFavouriteData (

    @SerializedName("status"      ) var status     : Boolean?        = null,
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("code"        ) var code       : Int?            = null,
    @SerializedName("message"     ) var message    : String?         = null,

)
