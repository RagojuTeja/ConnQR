package com.example.quickconnect.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.R
import com.example.quickconnect.activities.NumberScreen
import com.example.quickconnect.activities.SignUpActivity
import com.example.quickconnect.activities.UserViewActivity
import com.example.quickconnect.adapters.FavListAdapter
import com.example.quickconnect.adapters.SocialAccountAdapter
import com.example.quickconnect.adapters.ViewersListAdapter
import com.example.quickconnect.databinding.FragmentBookmarkBottomSheetBinding
import com.example.quickconnect.model.favouritesmodel.FavouriteData
import com.example.quickconnect.model.favouritesmodel.ViewersList
import com.example.quickconnect.model.sociallinksdata.CategoryData
import com.example.quickconnect.repository.FavouriteViewRepositary
import com.example.quickconnect.repository.LoginRepositary
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.FavouriteViewModel
import com.example.quickconnect.viewmodels.LoginViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sdsmdg.tastytoast.TastyToast
import com.squareup.picasso.Picasso
import dev.shreyaspatil.MaterialDialog.MaterialDialog


class BookmarkBottomSheetFragment : BottomSheetDialogFragment() , FavListAdapter.OnItemClickListener,ViewersListAdapter.OnItemClickListener {

    private lateinit var binding: FragmentBookmarkBottomSheetBinding
    lateinit var progressBarHelper: ProgressBarHelper
    lateinit var bottomSheetDialog : BottomSheetDialog


    lateinit var favouriteRv : RecyclerView
    lateinit var viewersRv : RecyclerView
    lateinit var changePassBtn : AppCompatButton
    lateinit var deleteAccPermBtn : AppCompatButton
    lateinit var signOutTv : TextView
    lateinit var changePasswordBtn : AppCompatButton

    lateinit var sharedPreferences : SharedPreferences
    lateinit var userToken : String

    lateinit var favouriteList: MutableList<FavouriteData>
    lateinit var viewList: MutableList<ViewersList>
    lateinit var favListAdapter: FavListAdapter
    lateinit var viewersListAdapter: ViewersListAdapter
    private var originalList: MutableList<FavouriteData> = mutableListOf()
    private var originalListViewer: MutableList<ViewersList> = mutableListOf()


    lateinit var getFullName : String
    lateinit var vagetWorkAt : String
    lateinit var getDesc : String
    lateinit var getProfile_pic : String



    private val addFavouriteViewModel: FavouriteViewModel by lazy {
        val addFavouriteViewModelFactory = FavouriteViewRepositary()
        ViewModelProvider(this, addFavouriteViewModelFactory)[FavouriteViewModel::class.java]
    }

    private val loginViewModel: LoginViewModel by lazy {
        val loginViewModelFactory = LoginRepositary()
        ViewModelProvider(this, loginViewModelFactory )[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentBookmarkBottomSheetBinding.inflate(layoutInflater)

        sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        userToken = sharedPreferences.getString("userToken", "").toString()
         getFullName = sharedPreferences.getString("fullName", "").toString()
         vagetWorkAt = sharedPreferences.getString("workAt", "").toString()
         getDesc = sharedPreferences.getString("desc", "").toString()
         getProfile_pic = sharedPreferences.getString("profile", "").toString()

        Initialization()
        callFavouriteList()
        callViewersList()
        observeList()
        observeViewersList()
        signOut(userToken)
        observerLogOutData()
        changePasswordDialog()
        observerChangePassData()
        observerUserDelete()
        observerFavRemoveData()
        searchView()



        favListAdapter = FavListAdapter(originalList, requireContext())
        favouriteRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        favouriteRv.adapter = favListAdapter


        viewersListAdapter = ViewersListAdapter(originalListViewer, requireContext())
        viewersRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        viewersRv.adapter = viewersListAdapter


        return binding.root
    }

    fun searchView(){

        val searchView = binding.searchView

        searchView.setOnClickListener {
            searchView.isIconified = false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
//                favListAdapter.filter(newText.orEmpty())
//                favListAdapter.notifyDataSetChanged()
//                filterLists(newText.orEmpty())
                favListAdapter.filter(newText.orEmpty())
                viewersListAdapter.filter(newText.orEmpty())
                viewersListAdapter.notifyDataSetChanged()
                viewersListAdapter.notifyDataSetChanged()
                return true
            }
        })
    }


    private fun signOut(token : String) {
        try {

        signOutTv.setOnClickListener {

            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Are you sure you want to logout?")
                .setTitle("")
                .setPositiveButton("YES") { dialog, id ->


                        logoutFromMobile(token)


                }
            builder.setNegativeButton("NO") { dialog, id -> }

            val dialog = builder.create()
            dialog.show()
        }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun logoutFromMobile(userToken : String){
        loginViewModel.logout(userToken)
    }



    private fun observerLogOutData(){
        loginViewModel.observeLogoutData().observe(this){ logOutData ->

            try {

            if(logOutData.status == true){
                sharedPreferences.edit().apply {
                    putString("UserData", "")
                    remove("userToken")
                    remove("fullName")
                    remove("workAt")
                    remove("desc")
                    remove("profile")
                    apply()
                }
                updateUIAfterLogout()

                val intent = Intent(context, NumberScreen::class.java)
                startActivity(intent)
                requireActivity().finishAffinity()
                val toast = TastyToast.makeText(context, "Logged out successfully", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)
                toast.setGravity(Gravity.FILL_VERTICAL,0,0)
                toast.show()
            }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun updateUIAfterLogout() {
        // Clear or update UI components as needed after logging out
        getFullName = ""
        vagetWorkAt = ""
        getDesc = ""
        // Clear or set default image for profile_pic
        // ...

        // Clear or update any other UI components
        // ...
    }

    private fun changePasswordDialog() {
        changePasswordBtn.setOnClickListener {
            bottomSheetDialog = BottomSheetDialog(requireActivity())
            val view = layoutInflater.inflate(R.layout.change_password_bottom_sheet, null)
            bottomSheetDialog.setContentView(view)

            changePassword(bottomSheetDialog)

            val terminate = bottomSheetDialog.findViewById<TextView>(R.id.terminate_cp)

            terminate!!.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }



    }


    private fun changePassword(bottomSheetDialog: BottomSheetDialog){
        // Access the ids from the layout
        val oldPassEt = bottomSheetDialog.findViewById<EditText>(R.id.old_pass_et)
        val newPassEt = bottomSheetDialog.findViewById<EditText>(R.id.new_pass_et)
        val confirmPassEt = bottomSheetDialog.findViewById<EditText>(R.id.confirm_pass_et)
        val doneTv = bottomSheetDialog.findViewById<TextView>(R.id.done_tv)



        doneTv?.setOnClickListener {

            try {

            val oldPass = oldPassEt?.text?.trim().toString()
            val newPass = newPassEt?.text?.trim().toString()
            val confirmPass = confirmPassEt?.text?.trim().toString()
            Log.e("TAG", "done: $", )
            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                val toast = TastyToast.makeText(context, "Fill all the Fields", TastyToast.LENGTH_LONG, TastyToast.ERROR)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()

//                Toast.makeText(context, "Fill all the Fields", Toast.LENGTH_LONG).show()
            }else{
                loginViewModel.changePassword(userToken, oldPass, newPass, confirmPass)
                bottomSheetDialog.dismiss()
            }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun observerChangePassData(){
        loginViewModel.observeChangePassData().observe(this){ChangePassData ->

            if(ChangePassData.message == "User entered incorrect password!!!"){
                Toast.makeText(context, "Old Password is incorrect please try again!", Toast.LENGTH_LONG).show()

            }else if (ChangePassData.message == "User entered new password and confirm password should be same!!!"){
                Toast.makeText(context, "New and Confirm password should be same try again!", Toast.LENGTH_LONG).show()
            }else {
                TastyToast.makeText(context, "Changed Password Successfully", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)

//                Toast.makeText(context, "Changed Password Successfully", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun userDeletePerm(){

        val mDialog = MaterialDialog.Builder(requireActivity())
            .setTitle("Delete?")
            .setMessage("Are you sure want to delete Account")
            .setCancelable(false)
            .setPositiveButton(
                "Delete", com.example.quickconnect.R.drawable.delete_icon
            ) { dialogInterface, which ->
                // Delete Operation
                // Remove item from the adapter and update the list
                loginViewModel.userDelete("$userToken")
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                "Cancel", com.example.quickconnect.R.drawable.clear_icon
            ) { dialogInterface, which -> dialogInterface.dismiss() }
            .build()

        // Show Dialog
        mDialog.show()
    }

    fun observerUserDelete(){
        loginViewModel.observeUserDelete().observe(viewLifecycleOwner){deleteAccount ->

//            try {

            if(deleteAccount.status == true){
                sharedPreferences.edit().putString("deleteFlag", "").apply().toString()
                sharedPreferences.edit().remove("userToken").apply()
                val intent = Intent(context, SignUpActivity::class.java)
                startActivity(intent)
                requireActivity().finishAffinity()
            }
//            }catch (e: Exception){
//                e.printStackTrace()
//            }
        }
    }





    private fun Initialization() {

        progressBarHelper =  ProgressBarHelper(requireActivity())


        favouriteRv = binding.favouritesRv
        viewersRv = binding.viewersRv
        changePassBtn = binding.changePasswordBtn
        deleteAccPermBtn = binding.deleteAccBtn
        signOutTv = binding.signOutTv
        changePasswordBtn = binding.changePasswordBtn

        binding.terminate.setOnClickListener {
            dialog!!.dismiss()
        }

        binding.deleteAccBtn.setOnClickListener {
            userDeletePerm()
        }

    }

    private fun callFavouriteList() {
        addFavouriteViewModel.favouriteList("$userToken")
    }

    fun observeList(){
        addFavouriteViewModel.observeFavouriteList().observe(requireActivity()){favList ->

            try {

                addToFavAdapter(favList)
                originalList = favList
                favListAdapter.setData(originalList)
                favListAdapter.notifyDataSetChanged()

            favListAdapter.onItemClick = {fav->
                val nameBk = fav.username
                Log.e("TAG", "observeListwf: $", )
                val intent = Intent(context, UserViewActivity::class.java)
                intent.putExtra("bookmark","bookmark")
                intent.putExtra("BookmarkUserName",nameBk)
                startActivity(intent)
            }

            Log.e("TAG", "observeFavList:$favList")

            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

     fun addToFavAdapter(favList: MutableList<FavouriteData>?) {

        try {
            favouriteList = favList!!
            favListAdapter = FavListAdapter(favList, requireContext())
            favouriteRv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
            favouriteRv.adapter = favListAdapter
            favListAdapter.onItemClickListener = this
            favListAdapter.notifyDataSetChanged()


        }catch(e : Exception) {
            e.printStackTrace()
        }
    }

    private fun callViewersList() {
        addFavouriteViewModel.viewersList("$userToken")
    }

    fun observeViewersList(){
        addFavouriteViewModel.observeViewersData().observe(requireActivity()){viewList ->

            try {
                Log.e("TAG", "viewerLists: $viewList", )

                addToViewAdapter(viewList)
                originalListViewer = viewList
                viewersListAdapter.setData(originalListViewer)
                viewersListAdapter.notifyDataSetChanged()

//                viewersListAdapter.onItemClick = {fav->
//                    val nameBk = fav.username
//                    Log.e("TAG", "observeListwf: $", )
//                    val intent = Intent(context, UserViewActivity::class.java)
//                    intent.putExtra("bookmark","bookmark")
//                    intent.putExtra("BookmarkUserName",nameBk)
//                    startActivity(intent)
//                }

                Log.e("TAG", "observeFavList:$viewList")

            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    fun addToViewAdapter(viewerList: MutableList<ViewersList>?) {

        try {
            viewList = viewerList!!
            viewersListAdapter = ViewersListAdapter(viewerList, requireContext())
            viewersRv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
            viewersRv.adapter = viewersListAdapter
            viewersListAdapter.onItemClickListener = this
            viewersListAdapter.notifyDataSetChanged()


        }catch(e : Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(item: FavouriteData, position: Int) {
        showRemoveFav(item, position)
        Log.e("TAG", "onClickfav: ", )
    }

    private fun showRemoveFav(item: FavouriteData, position: Int) {


        val mDialog = MaterialDialog.Builder(requireActivity())
            .setTitle("Remove?")
            .setMessage("Are you sure want to remove from favourites?")
            .setAnimation(R.raw.technologies)
            .setCancelable(false)
            .setPositiveButton(
                "Remove", R.drawable.delete_icon
            ) { dialogInterface, which ->
                // Delete Operation
                // Remove item from the adapter and update the list
                favListAdapter.removeFav(position)
                favListAdapter.notifyDataSetChanged()

//                // Step 4: Update backend
//                Log.e("TAG", "itemid: ${item.id}", )
                deleteItemFromBackend(item.id!!)
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                "Cancel", R.drawable.clear_icon
            ) { dialogInterface, which -> dialogInterface.dismiss() }
            .build()

        // Show Dialog
        mDialog.show()


//        AlertDialog.Builder(requireContext(),R.style.AlertDialogStyle)
//            .setTitle("Delete Item")
//            .setMessage("Are you sure you want to remove from  favourites?")
//            .setPositiveButton("Yes") { _, _ ->
//                // Remove item from the adapter and update the list
//                favListAdapter.removeFav(position)
//                favListAdapter.notifyDataSetChanged()
//                // Step 4: Update backend
//                Log.e("TAG", "itemid: ${item.id}", )
//                deleteItemFromBackend(item.id!!)
//            }
//            .setNegativeButton("No", null)
//            .show()

    }

    private fun deleteItemFromBackend(itemId: Int) {
        addFavouriteViewModel.removeFavourite("$userToken", itemId)
        favListAdapter.notifyDataSetChanged()

        Log.e("TAG", "deleteItemFromBackend: $itemId", )

    }

    private fun observerFavRemoveData(){
        addFavouriteViewModel.observeRemoveFavourite().observe(this){removeData ->

            Log.e("TAG", "observerFavRemoveData: $removeData", )

            try {

                if(removeData.status == true){
                    val toast = TastyToast.makeText(context, "Removed from favourites", TastyToast.LENGTH_LONG, TastyToast.SUCCESS)
                    toast.setGravity(Gravity.FILL_VERTICAL,0,0)
                    toast.show()
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onClick(item: ViewersList, position: Int) {
        TODO("Not yet implemented")
    }
}