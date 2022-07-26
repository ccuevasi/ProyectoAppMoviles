package com.example.proyecto

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.RowProductFavoriteBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class AdapterProductFavorite : RecyclerView.Adapter<AdapterProductFavorite.HolderProductFavorite> {

    private val context: Context
    private var productsArrayList: ArrayList<ModelProduct>

    private lateinit var binding: RowProductFavoriteBinding

    constructor(context: Context, productsArrayList: ArrayList<ModelProduct>) {
        this.context = context
        this.productsArrayList = productsArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProductFavorite {
        binding = RowProductFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderProductFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderProductFavorite, position: Int) {
        val model = productsArrayList[position]
        loadProductDetails(model,holder)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("productId", model.id)
            context.startActivity(intent)
        }

        holder.removeFavBtn.setOnClickListener {
            MyApplication.removeFromFavorite(context, model.id)
        }
    }

    private fun loadProductDetails(model: ModelProduct, holder: AdapterProductFavorite.HolderProductFavorite) {
        val productId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Products")
        ref.child(productId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val price = "${snapshot.child("price").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    model.isFavorite = true
                    model.title  = title
                    model.description = description
                    model.categoryId = categoryId
                    model.price = price
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()


                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    MyApplication.loadCategory("$categoryId",holder.categoryTv)
                    Glide.with(context).load(url).into(holder.productView)
                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.priceTv.text = price




                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun getItemCount(): Int {
       return  productsArrayList.size
    }

    inner class HolderProductFavorite(itemView: View): RecyclerView.ViewHolder(itemView){
    var productView = binding.productView
    var titleTv = binding.titleTv
    var removeFavBtn = binding.removeFavBtn
    var descriptionTv = binding.descriptionTv
    var categoryTv = binding.categoryTv
    var priceTv = binding.priceTv
    }
}