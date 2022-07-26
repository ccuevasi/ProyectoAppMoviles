package com.example.proyecto

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.RowsliderproductBinding


class AdapterSliderProduct: RecyclerView.Adapter<AdapterSliderProduct.HolderSlider> {
    private  val context: Context
    private val SliderArrayList: ArrayList<ModelImages>
    private lateinit var binding: RowsliderproductBinding


    constructor(context: Context, SliderArrayList: ArrayList<ModelImages>,)
            : super()
    {
        this.context = context
        this.SliderArrayList = SliderArrayList

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderSlider {
        binding = RowsliderproductBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderSlider(binding.root)
    }

    override fun onBindViewHolder(holder: HolderSlider, position: Int) {
        val model = SliderArrayList[position]
        try {
            Glide.with(context)
                .load(model.link)
                .into(holder.imageView)
        }
        catch (e:Exception){

        }


    }

    override fun getItemCount(): Int {
        return SliderArrayList.size
    }

    inner class HolderSlider(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageView: ImageView = binding.ImageSlider

    }


}