package com.example.proyecto


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.proyecto.databinding.ActivityProductListAdminctivityBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductListAdminctivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityProductListAdminctivityBinding

    private companion object{
        const val TAG =  "PRODUCT_LIST_ADMIN_TAG"
    }

    //category id,title,price, description
    private var categoryId = ""
    private var category= ""

    //array list to hold products
    private lateinit var productArrayList: ArrayList<ModelProduct>
    //adapter
    private lateinit var adapterProductAdmin: AdapterProductAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListAdminctivityBinding.inflate(layoutInflater)
        setContentView(binding.root)







        //get from intent, that I passed from adapter
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!
        //set product category
        binding.subtitleTv.text = category

        //load product

        loadProductList()

        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {

                }
                catch (e:Exception){
                    Log.d(TAG,"onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        //handle click,go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


        //filter data

    }

    private fun loadProductList() {
        productArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Products")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    productArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelProduct::class.java)
                        if (model != null) {
                            productArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }
                    adapterProductAdmin = AdapterProductAdmin(this@ProductListAdminctivity,productArrayList)
                    binding.productsRv.adapter = adapterProductAdmin
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


}