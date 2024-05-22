package com.example.quickconnect.model.CallToActionModel

import com.google.gson.annotations.SerializedName

data class CountActionData (

    @SerializedName("status"      ) var status     : Boolean?        = null,
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("code"        ) var code       : Int?            = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : MutableList<CountData>

)

data class CountData (

    @SerializedName("id"          ) var id         : Int?     = null,
    @SerializedName("name"        ) var name       : String?  = null,
    @SerializedName("count"       ) var count      : Int?     = null,
    @SerializedName("is_required" ) var isRequired : Boolean? = null,
    @SerializedName("created_at"  ) var createdAt  : String?  = null,
    @SerializedName("updated_at"  ) var updatedAt  : String?  = null

)
