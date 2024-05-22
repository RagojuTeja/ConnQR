package com.example.quickconnect.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quickconnect.ApiServices.Api
import com.example.quickconnect.ApiServices.RetrofitClient
import com.example.quickconnect.R
import com.example.quickconnect.activities.MainActivity
import com.example.quickconnect.adapters.CallToActionAdapter
import com.example.quickconnect.adapters.TypeofActionAdapter
import com.example.quickconnect.databinding.FragmentCallToActionBinding
import com.example.quickconnect.model.CallToActionModel.ActionList
import com.example.quickconnect.model.CallToActionModel.CountActionData
import com.example.quickconnect.repository.CallToActionRepositary
import com.example.quickconnect.utils.NoInternetUtils
import com.example.quickconnect.utils.ProgressBarHelper
import com.example.quickconnect.viewmodels.CallToActionViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sdsmdg.tastytoast.TastyToast
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallToActionFragment : BottomSheetDialogFragment(), CallToActionAdapter.OnMessageLongClickListener {

    private lateinit var binding: FragmentCallToActionBinding
    lateinit var progressBarHelper: ProgressBarHelper


    lateinit var callToActionAdapter: CallToActionAdapter
    lateinit var callToActionList: MutableList<ActionList>

    lateinit var taET : EditText
    lateinit var nameEt : EditText
    lateinit var linkEt : EditText
     var category_id : Int? = null

//    private var editProfileListener: EditProfileListener? = null

    private var mainActivityListener: MainActivityListener? = null




    lateinit var sharedPreferences: SharedPreferences
    lateinit var userSharedPreferences : SharedPreferences
    lateinit var userToken : String
     var countAction : Int?  = null
    var listCount : Int?  = null

    private val  callToActionViewModel: CallToActionViewModel by lazy {
        val callToActiionViewModelFactory = CallToActionRepositary()
        ViewModelProvider(this, callToActiionViewModelFactory)[CallToActionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentCallToActionBinding.inflate(layoutInflater)

        if (!NoInternetUtils.isInternetAvailable(requireContext())) {
            NoInternetUtils.showNoInternetDialog(requireContext())
        }

        Initialization()

        userSharedPreferences = requireContext().getSharedPreferences("UserData" , Context.MODE_PRIVATE)
        userToken = userSharedPreferences.getString("userToken","").toString()

        sharedPreferences = requireActivity().getSharedPreferences("CallToActionData", Context.MODE_PRIVATE)

        actionTypeDropDown()
        observerActionTypeListData()
        actionCreate()
        observerCreateAction()
        callActionList()
        observerCallToListData()
        countActions()


        return binding.root
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), theme)
        bottomSheetDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                // Set the background color
                val backgroundColor = ContextCompat.getColor(requireContext(), R.color.light_ashtr)
                sheet.setBackgroundColor(backgroundColor)

                // Apply blur effect to the background with semi-transparent overlay
                applyBlurWithOverlay(sheet)
            }
        }
        return bottomSheetDialog
    }

    private fun applyBlurWithOverlay(view: View) {
        // Create a bitmap of the view's content
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        // Create a semi-transparent overlay
        val overlayColor = Color.parseColor("#99CCCCC8") // Adjust transparency as needed
        val overlayPaint = Paint()
        overlayPaint.color = overlayColor
        canvas.drawRect(0f, 0f, view.width.toFloat(), view.height.toFloat(), overlayPaint)

        // Apply blur effect to the bitmap
        val blurredBitmap = blurBitmap(bitmap, 25f) // Adjust blur radius as needed

        // Set the blurred bitmap as the background
        val drawable = BitmapDrawable(resources, blurredBitmap)
        view.background = drawable
    }

    private fun blurBitmap(bitmap: Bitmap, radius: Float): Bitmap {
        val rsContext = RenderScript.create(requireContext())
        val blurScript = ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext))

        val input = Allocation.createFromBitmap(rsContext, bitmap)
        val output = Allocation.createTyped(rsContext, input.type)

        blurScript.setRadius(radius)
        blurScript.setInput(input)
        blurScript.forEach(output)

        output.copyTo(bitmap)

        rsContext.destroy()

        return bitmap
    }


    fun Initialization (){

        progressBarHelper =  ProgressBarHelper(requireActivity())


        taET = binding.taEt
        nameEt = binding.actionNameEt
        linkEt = binding.actionUrlEt

        binding.terminate.setOnClickListener {
            dialog!!.dismiss()
        }
    }

    fun actionTypeDropDown() {

        binding.taEt.setOnClickListener {
            callToActionViewModel.typeCategorieList()
        }
    }


    private fun observerActionTypeListData() {
        callToActionViewModel.observbleCallActionTypeList().observe(this) { actionTypeList ->
            Log.e("TAG", "observerCallToListData:$actionTypeList")

            try {

            binding.typeCard.isVisible = true

            val typeList = actionTypeList

            binding.stateRv.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            val typeofActionAdapter = TypeofActionAdapter(typeList, requireContext())
            binding.stateRv.adapter = typeofActionAdapter
            typeofActionAdapter.notifyDataSetChanged()

            Log.e("slisr", "stateDropDown: $typeList")

            typeofActionAdapter.onItemClick = {type ->

                Log.e("TAG", "Item clicked: ${type.name}")

                binding.typeCard.isVisible = false
                binding.taEt.setText(type.name)
                category_id = type.id

                val editor = sharedPreferences.edit()
                editor.putInt("category_id", category_id!!)
                editor.apply()

            }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }


    fun actionCreate() {

        binding.actionDoneTv.setOnClickListener {

            try {

            if (listCount == countAction){

                val mDialog = MaterialDialog.Builder(requireActivity())
                    .setTitle("Alert")
                    .setMessage("You can add 4 actions only. If you want to add more, remove existing ones.")
                    .setCancelable(true)
                    .setPositiveButton("Ok") { dialogInterface, which ->
                        dialogInterface.dismiss()
                    }
                    .build()

// Show Dialog
                mDialog.show()

//                AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
//                    .setTitle("Remove Action")
//                    .setMessage("You Can add 4 actions olny if we you want add! remove existing one")
//                    .setPositiveButton("ok") { _, _ ->
//                        dismiss()
//                    }
////                    .setNegativeButton("No", null)
//                    .show()

//               Toast.makeText(context, "You Can add 4 actions olny if we you want add new one remove existing", Toast.LENGTH_SHORT).show()

            }else {

//                editProfileListener?.onProfileUpdated()


                val typeofActionString = taET.text.trim().toString()
                val nameString = nameEt.text.trim().toString()
                val urlString = linkEt.text.trim().toString()

                Log.e("TAG", "nameString: $nameString")
                Log.e("TAG", "urlString: $urlString")
                Log.e("TAG", "actionCreate: $category_id")


                if (typeofActionString.isEmpty() ) {
                    TastyToast.makeText(context, "Select type of action", TastyToast.LENGTH_LONG, TastyToast.ERROR)
                } else if (nameString.isEmpty()) {
                    TastyToast.makeText(context, "Enter Name", TastyToast.LENGTH_LONG, TastyToast.ERROR)
                }else if (urlString.isEmpty()) {
                    TastyToast.makeText(
                        context,
                        "Enter URL",
                        TastyToast.LENGTH_LONG,
                        TastyToast.ERROR
                    )
                }else{


                    callToActionViewModel.createAction(
                        "$userToken",
                        nameString, urlString, category_id!!
                    )

                }

            }

            }catch (e: Exception){
                e.printStackTrace()
            }
       }

    }

    private fun observerCreateAction() {
        callToActionViewModel.observbleCreateAction().observe(this) { createaction ->
            Log.e("TAG", "observerCallToListData:$createaction")

            callActionList()

            val wffew= MainActivity()



//            wffew.callToActionAdapter
//
//            callToActionAdapter.notifyDataSetChanged()
        }
    }


    fun callActionList() {
            callToActionViewModel.callToActionList("$userToken")
    }

    @SuppressLint("SuspiciousIndentation")
    private fun observerCallToListData() {
        callToActionViewModel.observbleCallActionList().observe(this) { actionList ->
            Log.e("TAG", "observerCallToListData:$actionList")

            try {


                if (actionList.isNotEmpty()){
                    binding.cardViewCallAction.isVisible = true
                }

//            if (actionList.count() == 4) {
//                Toast.makeText(context, "Add 4 Actions only", Toast.LENGTH_SHORT).show()
//            }else{
                addTADataToAdapter(actionList)

                mainActivityListener!!.onApiCallCompleted()

            listCount = actionList.count()

                nameEt.text.clear()
                taET.text.clear()
                linkEt.text.clear()

//                binding.cardViewCallAction.isVisible = true
//            }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun addTADataToAdapter(actionListListAll : MutableList<ActionList>) {


        try {

//            if (actionListListAll.size > 4){
//                Toast.makeText(context, "You Can add 4 actions olny if we you want add new one remove existing", Toast.LENGTH_SHORT).show()
//            }else {

                binding.callToactionRv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                callToActionAdapter = CallToActionAdapter(actionListListAll, requireContext())
            callToActionAdapter.onMessageLongClickListener = this
                binding.callToactionRv.adapter = callToActionAdapter
            callToActionList = actionListListAll
            callToActionAdapter.onMessageLongClickListener = this
            callToActionAdapter.notifyDataSetChanged()

            callToActionAdapter.onItemClick = { url ->

                try {

                    val url = url.link

                    // Handle item click by opening the web browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                    // Check if the caught exception is an ActivityNotFoundException
                    if (e is ActivityNotFoundException) {
                        // Display a Toast message
                        TastyToast.makeText(requireContext(), "Please check URL", TastyToast.LENGTH_LONG, TastyToast.INFO)

//                        Toast.makeText(this, "Please check URL", Toast.LENGTH_SHORT).show()
                    }
                }
            }



//            dialog!!.dismiss()
//            }

        }catch(e : Exception) {
            e.printStackTrace()
        }
    }

    private fun countActions(){

        try {

         val apiServices = RetrofitClient.client.create(Api::class.java)


        val call = apiServices.countActions("Token $userToken")
        call.enqueue(object : Callback<CountActionData> {
            override fun onResponse(call: Call<CountActionData>, response: Response<CountActionData>) {
                if (response.isSuccessful){

//                    val count = response.body()!!.data.

                    countAction = response.body()?.data?.firstOrNull()?.count


                }
            }

            override fun onFailure(call: Call<CountActionData>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: retro failure $t", )
            }

        })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    override fun onMessageLongClick(item: ActionList, position: Int) {
        showDeleteConfirmationDialog(item, position)
    }

    private fun showDeleteConfirmationDialog(item: ActionList, position: Int) {

        val mDialog = MaterialDialog.Builder(requireActivity())
            .setTitle("Delete?")
            .setMessage("Are you sure want to delete this file?")
            .setAnimation(R.raw.technologies)
            .setCancelable(false)
            .setPositiveButton(
                "Delete", R.drawable.delete_icon
            ) { dialogInterface, which ->
                // Delete Operation
                // Remove item from the adapter and update the list
                callToActionAdapter.removeItem(position)
                callToActionAdapter.notifyDataSetChanged()
                // Step 4: Update backend
                deleteItemFromBackend(item.id!!)
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                "Cancel", R.drawable.clear_icon
            ) { dialogInterface, which -> dialogInterface.dismiss() }
            .build()

        // Show Dialog
        mDialog.show()

//        AlertDialog.Builder(requireContext())
//            .setTitle("Delete Item")
//            .setMessage("Are you sure you want to delete this item?")
//            .setPositiveButton("Yes") { _, _ ->
//                // Remove item from the adapter and update the list
//                callToActionAdapter.removeItem(position)
//                callToActionAdapter.notifyDataSetChanged()
//                // Step 4: Update backend
//                deleteItemFromBackend(item.id!!)
//            }
//            .setNegativeButton("No", null)
//            .show()
    }

        private fun deleteItemFromBackend(itemId: Int) {
        // Implement backend logic to delete the item
        // This could involve calling a ViewModel or Repository method
        // to update the data in your backend system.
        callToActionViewModel.deleteAction("$userToken", itemId)
            callToActionAdapter.notifyDataSetChanged()

    }


//    interface EditProfileListener {
//        fun onProfileUpdated()
//    }

    interface MainActivityListener {
        fun onApiCallCompleted()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivityListener) {
            mainActivityListener = context
        } else {
            throw ClassCastException("$context must implement MainActivityListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivityListener = null
    }

//    private fun notifyMainActivity() {
//        mainActivityListener?.onApiCallCompleted()
//    }




}