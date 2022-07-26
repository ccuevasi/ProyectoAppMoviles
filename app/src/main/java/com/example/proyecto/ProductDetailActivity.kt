package com.example.proyecto

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.proyecto.databinding.ActivityProductDetailBinding
import com.example.proyecto.databinding.DialogCommentAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay

class ProductDetailActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityProductDetailBinding



    private companion object{
        const val TAG = "BOOK_DETAILS_TAG"
    }

    private var productId = ""

    private var isInMyFavorite = false
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var commentArrayList: ArrayList<ModelComment>
    private lateinit var adapterComment: AdapterComment
    private lateinit var SliderArrayList : ArrayList<ModelImages>
    private lateinit var adapterSlider: AdapterSliderProduct
    private lateinit var viewPager : ViewPager2





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Porfavor Espere")

        
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null){
            checkIsFavorite()
        }

        //get product id from intent
        productId = intent.getStringExtra("productId")!!
        MyApplication.incrementProductViewCount(productId)
        loadProductDetails()

        showComments()
        //handle back button click
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, add/remove favorite
        binding.favoriteBtn.setOnClickListener {
            if(firebaseAuth.currentUser == null){
                Toast.makeText( this,"No has iniciado sesion", Toast.LENGTH_SHORT).show()
            }
            else{
                if (isInMyFavorite){
                    MyApplication.removeFromFavorite(this,productId)
                }
                else{
                    addToFavorite()
                }
            }
        }

        binding.addCommentBtn.setOnClickListener {
            if (firebaseAuth.currentUser == null){
                Toast.makeText(this, "No has iniciado sesion", Toast.LENGTH_SHORT).show()
            }
            else{
                addCommentDialog()
            }
        }

    }




    private fun loadImages() {
        SliderArrayList = ArrayList()
        viewPager = binding.productView
        val ref = FirebaseDatabase.getInstance().getReference("Products")
        ref.child(productId).child("Images")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    SliderArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelImages::class.java)

                        SliderArrayList.add(model!!)
                    }
                    adapterSlider = AdapterSliderProduct(this@ProductDetailActivity,SliderArrayList)

                    viewPager.adapter = adapterSlider
                    viewPager.autoScroll(lifecycleScope,2000)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }
    private fun ViewPager2.autoScroll(lifecyclerScope: LifecycleCoroutineScope, interval: Long){
        lifecyclerScope.launchWhenResumed {
            scrollIndefinitely(interval)
        }
    }
    private  suspend fun ViewPager2.scrollIndefinitely(interval:Long){
        delay(interval)
        val numberOfItems = adapterSlider?.itemCount ?:0
        val lastIndex = if (numberOfItems>0) numberOfItems-1 else 0
        val nextItem = if (currentItem==lastIndex) 0 else currentItem+1

        setCurrentItem(nextItem, true)
        scrollIndefinitely(interval)
    }

    private fun showComments() {
        commentArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Products")
        ref.child(productId).child("Comments")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelComment::class.java)

                        commentArrayList.add(model!!)
                    }
                    adapterComment = AdapterComment(this@ProductDetailActivity,commentArrayList)

                    binding.commentRV.adapter = adapterComment
                }


                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private var comment = ""
    private fun addCommentDialog() {
        var commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this,R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()

        commentAddBinding.backBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        commentAddBinding.submitBtn.setOnClickListener {
            comment = commentAddBinding.commentEt.text.toString().trim()
            
            if (comment.isEmpty()){
                Toast.makeText(this, "Ingrese un comentario", Toast.LENGTH_SHORT).show()
            }
            else{
                alertDialog.dismiss()
                addComment()
            }
            
        }

    }

    private fun addComment() {

        progressDialog.setTitle("Agregando Comentario")
        progressDialog.show()

        val timestamp = "${System.currentTimeMillis()}"

        val hashMap = HashMap<String,Any>()
        hashMap["id"] = "$timestamp"
        hashMap["productId"] = "$productId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Products")
        ref.child(productId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Comentario Agregado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo agregar el comentario debido a ${e.message} ", Toast.LENGTH_SHORT).show()

            }
    }

    private fun loadProductDetails() {
        //Product > productId > Detail

        val ref = FirebaseDatabase.getInstance().getReference("Products")
        ref.child(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val price = "${snapshot.child("price").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val views = "${snapshot.child("viewsCount").value}"
                    loadImages()
                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    MyApplication.loadCategory(categoryId, binding.categoryTv)






                    binding.titleTv.text = title
                    binding.dateTv.text = date
                    binding.descriptionTv.text = description
                    binding.priceTv.text = price
                    binding.viewTv.text = views
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
    
    private fun checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: Checking if book is in fav or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(productId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite  = snapshot.exists()
                    if (isInMyFavorite){
                        Log.d(TAG, "onDataChange: available in favorite")
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_filled_white,0,0)
                        binding.favoriteBtn.text = "Eliminar de Favoritos"
                    }
                    else{
                        Log.d(TAG, "onDataChange: ")

                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_white,0,0)
                        binding.favoriteBtn.text = "Añadir a Favoritos"

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
    
    private fun addToFavorite(){
        val timestamp = System.currentTimeMillis()
        
        
        val hashMap = HashMap<String, Any>()
        hashMap["productId"] = productId
        hashMap["timestamp"] = timestamp
        
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(productId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addToFavorite: Added to fav")
                Toast.makeText(this,"Añadido a Favoritos",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "addToFavorite: Failed to add to fav due to ${e.message}")
                Toast.makeText(this,"No se puedo agregar a favoritos  debido a  ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }


}