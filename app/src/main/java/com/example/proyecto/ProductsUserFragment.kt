package com.example.proyecto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.proyecto.databinding.FragmentProductsUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ProductsUserFragment : Fragment {

    private lateinit var binding: FragmentProductsUserBinding

    public companion object{
        private const val TAG ="PRODUCTS_USER_TAG"

        public fun newInstance(categoryId: String, category: String, uid: String):ProductsUserFragment{
            val fragment = ProductsUserFragment()

            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId =""
    private var  category = ""
    private var uid = ""

     lateinit var productArrayList: ArrayList<ModelProduct>
    private lateinit var adapterProductUser: AdapterProductUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if(args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductsUserBinding.inflate(LayoutInflater.from(context), container, false)

        Log.d(TAG, "onCreateView: Category: $category")
        if(category == "Todos"){
            loadAllProducts()
        }
        else if (category == "Mas Vistos"){
            loadMostViewedProducts("viewsCount")
        }
        else{
             loadCategorizedProduct()
        }

        binding.searchEt.addTextChangedListener {object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterProductUser.filter.filter(s)
                }catch (e: Exception){
                    Log.d(TAG, "onTextChanged: Search Exception: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        }}
        return binding.root
    }

    private fun loadAllProducts() {

        productArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Products")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                productArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelProduct::class.java)
                    productArrayList.add(model!!)
                }
                adapterProductUser =    AdapterProductUser(context!!, productArrayList)

                binding.productsRv.adapter = adapterProductUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadMostViewedProducts(orderBy: String) {

        productArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Products")
        ref.orderByChild(orderBy).limitToLast(10)
            .addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                productArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelProduct::class.java)
                    productArrayList.add(model!!)
                }
                adapterProductUser =    AdapterProductUser(context!!, productArrayList)

                binding.productsRv.adapter = adapterProductUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadCategorizedProduct() {

        productArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Products")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    productArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelProduct::class.java)
                        productArrayList.add(model!!)
                    }
                    adapterProductUser =    AdapterProductUser(context!!, productArrayList)

                    binding.productsRv.adapter = adapterProductUser
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


}