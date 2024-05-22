package com.example.quickconnect.model.viewersmodel

import com.google.gson.annotations.SerializedName



data class UserViewersData (

    @SerializedName("status"      ) var status     : Boolean? = null,
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("code"        ) var code       : Int?     = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : UserView?    = UserView()

)

data class UserView (

    @SerializedName("id"                     ) var id                   : Int?                   = null,
    @SerializedName("username"               ) var username             : String?                = null,
    @SerializedName("full_name"              ) var fullName             : String?                = null,
    @SerializedName("email"                  ) var email                : String?                = null,
    @SerializedName("mobile_number"          ) var mobileNumber         : String?                = null,
    @SerializedName("profile_pic"            ) var profilePic           : String?                = null,
    @SerializedName("primary_phone_number"   ) var primaryPhoneNumber   : String?                = null,
    @SerializedName("secondary_phone_number" ) var secondaryPhoneNumber : String?                = null,
    @SerializedName("position"               ) var position             : String?                = null,
    @SerializedName("work_at"                ) var workAt               : String?                = null,
    @SerializedName("description"            ) var description          : String?                = null,
    @SerializedName("address"                ) var address              : String?                = null,
    @SerializedName("login_type"             ) var loginType            : String?                = null,
    @SerializedName("device_type"            ) var deviceType           : String?                = null,
    @SerializedName("is_active"              ) var isActive             : Boolean?               = null,
    @SerializedName("is_logged_in"           ) var isLoggedIn           : Boolean?               = null,
    @SerializedName("social_links"           ) var socialLinks          : ArrayList<SocialLinks> = arrayListOf(),
    @SerializedName("is_favorite"            ) var isFavorite           : Boolean?               = null


)

data class SocialLinks (

    @SerializedName("id"         ) var id        : Int?    = null,
    @SerializedName("created_at" ) var createdAt : String? = null,
    @SerializedName("updated_at" ) var updatedAt : String? = null,
    @SerializedName("icon"       ) var icon      : String? = null,
    @SerializedName("name"       ) var name      : String? = null,
    @SerializedName("url"        ) var url       : String? = null,
    @SerializedName("links"      ) var links     : Links?  = Links()

)

data class Links (

    @SerializedName("id"        ) var id       : Int?     = null,
    @SerializedName("is_locked" ) var isLocked : Boolean? = null,
    @SerializedName("link") var link : String? = null

)


