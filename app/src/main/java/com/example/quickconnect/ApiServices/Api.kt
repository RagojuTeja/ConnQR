package com.example.quickconnect.ApiServices

import com.example.quickconnect.model.CallToActionModel.ActionsDeleteData
import com.example.quickconnect.model.CallToActionModel.ActionsUpdateData
import com.example.quickconnect.model.CallToActionModel.CallListTypeCategorieData
import com.example.quickconnect.model.CallToActionModel.CallToActionListData
import com.example.quickconnect.model.CallToActionModel.CountActionData
import com.example.quickconnect.model.CallToActionModel.CreateActionData
import com.example.quickconnect.model.favouritesmodel.AddFavouriteData
import com.example.quickconnect.model.favouritesmodel.FavouriteListData
import com.example.quickconnect.model.favouritesmodel.RemoveFavouriteData
import com.example.quickconnect.model.favouritesmodel.ViewersData
import com.example.quickconnect.model.notificationmodel.ApprovalRequestData
import com.example.quickconnect.model.notificationmodel.ListOfSocialRequestsData
import com.example.quickconnect.model.notificationmodel.ListOfUserRequestData
import com.example.quickconnect.model.notificationmodel.MyRequestDataList
import com.example.quickconnect.model.notificationmodel.MyRequestUserListData
import com.example.quickconnect.model.notificationmodel.MyRequestedListData
import com.example.quickconnect.model.notificationmodel.OtherRequestUserData
import com.example.quickconnect.model.notificationmodel.OtherRequestedListData
import com.example.quickconnect.model.notificationmodel.SendRequestData
import com.example.quickconnect.model.usermodel.QrData
import com.example.quickconnect.model.usermodel.SendOtpData
import com.example.quickconnect.model.usermodel.SignUpData
import com.example.quickconnect.model.sociallinksdata.CreateSocialLinkData
import com.example.quickconnect.model.sociallinksdata.SearchData
import com.example.quickconnect.model.sociallinksdata.SocialLinkDeleteData
import com.example.quickconnect.model.sociallinksdata.SocialLinkListData
import com.example.quickconnect.model.sociallinksdata.SocialLinksUpdateData
import com.example.quickconnect.model.usermodel.ChangePasswordData
import com.example.quickconnect.model.usermodel.EditData
import com.example.quickconnect.model.usermodel.EditProfileData
import com.example.quickconnect.model.usermodel.LogoutData
import com.example.quickconnect.model.usermodel.UserDeleteData
import com.example.quickconnect.model.viewersmodel.UserViewersData
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {

    //Sendotp
    @FormUrlEncoded
    @POST ("/users/send_otp/")
    fun sendOTP (@Field ("mobile") mobile : String,
                 @Field("login_type") login_type : String) : Call<SendOtpData>

    //Signup
    @FormUrlEncoded
    @POST("/users/create_password/")
    fun SignUp(
        @Field("mobile") mobile: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("confirm_password") confirm_password: String
    ): Call<SignUpData>

    @FormUrlEncoded
    @POST("/users/change_password/")
    fun changePassword(@Header ("Authorization") Authorization : String,
        @Field("old_password") mobile: String,
        @Field("new_password") username: String,
        @Field("confirm_password") password: String,
    ): Call<ChangePasswordData>


    //Login
    @FormUrlEncoded
    @POST ("/users/login/")
    fun Login(@Field("login_type") login_type: String,
              @Field("mobile") mobile: String,
              @Field("password") password: String
    ) : Call<SignUpData>


    //GET PROFILE
    @GET ("/users/profile_me/")
    fun getProfile(@Header ("Authorization") Authorization : String
    ) :Call<QrData>

    //Edit Profile
    @FormUrlEncoded
    @PATCH ("/users/edit_profile/")
    fun editProfile(@Header ("Authorization") Authorization : String,
                    @Field ("full_name") full_name : String,
                    @Field ("email") email : String,
                    @Field ("primary_phone_number") primary_phone_number : String,
                    @Field ("secondary_phone_number") secondary_phone_number : String,
                    @Field ("work_at") work_at : String,
                    @Field ("description") description : String
//                    @Field ("profile_pic") profile_pic : String
    ) : Call<EditProfileData>

    @FormUrlEncoded
    @PATCH ("/users/edit_profile/")
    fun photoUpadate(@Header ("Authorization") Authorization : String,
                     @Field ("profile_pic") profile_pic : String
    ) : Call<EditProfileData>


    //LAGOUT
    @GET ("/users/logout/")
    fun logout (@Header ("Authorization") Authorization : String
    ) : Call<LogoutData>

    //Delete Account
    @DELETE ("/users/delete/")
    fun userDelete (@Header ("Authorization") Authorization : String
    ) : Call<UserDeleteData>

    //Viewers Data
    @FormUrlEncoded
    @POST ("/users/user_access_view_links/")
    fun userViewLinks(@Header ("Authorization") Authorization : String,
                      @Field ("username") username : String
    ) : Call<UserViewersData>

    @FormUrlEncoded
    @POST ("/social_media/search_category/")
    fun search(@Header ("Authorization") Authorization : String,
                      @Field ("category_name") category_name : String
    ) : Call<SearchData>

    @GET ("/core/action_count/")
    fun countActions(@Header ("Authorization") Authorization : String,
    ) : Call<CountActionData>





    //GET Categorie Type List
    @GET ("/social_media/category/")
    fun getActionTypeList() : Call<CallListTypeCategorieData>

    //ActionCreate
    @FormUrlEncoded
    @POST ("/actions/create/")
    fun createAction(@Header ("Authorization") Authorization : String,
                     @Field ("name") name : String,
                     @Field ("link") link : String,
                     @Field ("category_id") category_id : Int
    ) : Call<CreateActionData>

    //GET ACTION LIST
    @GET ("/actions/list/")
    fun getActionList(@Header ("Authorization") Authorization: String
    ) : Call<CallToActionListData>

    //Actions Update
    @PATCH ("/actions/update/")
    fun actionsUpdate(@Header ("Authorization") Authorization: String,
                      @Field ("action_id") action_id : Int,
                      @Field ("name") name : String,
                      @Field ("category") category : Int
    ) : Call<ActionsUpdateData>


    //Actions Delete
    @FormUrlEncoded
    @POST ("/actions/delete/")
    fun actionsDelete(@Header ("Authorization") Authorization: String,
                      @Field ("id") id : Int
    ) : Call<ActionsDeleteData>



    //All Social Links
    @GET ("/social_media/social_links/")
    fun allSocialList(@Header ("Authorization") Authorization: String) : Call<SocialLinkListData>


    //Create Social Link List
    @FormUrlEncoded
    @POST ("/social_media/social_links/create/")
    fun createSocialLink(@Header ("Authorization") Authorization: String,
                         @Field ("name") name : String,
                         @Field ("category") category : Int,
                         @Field ("link") link : String,
                         @Field ("is_locked") is_locked : Int
    ) : Call<CreateSocialLinkData>

    //Social Link lIST
    @GET ("/social_media/social_links/list/")
    fun socialList(@Header ("Authorization") Authorization: String
    ) : Call<SocialLinkListData>


    //Social Links Upadate
    @FormUrlEncoded
    @PATCH ("/social_media/social_links/update/")
    fun updateLinks(@Header ("Authorization") Authorization: String,
                    @Field ("category_id") category_id : Int,
                    @Field ("link") link : String,
                    @Field ("is_locked") is_locked : Int
    ) : Call<SocialLinksUpdateData>


    //Social Links Upadate
    @FormUrlEncoded
    @PATCH ("/social_media/social_links/update/")
    fun switchUpdate(@Header ("Authorization") Authorization: String,
                     @Field ("category_id") category_id : Int,
                    @Field ("is_locked") is_locked : Int
    ) : Call<SocialLinksUpdateData>


    //Social link Delete
    //# able to pass inside links:{id} from
    // list api in line 22
    @FormUrlEncoded
    @POST ("/social_media/social_links/delete/")
    fun deleteSocialUrl (@Header ("Authorization") Authorization: String,
                         @Field ("id") id : Int
    ) : Call<SocialLinkDeleteData>


    //Favorites APIS

    //ADD FAVOURITES
    @FormUrlEncoded
    @POST ("/users/add_favorite/")
    fun addFavorites (@Header ("Authorization") Authorization: String,
                      @Field ("username") username : String
    ) : Call<AddFavouriteData>


    //Get Favourite List
    @GET ("/users/favorite_list/")
    fun getFavouriteList (@Header ("Authorization") Authorization: String
    ) : Call<FavouriteListData>


    //Remove Favourite Data
    @FormUrlEncoded
    @POST("/users/remove_favorite/")
    fun removeFavourite(
        @Header("Authorization") authorization: String,
        @Field("user_id") userId: Int
    ): Call<RemoveFavouriteData>

//viewers list
    @GET ("/users/viewers/")
    fun getViewersList (@Header ("Authorization") Authorization: String
    ) : Call<ViewersData>


    //send request
    @FormUrlEncoded
    @POST ("/social_media/request/send_request/")
    fun sendRequest(@Header ("Authorization") Authorization : String,
                    @Field ("social_link_id") social_link_id : Int
    ) : Call<SendRequestData>


    //Request Approval
    @FormUrlEncoded
    @POST ("/social_media/request/grant_or_decline/")
    fun approvalRequest(@Header ("Authorization") Authorization : String,
                        @Field ("request_id") request_id : Int,
                        @Field ("action") action : String
    ) : Call<ApprovalRequestData>

    //List of user request
    @GET ("/social_media/request/list_of_user_request/")
    fun listOfUserRequest(@Header ("Authorization") Authorization : String
    ) : Call<ListOfUserRequestData>


    //List of Request and Accepted from
    //selected user
    @FormUrlEncoded
    @POST ("/social_media/request/list_of_request/")
    fun listOfSocialRequest(@Header ("Authorization") Authorization : String,
                          @Field ("username") username : String
    ) : Call<ListOfSocialRequestsData>


    //List of user request
    @GET ("/social_media/request/my_requested_user_list/")
    fun myRequestUserList(@Header ("Authorization") Authorization : String
    ) : Call<MyRequestUserListData>


    //List of user request
    @FormUrlEncoded
    @POST ("/social_media/request/my_requested_list/")
    fun mySocialRequestList(@Header ("Authorization") Authorization : String,
                      @Field ("username") username : String
    ) : Call<ListOfSocialRequestsData>

    //List of user request
    @GET ("/social_media/request/other_requesting_user_list/")
    fun otherRequestUserList(@Header ("Authorization") Authorization : String,
    ) : Call<OtherRequestUserData>

    @FormUrlEncoded
    @POST ("/social_media/request/other_requesting_list/")
    fun otherRequestSocialList(@Header ("Authorization") Authorization : String,
                      @Field ("username") username : String
    ) : Call<ListOfSocialRequestsData>

}