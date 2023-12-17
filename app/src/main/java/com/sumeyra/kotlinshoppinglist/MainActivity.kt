package com.sumeyra.kotlinshoppinglist

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.sumeyra.kotlinshoppinglist.databinding.ActivityMainBinding
import kotlinx.coroutines.processNextEventInCurrentThread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var productsList : ArrayList<Products>
    private lateinit var productsAdapter: ProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view= binding.root
        setContentView(view)

        productsList = ArrayList<Products>()

        //verileri çekmeden önce recycler view ile ilgili işlemleri yapalım
        productsAdapter = ProductsAdapter(productsList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter= productsAdapter
        pullDataSQLite()



    }
    fun pullDataSQLite(){
        // databaseden veri çekmek için

        try {
            val database = this.openOrCreateDatabase("Products", MODE_PRIVATE,null)
            val cursor= database.rawQuery("SELECT * FROM products ",null)
            val nameIx= cursor.getColumnIndex("productName")
            val idIx= cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                val name= cursor.getString(nameIx)
                val id = cursor.getInt(idIx)
                //Products class oluşturduk ve initilize ettik
                val products = Products(name,id)
                //ArrayList oluşturduk ve initilize ettik
                productsList.add(products)
            }
            productsAdapter.notifyDataSetChanged()
            cursor.close()

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //Menu işlemleri:
    //1-
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //Inflater

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_product,menu)

        return super.onCreateOptionsMenu(menu)
    }

    //2-
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Intent to details activity
        if(item.itemId==R.id.add_product){
            val intent = Intent(this@MainActivity, DetailsActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}