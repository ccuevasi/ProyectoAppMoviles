package com.example.proyecto

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.RowProductAdminBinding

class AdapterProductAdmin :RecyclerView.Adapter<AdapterProductAdmin.HolderProductAdmin>,Filterable {

    //context
    private var context: Context

    //arraylist to hold products

    var productArrayList: ArrayList<ModelProduct>
    private val filterList: ArrayList<ModelProduct>

    //viewBinding
    private lateinit var binding: RowProductAdminBinding



    //filter object
    private var filter: FilterProductAdmin? = null

    //contructor
    constructor(context: Context, productArrayList: ArrayList<ModelProduct>) : super() {
        this.context = context
        this.productArrayList = productArrayList
        this.filterList = productArrayList

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProductAdmin {
        //bind/inlfate layout
        binding = RowProductAdminBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderProductAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderProductAdmin, position: Int) {
        /*--Get Data--*/

        //get data
        val model = productArrayList[position]
        val productId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val price = model.price
        val productUrl = model.url
        val timestamp = model.timestamp
        //convert timestamp to dd/MM/yyyy

        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.priceTv.text = price
        Glide.with(context)
            .load(productUrl)
            .into(holder.productView)
        holder.dateTv.text = formattedDate

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        //handle item click, open Product DetailsActivity
        holder.itemView.setOnClickListener {
            //intent with product id
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("productId",productId)
            context.startActivity(intent)
        }

    }

    private fun moreOptionsDialog(model: ModelProduct, holder: HolderProductAdmin) {
        // get id , url , title, of book
        val productId = model.id
        val productUrl = model.url
        val productTitle = model.title

        //options to show in dialog
        val options = arrayOf("Edit","Delete")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Escoge una Opcion")
            .setItems(options){dialog, position ->
                if (position == 0){
                    val intent = Intent(context, ProductEditActivity::class.java)
                    intent.putExtra("productId",productId)
                    context.startActivity(intent)
                }
                else if (position == 1 ){
                    //Delete is clicked
                    MyApplication.deleteProduct(context, productId, productUrl, productTitle)
                }
            }
            .show()
    }

    override fun getItemCount(): Int {
        return productArrayList.size
    }
    /*View Holder class for row_product_admin.xml*/
    inner class HolderProductAdmin(itemView: View) : RecyclerView.ViewHolder(itemView){
        val productView = binding.productView
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val priceTv = binding.priceTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterProductAdmin(filterList, this)
        }

        return filter as FilterProductAdmin
    }

}