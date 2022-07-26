package com.example.proyecto

import android.widget.Filter

class FilterProductUser: Filter {

    var filterList: ArrayList<ModelProduct>

    var adapterProductUser: AdapterProductUser

    constructor(
        filterList: ArrayList<ModelProduct>,
        adapterProductUser: AdapterProductUser
    ) : super() {
        this.filterList = filterList
        this.adapterProductUser = adapterProductUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint

        val results = FilterResults()

        if (constraint != null && constraint.length > 0){
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<ModelProduct>()
            for (i in filterList.indices){
                if(filterList[i].title.uppercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            results.count =- filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {

        adapterProductUser.productArrayList = results.values as ArrayList<ModelProduct> /* = java.util.ArrayList<com.example.proyecto.ModelProduct> */

        adapterProductUser.notifyDataSetChanged()
    }
}