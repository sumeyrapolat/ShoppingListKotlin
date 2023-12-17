package com.sumeyra.kotlinshoppinglist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.sumeyra.kotlinshoppinglist.databinding.ActivityDetailsBinding
import java.io.ByteArrayOutputStream
import java.security.Permission
import java.sql.Statement

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var goToGalleryLauncher : ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var database : SQLiteDatabase

    var selectedBitmap : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Products", MODE_PRIVATE,null)

        registerLauncher()

        //verileri alma son aşama
        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")){
            //ekleme sayfası
            binding.productText.setText("")
            binding.priceText.setText("")
            binding.storeText.setText("")
            binding.imageView.setImageResource(R.drawable.select)

            binding.button.visibility= View.VISIBLE

        }else{
            //eski verilerin kaydedilmiş sayfası
            binding.button.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)

            val cursor= database.rawQuery("SELECT * FROM products WHERE id= ?", arrayOf(selectedId.toString()))

            //(productName, storeName, price,image)
            val productNameIx= cursor.getColumnIndex("productName")
            val storeNameIx= cursor.getColumnIndex("storeName")
            val priceIx= cursor.getColumnIndex("price")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.productText.setText(cursor.getString(productNameIx))
                binding.storeText.setText(cursor.getString(storeNameIx))
                binding.priceText.setText(cursor.getString(priceIx))

                val byteArray= cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)

            }
                cursor.close()
        }


    }

        fun save(view: View){

            val productName = binding.productText.text.toString()
            val storeName= binding.storeText.text.toString()
            val price = binding.priceText.text.toString()

            if(selectedBitmap!= null){
                val smallBitmap = makeSmaller4Bitmap(selectedBitmap!!,300)

                //outputstream yardımcı sınıfı ile görselimi bitmapten byteArray formatına çeviriyorum
                val outputStream = ByteArrayOutputStream()
                smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
                val byteArray = outputStream.toByteArray()

                //DATABASE
                try {
                    //val database = this.openOrCreateDatabase("Products", MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS products ( id INTEGER PRIMARY KEY, productName VARCHAR, storeName VARCHAR, price VARCHAR, image BLOB)")
                    val sqliteString =  "INSERT INTO products (productName, storeName, price,image) VALUES (?,?,?,?)"
                    //Soru işaretleri ile değerlerimi bağlayan bir class var
                    //bu sayede sql stringim hemen çalıştırılmıyor öncesinde bir binding işlemine süremiz oluyor
                    val statement = database.compileStatement(sqliteString)
                    statement.bindString(1,productName)
                    statement.bindString(2,storeName)
                    statement.bindString(3,price)
                    statement.bindBlob(4,byteArray)
                    statement.execute() // bu şekilde de çalıştıyoruz

                }catch (e: Exception){
                    e.printStackTrace()
                }

                //veri kaydetme işlemimiz bittikten sonra addFlag ile arkadaki tüm aktivityleri kapatmamız gerekiyor

                val intentToMain = Intent(this@DetailsActivity, MainActivity::class.java)
                intentToMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intentToMain)

            }
        }

        fun makeSmaller4Bitmap(image: Bitmap, maximumSize: Int, ): Bitmap {
        //görselimizi kaydetmeden önce küçültmemiz gerekiyor

        var width=image.width
        var height = image.height

        val bitmapRatio :Double =width.toDouble()/height.toDouble()
        if (bitmapRatio>1){
            //landscape
            width= maximumSize
            val scaledHeight= width/bitmapRatio
            height=scaledHeight.toInt()
        }else{
            //portrait
            height= maximumSize
            val scaledWidth= height/bitmapRatio
            width= scaledWidth.toInt()
        }


        return Bitmap.createScaledBitmap(image,width,height,true)
    }

        fun select(view : View){
    //Daha önce izin verildi mi kontrol etmemiz lazım
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //android33+
            if (ContextCompat.checkSelfPermission(this@DetailsActivity,Manifest.permission.READ_MEDIA_IMAGES )!= PackageManager.PERMISSION_GRANTED){
                //request permission parts
                //rational depends on totally android
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@DetailsActivity,Manifest.permission.READ_MEDIA_IMAGES)){
                    //show rational with snackbar
                    Snackbar.make(view,"Permission Needed!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                        View.OnClickListener {
                            //request permission
                            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                }else{
                    //request permission
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{

                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                goToGalleryLauncher.launch(intentToGallery)
            }
        }else {
            //android 33-
            if (ContextCompat.checkSelfPermission(this@DetailsActivity,Manifest.permission.READ_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
                //request permission parts
                //rational depends on totally android
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@DetailsActivity,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //show rational with snackbar
                    Snackbar.make(view,"Permission Needed!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                        View.OnClickListener {
                            //request permission
                            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                }else{
                    //request permission
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{

                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                goToGalleryLauncher.launch(intentToGallery)
            }
        }

    }

        fun registerLauncher(){
        //go to gallery

        goToGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode== RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){
                    val imageURI = intentFromResult.data

                    if (imageURI != null) {try {
                        if (Build.VERSION.SDK_INT>= 28){
                            val source = ImageDecoder.createSource(this@DetailsActivity.contentResolver , imageURI)
                            selectedBitmap=ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }else{
                            //SDK < 28 ise:
                            selectedBitmap =MediaStore.Images.Media.getBitmap(contentResolver,imageURI)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }

                    }catch(e : Exception){
                        e.printStackTrace()
                    } }
                    }
            }
        }
        //request permission
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if (result){
                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                goToGalleryLauncher.launch(intentToGallery)
            }else{
                //permission denied
                Toast.makeText(this@DetailsActivity, "Permission Needed !",Toast.LENGTH_LONG).show()
            }

        }
    }



}