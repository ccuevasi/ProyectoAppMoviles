package com.example.proyecto

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.proyecto.databinding.ActivityProductAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProductAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductAddBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private var productUri: Uri? = null
    private val PICK_IMG = 1
    private val ImageList = ArrayList<Uri>()
    private var uploads = 0
    val timestamp = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadProductsCategories()


        //setup progress dialog

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Porfavor Espere")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


        //handle click, show category pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        //handle click, pick product dialog
        binding.attachImageBtn.setOnClickListener {
            productPickIntent()
        }
        binding.imagesBtn.setOnClickListener {
            imagesPickIntent()
        }

        //handle click, start uploading data/product
        binding.submitBtn.setOnClickListener {
            //STEP1: Validate Data
            //STEP2: Upload product to firebase Storage
            //STEP3: Get url of uploaded product
            //STEP4: Upload product info to firebase db

            validateData()


        }
    }



    private fun imagesPickIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMG)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMG) {
            if (resultCode == RESULT_OK) {
                if (data!!.clipData != null) {
                    val count = data.clipData!!.itemCount
                    var CurrentImageSelect = 0
                    while (CurrentImageSelect < count) {
                        val imageuri = data.clipData!!.getItemAt(CurrentImageSelect).uri
                        ImageList.add(imageuri)
                        CurrentImageSelect = CurrentImageSelect + 1
                    }
                    binding.imagesBtn!!.visibility = View.GONE
                }
            }
        }
    }


    private var title = ""
    private var description = ""
    private var category = ""
    private var price = ""

    private fun validateData() {
        //STEP1: Validate Data
        Log.d(TAG,"ValidateData: validating data")

        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        price = binding.priceEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()


        //validate data
        if (title.isEmpty()){
            Toast.makeText(this,"Ingrese el titulo...",Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()){
            Toast.makeText(this,"Ingrese una descripcion...",Toast.LENGTH_SHORT).show()
        }
        else if (category.isEmpty()){
            Toast.makeText(this,"Escoja una categoria...",Toast.LENGTH_SHORT).show()
        }
        else if(price.isEmpty()){
            Toast.makeText(this,"Ingresa un precio...",Toast.LENGTH_SHORT).show()
        }
        else if(productUri == null){
            Toast.makeText(this,"Seleccione una imagen...",Toast.LENGTH_SHORT).show()
        }
        else{
            //data validate, begin upload
            uploadProductStorage()
        }
    }

    private fun uploadProductStorage() {
        //STEP2: Upload product to firebase storage
        Log.d(TAG,"uploadProductStorage: uploading to storage...")

        //show progress dialog
        progressDialog.setMessage("Subiendo Producto")
        progressDialog.show()


        //timestamp


        //path of product in firebase storage
        val filePathAndName = "Products/$timestamp"
        //storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(productUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Log.d(TAG,"uploadProductStorage: PRODUCT uploaded now getting url...")

                //STEP3: Get url of uploaded product
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful );
                val uploadedProductUrl = "${uriTask.result}"

                uploadProductInfoToDb(uploadedProductUrl, timestamp)
            }
            .addOnFailureListener{ e ->
                Log.d(TAG,"uploadProductStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to upload due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProductInfoToDb(uploadedProductUrl: String, timestamp: Long) {
        //STEP4: Upload Product info to firebase db
        Log.d(TAG,"uploadProductInfoToDb: uploading to db")
        progressDialog.setMessage("Subiando la informacion del producto...")

        //uid of current user
        val uid = firebaseAuth.uid

        //setup data to upload
        val hashMap: HashMap<String,Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["price"] = "$price"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedProductUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0



        //db reference DB > Product > ProductId > (Product Info)
        val ref = FirebaseDatabase.getInstance().getReference("Products")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"uploadProductInfoToDb: uploaded to db")
                if (ImageList == null){
                    progressDialog.dismiss()
                    Toast.makeText(this,"Uploaded",Toast.LENGTH_SHORT).show()
                    productUri = null
                    limpiar()
                }else{
                    uploadImage()
                }

            }
            .addOnFailureListener { e->
                Log.d(TAG,"uploadProductInfoToDb:  failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to upload due to ${e.message}",Toast.LENGTH_SHORT).show()
            }


    }

    private fun uploadImage() {
        val filePathAndName = "Products/"
        //storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        uploads = 0
        while (uploads < ImageList.size) {
            val Image = ImageList[uploads]
            val imagename = storageReference.child("image/" + Image.lastPathSegment)
            imagename.putFile(ImageList[uploads]).addOnSuccessListener {
                imagename.downloadUrl.addOnSuccessListener { uri ->
                    val url = uri.toString()
                    SendLink(url,timestamp)
                }
            }
            uploads++
        }
    }

    private fun SendLink(url: String, timestamp: Long) {
        val hashMap = HashMap<String, String>()
        hashMap["link"] = url
        val ref = FirebaseDatabase.getInstance().getReference("Products")
        ref.child("$timestamp").child("Images").push()
            .setValue(hashMap).addOnCompleteListener {
                progressDialog!!.dismiss()
                Toast.makeText(this,"Uploaded",Toast.LENGTH_SHORT).show()
                productUri = null
                binding.imagesBtn.setVisibility(View.VISIBLE)
                ImageList.clear()
                limpiar()
            }
    }

    private fun loadProductsCategories() {
        Log.d(TAG,"loadProductCategories: Loading product categories")
        //init arraylist
        categoryArrayList = ArrayList()

        //db reference to load categories DF > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to arraylist
                    categoryArrayList.add(model!!)
                    Log.d(TAG,"onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""
    private fun categoryPickDialog(){
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        //get string array of categories from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Escoge una Category")
            .setItems(categoriesArray){dialog, which ->
                //handle item click
                //get clicked item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                //set category to textview
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG,"categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG,"categoryPickDialog: Selected Category Title: $selectedCategoryTitle")
            }
            .show()
    }
    private fun productPickIntent(){
        Log.d(TAG,"productPickIntent: starting product pick intent")

        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        productActivityResultLauncher.launch(intent)
    }
    val productActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            if(result.resultCode == RESULT_OK){
                Log.d(TAG,"Producto Seleccionado")
                productUri = result.data!!.data
            }
            else{
                Log.d(TAG,"Producto Seleccionado cancelado")
                Toast.makeText(this,"Cancelado",Toast.LENGTH_SHORT).show()
            }
        }
    )

    private fun limpiar(){
        binding.titleEt.text = null
        binding.descriptionEt.text = null
        binding.priceEt.text = null
        binding.categoryTv.text = null
    }



}