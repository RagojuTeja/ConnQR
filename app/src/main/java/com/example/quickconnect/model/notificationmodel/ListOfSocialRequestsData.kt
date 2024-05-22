package com.example.quickconnect.model.notificationmodel

import com.google.gson.annotations.SerializedName

data class ListOfSocialRequestsData (

    @SerializedName("status"      ) var status     : Boolean?        = null,
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("code"        ) var code       : Int?            = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<SocialRequestsData> = arrayListOf()

)

data class SocialRequestsData (

    @SerializedName("id"          ) var id         : Int?        = null,
    @SerializedName("message"     ) var message    : String?     = null,
    @SerializedName("is_accepted" ) var isAccepted : Boolean?    = null,
    @SerializedName("social_link" ) var socialLink : SocialLink? = SocialLink()

)

data class SocialLink (

    @SerializedName("id"        ) var id       : Int?      = null,
    @SerializedName("name"      ) var name     : String?   = null,
    @SerializedName("is_locked" ) var isLocked : Boolean?  = null,
    @SerializedName("user"      ) var user     : User?     = User(),
    @SerializedName("category"  ) var category : Category? = Category()

)


data class User (

    @SerializedName("id"                     ) var id                   : Int?     = null,
    @SerializedName("username"               ) var username             : String?  = null,
    @SerializedName("full_name"              ) var fullName             : String?  = null,
    @SerializedName("email"                  ) var email                : String?  = null,
    @SerializedName("mobile_number"          ) var mobileNumber         : String?  = null,
    @SerializedName("profile_pic"            ) var profilePic           : String?  = null,
    @SerializedName("primary_phone_number"   ) var primaryPhoneNumber   : String?  = null,
    @SerializedName("secondary_phone_number" ) var secondaryPhoneNumber : String?  = null,
    @SerializedName("position"               ) var position             : String?  = null,
    @SerializedName("work_at"                ) var workAt               : String?  = null,
    @SerializedName("description"            ) var description          : String?  = null,
    @SerializedName("address"                ) var address              : String?  = null,
    @SerializedName("login_type"             ) var loginType            : String?  = null,
    @SerializedName("device_type"            ) var deviceType           : String?  = null,
    @SerializedName("is_active"              ) var isActive             : Boolean? = null,
    @SerializedName("is_logged_in"           ) var isLoggedIn           : Boolean? = null

)

data class Category (

    @SerializedName("id"         ) var id        : Int?    = null,
    @SerializedName("created_at" ) var createdAt : String? = null,
    @SerializedName("updated_at" ) var updatedAt : String? = null,
    @SerializedName("icon"       ) var icon      : String? = null,
    @SerializedName("name"       ) var name      : String? = null,
    @SerializedName("url"        ) var url       : String? = null

)