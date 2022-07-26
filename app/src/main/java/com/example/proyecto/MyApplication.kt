package com.example.proyecto

import android.app.Application
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.util.Log
import android.widget.*
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import java.util.*
import kotlin.collections.HashMap

class MyApplication:Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object{

        //created a static method to convert timestamp to proper date format, so we can use it everywhere in project, no need to rewrite again
        fun formatTimeStamp(timestamp: Long) : String{
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            //format dd/MM/yyyy
            return DateFormat.format("dd/MM/yyyy",cal).toString()
        }

        fun loadCategory(categoryId: String, categoryTv: TextView){
            //load category using category id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val category:String = "${snapshot.child("category").value}"
                        //set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        fun deleteProduct(context: Context, productId: String, productUrl: String, productTitle:String,){
            val TAG = "DELETE_PRODUCT_TAG"
            Log.d(TAG,"deleteProduct: Deleting...")

            //progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Porfavor Espere")
            progressDialog.setMessage("Deleting $productTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG,"deleteProduct: Deleting from store...")
            val storeReference = FirebaseStorage.getInstance().getReference(productUrl)
            storeReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteProduct: Eliminado de Storage")
                    Log.d(TAG, "deleteProduct: Eliminando de la bd ahora")
                    Toast.makeText(context,"Eliminado de Storage", Toast.LENGTH_SHORT).show()

                    val ref = FirebaseDatabase.getInstance().getReference("Products")
                    ref.child(productId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context,"Elimando Exitosamente", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteProduct: Eliminando exitosamente")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "deleteProduct: Fallo al eliminar de bd debido a ${e.message}")
                            Toast.makeText(context,"Fallo al eliminar debido a ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "deleteProduct: Fallo al eliminar de storage debido a ${e.message}")
                    Toast.makeText(context,"Fallo al eliminar debido a ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        fun incrementProductViewCount(productId: String){
            //Get current product views count
            val ref = FirebaseDatabase.getInstance().getReference("Products")
            ref.child(productId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount == "" || viewsCount == "null"){
                            viewsCount = "0";
                        }

                        val newViewsCount = viewsCount.toLong() + 1

                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        val dbRef = FirebaseDatabase.getInstance().getReference("Products")
                        dbRef.child(productId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        fun loadImage(context: Context,url: String,productView: ImageView ){
            Glide.with(context)
                .load(url)
                .into(productView)
        }

        public fun removeFromFavorite(context: Context, productId: String){
            val TAG = "REMOVE_FAV_TAG"

            Log.d(TAG, "removeFromFavorite: Removing from fav")

            val firebaseAuth = FirebaseAuth.getInstance()

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(productId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "removeFromFavorite: removed from fav")
                    Toast.makeText(context,"Eliminado de favoritos",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "removeFromFavorite: Failed to remove fav due to ${e.message}")
                    Toast.makeText(context,"No se puedo eliminar de favoritos debido a  ${e.message}",Toast.LENGTH_SHORT).show()

                }

        }

    }



}

