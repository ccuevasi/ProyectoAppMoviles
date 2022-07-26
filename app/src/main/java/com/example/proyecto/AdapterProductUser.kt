package com.example.proyecto

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.RowProductUserBinding

class AdapterProductUser: RecyclerView.Adapter<AdapterProductUser.HolderProductUser> ,Filterable {
    private var context: Context
    public var productArrayList: ArrayList<ModelProduct>
    public var filterList: ArrayList<ModelProduct>
    private lateinit var binding: RowProductUserBinding
    private var filter: FilterProductUser? = null

    constructor(
        context: Context,
        productArrayList: ArrayList<ModelProduct>,

    ) : super() {
        this.context = context
        this.productArrayList = productArrayList
        this.filterList = productArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProductUser {
        binding = RowProductUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderProductUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderProductUser, position: Int) {
        val model = productArrayList[position]
        val productId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val price = model.price
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        holder.titletv.text = title
        holder.descriptiontv.text = description
        holder.pricetv.text = "$"+price
        Glide.with(context)
            .load(url)
            .into(holder.productView)
        MyApplication.loadCategory(categoryId, holder.categorytv)


        holder.itemView.setOnClickListener {

            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("productId", productId)
            context.startActivity(intent)
        }

    }
    override fun getItemCount(): Int {
        return productArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterProductUser(filterList, this)
        }

        return filter as FilterProductUser
    }

    inner  class HolderProductUser(itemView: View): RecyclerView.ViewHolder(itemView){
        var productView = binding.productView
        var titletv = binding.titleTv
        var descriptiontv = binding.descriptionTv
        var pricetv = binding.priceTv
        var categorytv = binding.categoryTv
    }

}