package com.example.proyecto

import android.widget.Filter

class FilterProductAdmin : Filter {

    var filterList: ArrayList<ModelProduct>

    var adapterProductAdmin: AdapterProductAdmin

    constructor(filterList: ArrayList<ModelProduct>, adapterProductAdmin: AdapterProductAdmin) {
        this.filterList = filterList
        this.adapterProductAdmin = adapterProductAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint
        val  results = FilterResults()

        if(constraint != null && constraint.length>0){

            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelProduct>()
            for (i in filterList.indices){
                if (filterList[i].title.lowercase().contains(constraint)){

                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            results.count = filterList.size
            results.values = filterList
        }
        return  results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
         adapterProductAdmin.productArrayList = results.values as ArrayList<ModelProduct> /* = java.util.ArrayList<com.example.proyecto.ModelProduct> */

        adapterProductAdmin.notifyDataSetChanged()
    }
}