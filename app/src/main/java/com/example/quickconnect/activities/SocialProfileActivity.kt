package com.example.quickconnect.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.SearchView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.R
import com.example.quickconnect.adapters.SocialAccountAdapter
import com.example.quickconnect.databinding.ActivitySocialProfileBinding
import com.example.quickconnect.model.sociallinksdata.CategoryData
import com.example.quickconnect.model.sociallinksdata.SocialLinkListData
import com.example.quickconnect.repository.SocialLinksRepositary
import com.example.quickconnect.utils.NoInternetUtils
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.SocialLinksViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sdsmdg.tastytoast.TastyToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SocialProfileActivity : AppCompatActivity() , SocialAccountAdapter.ItemClickListener,SocialAccountAdapter.SwitchChangeListener{

    private lateinit var binding: ActivitySocialProfileBinding
    lateinit var progressBarHelper: ProgressBarHelper


    lateinit var socialAccountAdapter: SocialAccountAdapter
    lateinit var socialAcountList : ArrayList<SocialLinkListData>
    lateinit var socialAcRV : RecyclerView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var userSharedPreferences : SharedPreferences
    lateinit var userToken : String


     private var searchQuery: String = ""
    private var originalList: List<CategoryData> = emptyList()


    lateinit var switch : Switch
     var switchAc : Boolean? = null

    private val handler = Handler(Looper.getMainLooper())
    private var searchJob: Job? = null
    private val SEARCH_DELAY = 500L // Adjust this delay according to your needs






    lateinit var saveUrl : String

      var category_id : Int? = null
    lateinit var link: String

    //    var is_locked  = 0
    lateinit var name : String
    lateinit var url : String
     var selectedItemId : Int = -1

    lateinit var updateLink : String


    private  val SOCIAL_LIST_KEY = "social_list"



    private val enteredUrls: MutableMap<Int, String> = mutableMapOf()

  //gvfcycvguv


    private val socialDataViewModel: SocialLinksViewModel by lazy {
        val appDataViewModelFactory = SocialLinksRepositary()
        ViewModelProvider(this, appDataViewModelFactory)[SocialLinksViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        progressBarHelper =  ProgressBarHelper(this)


        if (!NoInternetUtils.isInternetAvailable(this)) {
            NoInternetUtils.showNoInternetDialog(this)
            binding.noListImg.isVisible = true
            progressBarHelper.hideProgressDialog()
        }

        val swicvf = findViewById<Switch>(R.id.switch_action)

        if (switchAc == true){

            swicvf.isChecked = true

        }



        userSharedPreferences = getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        userToken = userSharedPreferences.getString("userToken","").toString()

        sharedPreferences = getSharedPreferences("GetListData", Context.MODE_PRIVATE)
//        saveUrl = sharedPreferences.getString("urlString", "").toString()

        observerSocialListData()


//        searchView.queryHint = "Search"
        try {

        binding.socialProfileDneTv.setOnClickListener {
//            try {

            if (link.isNullOrBlank() || link == "null"){
                saveUrlData()
            }else {
                url = binding.socialProfileUrlEt.text.trim().toString()
                if (url.isEmpty()){
                    TastyToast.makeText(this, "Please enter URL!", TastyToast.LENGTH_LONG, TastyToast.CONFUSING)

//                    Toast.makeText(this, "Please enter URL!", Toast.LENGTH_SHORT).show()
                }else{
                    updateLinkData(category_id!!, url)
                    Log.e("TAG", "updateurl;:$url ")
                }
            }


//        }catch (e:Exception) { e.printStackTrace() }
        }
        }catch (e: Exception){
            e.printStackTrace()
        }
        binding.cancel.setOnClickListener {
            binding.socialProfileUrlLinear.isVisible = false
        }


        observerSocialUpdateData()
        observerSocialCreateData()
        searchView()
//        if (category_id != null) {
//            switchUpdate(category_id!!)
//        }
//        updateData ()
        socialDataViewModel.socialListAll("$userToken")
        Log.e("TAG", "onCreate: $userToken")


        binding.onBackTv.setOnClickListener {
            onBackPressed()
        }


        socialAcRV = binding.socialAcRv
        socialAccountAdapter = SocialAccountAdapter(originalList, this)
        socialAcRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_slide_from_bottom)
        socialAcRV.layoutAnimation = animation
        socialAcRV.adapter = socialAccountAdapter

    }


    fun searchView(){

        val searchView = findViewById<SearchView>(R.id.searchView)

        searchView.setOnClickListener {
            searchView.isIconified = false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                socialAccountAdapter.filter(newText.orEmpty())
                socialAccountAdapter.notifyDataSetChanged()
                return true
            }
        })
    }


    private fun applyFilter(newList: List<CategoryData>) {
        socialAcountList?.clear()
        socialAcountList?.addAll(newList.map { it as SocialLinkListData })
        socialAccountAdapter?.notifyDataSetChanged()
    }



    private fun saveUrlData() {
        url = binding.socialProfileUrlEt.text.trim().toString()
        Log.e("TAG", "saveUrlData: $url", )
        if (url.isEmpty()){
            TastyToast.makeText(this, "Please enter URL!", TastyToast.LENGTH_LONG, TastyToast.CONFUSING)

//            Toast.makeText(this, "Please enter URL", Toast.LENGTH_SHORT).show()
        }else{
            CraeteData(category_id!!, url)
        }
    }


    @SuppressLint("ResourceAsColor")
    private fun CraeteData(id : Int,url : String ) {

        try {

        val isLocked = socialAccountAdapter.socialList.firstOrNull { it.id == id }?.links!!.isLocked ?: false

        Log.e("TAG", "nameidurl: $name + $id + $url", )

        socialDataViewModel.createSocialLink("$userToken",
            name,
            id,
            url,
            if (isLocked) 1 else 0)


        binding.socialProfileUrlLinear.isVisible = false

        socialAccountAdapter.notifyDataSetChanged()

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun observerSocialCreateData() {

//        progressBarHelper.showProgressDialog()

        socialDataViewModel.observbleCreateSocialLink().observe(this) { CreateData ->
//            progressBarHelper.hideProgressDialog()

            try {

            Log.e("TAG", "observbleCreateSocialLink: $CreateData", )

            if (CreateData.status == true) {
                // Refresh the social list data



                // Clear the enteredUrls map
                socialDataViewModel.socialListAll("$userToken")

                observerSocialListData()



                // Notify the adapter that the data has changed
                socialAccountAdapter.notifyDataSetChanged()

                // Add any additional UI update logic here

                // Optionally, recreate the activity
//                recreate()
            }

            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("ResourceAsColor", "SuspiciousIndentation")
    private fun observerSocialListData() {

        progressBarHelper.showProgressDialog()

        // Check if there is cached data in SharedPreferences
        val cachedSocialListJson = sharedPreferences.getString(SOCIAL_LIST_KEY, null)
        if (cachedSocialListJson != null) {
            val cachedSocialList = Gson().fromJson<List<CategoryData>>(
                cachedSocialListJson,
                object : TypeToken<List<CategoryData>>() {}.type
            )
            addTADataToAdapter(cachedSocialList)
        }

        socialDataViewModel.observbleSocialLinksAll().observe(this){ListAllData->


//            for (item in ListAllData) {
//                val existingItem = socialAcountList.find { it.data.last().id == item.id }
//                existingItem?.data?.last()?.links?.isLocked = item.links?.isLocked == true
//            }


            Log.e("TAG", "observerSocialLisefvrwtData: $ListAllData", )

            try {

//                originalList = ListAllData

                addTADataToAdapter(ListAllData)

                originalList = ListAllData
                socialAccountAdapter.setData(originalList)
                socialAccountAdapter.notifyDataSetChanged()


//                applyFilter(originalList)

                socialAccountAdapter.onItemClick = {data ->

                    binding.socialProfileUrlLinear.isVisible = true

                    name = data.name.toString()
                    category_id = data.id

                    // Perform null checks before accessing properties of data.links
                    link = data.links?.link.toString()
                    links_id = data.links?.id ?: -1 // Use a default value or handle null case appropriately
                    switchAc = data.links!!.isLocked

                    Log.e("TAG", "linkSearch: $link", )
                    Log.e("TAG", "namecc: $name", )

                    val editor = sharedPreferences.edit()
                    editor.putString("name", name)
                    editor.putInt("category_id", category_id!!)
                    editor.putInt("link_id", links_id!!)
                    editor.putString("link", link)
                    editor.apply()

                    Log.e("TAG", "observerSocialListDatalinkid:$links_id")

                fun CharSequence?.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

                    binding.socialProfileUrlEt.setText(link?.takeIf { it.isNotEmpty() } ?: "")

                    if (link == "null"){
                        binding.socialProfileUrlEt.text = "".toEditable()
                    }else{
                       binding.socialProfileUrlEt.setText(link)

                    }
                }

                socialAccountAdapter.notifyDataSetChanged()

                // Save the latest data to SharedPreferences for caching
                val json = Gson().toJson(ListAllData)
                sharedPreferences.edit().putString(SOCIAL_LIST_KEY, json).apply()




                Log.e("TAG", "observerSocialData:$category_id + $link  ", )


            }catch(e: Exception){
                e.printStackTrace()
            }
        }
    }


    fun addTADataToAdapter(socialLinkListAll : List<CategoryData>) {


         try {

             progressBarHelper.hideProgressDialog()

             socialAcRV = binding.socialAcRv

             socialAcountList = ArrayList()
             socialAccountAdapter = SocialAccountAdapter(socialLinkListAll, this)

             socialAcRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
             socialAcRV.adapter = socialAccountAdapter
             socialAccountAdapter.itemClickListener = this
             socialAccountAdapter.switchChangeListener = this
             val animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_slide_from_bottom)
             socialAcRV.layoutAnimation = animation
             socialAccountAdapter.notifyDataSetChanged()

         }catch(e : Exception) {
             e.printStackTrace()
         }
    }

    private fun updateLinkData(id : Int,url : String ) {


        val isLocked = socialAccountAdapter.socialList.firstOrNull { it.id == id }?.links!!.isLocked ?: false


        socialDataViewModel.socialLinkUpadte("$userToken",
            id,
            url,
            if (isLocked) 1 else 0
        )
        binding.socialProfileUrlLinear.isVisible = false
    }

    private fun switchUpdate(id : Int) {

        val isLocked = socialAccountAdapter.socialList.firstOrNull { it.id == id }?.links!!.isLocked ?: false

        socialDataViewModel.switchUpadte("$userToken",
            id,
            if (isLocked) 1 else 0
        )

    }



    private fun observerSocialUpdateData() {
        socialDataViewModel.observbleSocialLinkUpdate().observe(this){updateData->

            Log.e("TAG", "observerUpdateLinkData: $updateData")
            try {

//                if (updateData.status == true){



                     updateLink = updateData.data!!.link.toString()

                    binding.socialProfileUrlEt.setText(updateLink)

                socialAccountAdapter.notifyDataSetChanged()

//                }

            }catch(e: Exception){
                e.printStackTrace()
            }
        }
    }



    @SuppressLint("SuspiciousIndentation")
    override fun onItemClick(item: CategoryData, id : Int) {
//        val socialProfileFragment = SocialProfileFragment()
//            socialProfileFragment.show(supportFragmentManager, socialProfileFragment.tag)
        binding.socialProfileUrlLinear.isVisible = true

        // Save the ID and clear the EditText
        category_id = id
        name= item.name.toString()

        // Show the entered URL for the selected item in the activity's EditText
        binding.socialProfileUrlEt.setText(socialAccountAdapter.enteredUrls[id])

        Log.e("TAG", "onItemClick: $selectedItemId")

    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        socialAccountAdapter.onSaveInstanceState(outState)
//    }

//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        socialAccountAdapter.onRestoreInstanceState(savedInstanceState)
//    }


    companion object{
        var links_id : Int? = null
    }

    override fun onSwitchChanged(id: Int, isChecked: Boolean) {
//        switchUpdate(id)
        socialDataViewModel.switchUpadte(userToken, id, if (isChecked) 1 else 0)

    }
}