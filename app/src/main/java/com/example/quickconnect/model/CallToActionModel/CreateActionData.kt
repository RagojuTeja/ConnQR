package com.example.quickconnect.model.CallToActionModel

import com.google.gson.annotations.SerializedName


data class CreateActionData (

    @SerializedName("status"      ) var status     : Boolean? = null,
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("code"        ) var code       : Int?     = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()

)


data class Data (

    @SerializedName("id"         ) var id        : Int?      = null,
    @SerializedName("category"   ) var category  : CreateCategory? = CreateCategory(),
    @SerializedName("name"       ) var name      : String?   = null,
    @SerializedName("link"       ) var link      : String?   = null,
    @SerializedName("created_at" ) var createdAt : String?   = null,
    @SerializedName("updated_at" ) var updatedAt : String?   = null,
    @SerializedName("user"       ) var user      : Int?      = null

)

data class CreateCategory (

    @SerializedName("id"         ) var id        : Int?    = null,
    @SerializedName("created_at" ) var createdAt : String? = null,
    @SerializedName("updated_at" ) var updatedAt : String? = null,
    @SerializedName("icon"       ) var icon      : String? = null,
    @SerializedName("name"       ) var name      : String? = null,
    @SerializedName("url"        ) var url       : String? = null

)