package com.sumeyra.kotlinshoppinglist

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sumeyra.kotlinshoppinglist.databinding.RecyclerRowBinding

class ProductsAdapter (val productsList: ArrayList<Products>): RecyclerView.Adapter<ProductsAdapter.ProductsHolder>() {

    class ProductsHolder( val binding : RecyclerRowBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ProductsHolder(binding )
    }

    override fun getItemCount(): Int {
        return productsList.size
    }

    override fun onBindViewHolder(holder: ProductsHolder, position: Int) {
        holder.binding.recyclerRowText.text= productsList.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailsActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",productsList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }
    }
}