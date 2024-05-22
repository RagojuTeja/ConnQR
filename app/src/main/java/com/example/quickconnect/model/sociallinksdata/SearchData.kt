package com.example.quickconnect.model.sociallinksdata

import com.google.gson.annotations.SerializedName

data class SearchData (

    @SerializedName("status"      ) var status     : Boolean?        = null,
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("code"        ) var code       : Int?            = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<DataSearch> = arrayListOf()

)


data class DataSearch (

    @SerializedName("id"         ) var id        : Int?    = null,
    @SerializedName("created_at" ) var createdAt : String? = null,
    @SerializedName("updated_at" ) var updatedAt : String? = null,
    @SerializedName("icon"       ) var icon      : String? = null,
    @SerializedName("name"       ) var name      : String? = null,
    @SerializedName("url"        ) var url       : String? = null,
    @SerializedName("links"      ) var links     : SearchLinks?  = SearchLinks()

)

data class SearchLinks (

    @SerializedName("id"                ) var id              : Int?              = null,
    @SerializedName("name"              ) var name            : String?           = null,
    @SerializedName("link"              ) var link            : String?           = null,
    @SerializedName("is_locked"         ) var isLocked        : Boolean?          = null,
    @SerializedName("user"              ) var user            : Int?              = null,
    @SerializedName("category"          ) var category        : Int?              = null,
    @SerializedName("users_with_access" ) var usersWithAccess : ArrayList<String> = arrayListOf()

)