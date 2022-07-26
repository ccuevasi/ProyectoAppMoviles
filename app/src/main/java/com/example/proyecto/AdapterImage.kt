package com.example.proyecto

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.RowImageBinding

class AdapterImage:RecyclerView.Adapter<AdapterImage.HolderImage> {

    private val context : Context
    var ImageArrayList: ArrayList<ModelImages>
    private lateinit var binding: RowImageBinding

    constructor(context: Context, ImageArrayList: ArrayList<ModelImages>) {
        this.context = context
        this.ImageArrayList = ImageArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImage {
        binding = RowImageBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderImage(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImage, position: Int) {
        val model = ImageArrayList[position]


        try {
            Glide.with(context)
                .load(model.link)
                .into(holder.imageView)
        }
        catch (e:Exception){

        }
    }

    override fun getItemCount(): Int {
        return ImageArrayList.size
    }
    inner class HolderImage(itemView: View): RecyclerView.ViewHolder(itemView){
        var imageView: ImageView = binding.productView


    }
}