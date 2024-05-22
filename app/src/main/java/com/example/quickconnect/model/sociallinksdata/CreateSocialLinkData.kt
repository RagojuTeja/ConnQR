package com.example.quickconnect.model.sociallinksdata

import com.google.gson.annotations.SerializedName

data class CreateSocialLinkData (

    @SerializedName("status"      ) var status     : Boolean? = null,
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("code"        ) var code       : Int?     = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()

)

data class Data (

    @SerializedName("id"                ) var id              : Int?              = null,
    @SerializedName("name"              ) var name            : String?           = null,
    @SerializedName("link"              ) var link            : String?           = null,
    @SerializedName("is_locked"         ) var isLocked        : Boolean?          = null,
    @SerializedName("user"              ) var user            : Int?              = null,
    @SerializedName("category"          ) var category        : Int?              = null,
    @SerializedName("users_with_access" ) var usersWithAccess : ArrayList<String> = arrayListOf()

)
