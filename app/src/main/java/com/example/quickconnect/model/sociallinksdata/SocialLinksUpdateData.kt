package com.example.quickconnect.model.sociallinksdata

import com.google.gson.annotations.SerializedName

data class SocialLinksUpdateData (

    @SerializedName("status"      ) var status     : Boolean? = null,
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("code"        ) var code       : Int?     = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : UpdateData?    = UpdateData()

)

data class UpdateData (

    @SerializedName("id"        ) var id       : Int?     = null,
    @SerializedName("is_locked" ) var isLocked : Boolean? = null,
    @SerializedName("link"      ) var link     : String?  = null

)
