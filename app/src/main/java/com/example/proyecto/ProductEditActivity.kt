package com.example.proyecto

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.proyecto.databinding.ActivityProductEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

 class ProductEditActivity : AppCompatActivity() {
    //View binding
    private lateinit var binding: ActivityProductEditBinding

    private companion object{
        private const val TAG = "PRODUCT_EDIT_TAG"
    }

    //book id get from intent started from AdapterProductAdmin
    private var productId = ""
     // progress dialog
     private lateinit var progressDialog: ProgressDialog
     //arraylist to hold category
     private lateinit var categoryTitleArrayList: ArrayList<String>
     //arraylist to hold category ids
     private lateinit var categoryIdArrayList: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

         productId = intent.getStringExtra("productId")!!

         //setup progress dialog
         progressDialog = ProgressDialog(this)
         progressDialog.setTitle("Porfavor Espere")
         progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadProductInfo()

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, pick category
        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        //handle click, begin update

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

     private fun loadProductInfo() {
         Log.d(TAG, "loadProductInfo: Buscando product")

         val ref = FirebaseDatabase.getInstance().getReference("Products")
         ref.child(productId)
             .addListenerForSingleValueEvent(object: ValueEventListener{
                 override fun onDataChange(snapshot: DataSnapshot) {
                     selectCategoryId = snapshot.child("categoryId").value.toString()
                     val description = snapshot.child("description").value.toString()
                     val title = snapshot.child("title").value.toString()
                     val price = snapshot.child("price").value.toString()

                     binding.titleEt.setText(title)
                     binding.descriptionEt.setText(description)
                     binding.priceEt.setText(price)


                     Log.d(TAG, "onDataChange: Loading product category info")
                     val refProductCategory = FirebaseDatabase.getInstance().getReference("Categories")
                     refProductCategory.child(selectCategoryId)
                         .addListenerForSingleValueEvent(object:ValueEventListener{
                             override fun onDataChange(snapshot: DataSnapshot) {
                                val category = snapshot.child("category").value

                                 binding.categoryTv.text = category.toString()
                             }

                             override fun onCancelled(error: DatabaseError) {

                             }

                         })
                 }

                 override fun onCancelled(error: DatabaseError) {

                 }

             })
     }

     private var title = ""
     private var description = ""
     private var price = ""
     private fun validateData() {
         //get data
         title = binding.titleEt.text.toString().trim()
         description = binding.descriptionEt.text.toString().trim()
         price = binding.priceEt.text.toString().trim()

         //validate data
         if (title.isEmpty()){
             Toast.makeText(this,"Ingresa un titulo", Toast.LENGTH_SHORT).show()
         }
         else if (description.isEmpty()){
             Toast.makeText(this,"Ingresa una descripcion", Toast.LENGTH_SHORT).show()
         }
         else if (price.isEmpty()){
             Toast.makeText(this,"Ingresa un precio", Toast.LENGTH_SHORT).show()
         }
         else if (selectCategoryId.isEmpty()){
             Toast.makeText(this,"Escoje una categoria ", Toast.LENGTH_SHORT).show()
         }
         else{
             updateProduct()
         }
     }

     private fun updateProduct() {
         Log.d(TAG,"updateProduct: Starting updating product info...")

         //show progress
         progressDialog.setMessage("Updating book info")
         progressDialog.show()

         //setup data to update to db
         val hashMap = HashMap<String, Any>()
         hashMap["title"] = "$title"
         hashMap["description"] = "$description"
         hashMap["price"] = "$price"
         hashMap["categoryId"] = "${selectCategoryId}"

         //start updating
         val ref = FirebaseDatabase.getInstance().getReference("Products")
         ref.child(productId)
             .updateChildren(hashMap)
             .addOnSuccessListener {
                 progressDialog.dismiss()
                 Log.d(TAG, "updateProduct:Actualizacion Exitosa...")
                 Toast.makeText(this,"Actualizacion Exitosa...", Toast.LENGTH_SHORT).show()
             }
             .addOnFailureListener {e ->
                 Log.d(TAG, "updateProduct: Fallo al actualizar debido a ${e.message}")
                 progressDialog.dismiss()
                 Toast.makeText(this,"Fallo al actualizar debido a ${e.message}", Toast.LENGTH_SHORT).show()
             }

     }

     private  var selectCategoryId = ""
     private  var selectCategoryTitle = ""
     private fun categoryDialog() {
         /*Show dialog to pick the category of product we already got the categories */

         //make string array from arraylist of string
         val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
         for (i in categoryTitleArrayList.indices){
             categoriesArray[i] = categoryTitleArrayList[i]
         }



             //alert dialog
         val builder = AlertDialog.Builder(this)
         builder.setTitle("Escoje una Categoria")
             .setItems(categoriesArray){dialog, position ->
                 //handle click, save clicked category id and title
                 selectCategoryId = categoryIdArrayList[position]
                 selectCategoryTitle = categoryTitleArrayList[position]

                 //set to textview
                 binding.categoryTv.text = selectCategoryTitle
             }
             .show()
     }

     private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories...")
         categoryTitleArrayList = ArrayList()
         categoryIdArrayList = ArrayList()
         val ref = FirebaseDatabase.getInstance().getReference("Categories")
         ref.addListenerForSingleValueEvent(object: ValueEventListener{
             override fun onDataChange(snapshot: DataSnapshot) {
                 //clear list before starting adding data into them
                 categoryTitleArrayList.clear()
                 categoryIdArrayList.clear()

                 for (ds in snapshot.children){
                     val id  = "${ds.child("id").value}"
                     val category = "${ds.child("category").value}"

                     categoryIdArrayList.add(id)
                     categoryTitleArrayList.add(category)

                     Log.d(TAG,"onDataChange: Category ID $id")
                     Log.d(TAG,"onDataChange: Category $category")
                 }
             }

             override fun onCancelled(error: DatabaseError) {

             }

         })
     }
 }