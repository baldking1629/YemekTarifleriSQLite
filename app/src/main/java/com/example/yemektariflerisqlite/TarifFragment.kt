package com.example.yemektariflerisqlite

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.yemektariflerisqlite.databinding.FragmentTarifBinding
import java.io.ByteArrayOutputStream

class TarifFragment : Fragment() {

    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTarifBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.setOnClickListener {
            kaydet(it)
        }

        binding.imageView.setOnClickListener{
            gorselSec(it)
        }

        arguments?.let {

            var gelenbilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if(gelenbilgi.equals("menudengeldim")){
                //yeni bir yemek eklemeye geldi
                binding.yemekIsmiText.setText("")
                binding.yemekMalzemeText.setText("")
                binding.button.visibility = View.VISIBLE

                val gorselSecmeArkaPlani = BitmapFactory.decodeResource(context?.resources,R.drawable.gorselsecimi)
                binding.imageView.setImageBitmap(gorselSecmeArkaPlani)

            }else{
                //daha önce oluşturulan yemeği görmeye geldi
                binding.button.visibility = View.INVISIBLE

                val secilenId = TarifFragmentArgs.fromBundle(it).id
                context?.let {
                    try {

                        val db = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                        val cursor = db.rawQuery("Select * From yemekler Where id = ?", arrayOf(secilenId.toString()))

                        val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeIndex = cursor.getColumnIndex("yemekmalzemesi")
                        val yemekGorseli = cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()){
                            binding.yemekIsmiText.setText(cursor.getString(yemekIsmiIndex))
                            binding.yemekMalzemeText.setText(cursor.getString(yemekMalzemeIndex))

                            val byteDizisi = cursor.getBlob(yemekGorseli)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                            binding.imageView.setImageBitmap(bitmap)


                        }

                        cursor.close()

                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                }


            }

        }

    }

    fun kaydet(view: View){
        //SQLite a Kaydetme
        val yemekIsmi = binding.yemekIsmiText.text.toString()
        val yemekMalzemeler = binding.yemekMalzemeText.text.toString()

        if (secilenBitmap != null){
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteDizisi = outputStream.toByteArray()

            try{
                context?.let {
                    val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id Integer Primary Key, yemekismi Varchar, yemekmalzemesi varchar, gorsel blob)")

                    val sqlString = "insert into yemekler (yemekismi, yemekmalzemesi,gorsel) Values (?, ?, ?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,yemekIsmi)
                    statement.bindString(2,yemekMalzemeler)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()


                }
            }catch (e:Exception){
                e.printStackTrace()
            }

            val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)
        }

    }

    fun gorselSec(view: View){

        activity?.let{
            if (ContextCompat.checkSelfPermission(it.applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                //izin verilmedi, izin istememiz gerekiyor

                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)

            }else{
                //izin zaten verilmiş, tekrar istemene gerek yok.

                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == 1){

            if (grantResults.size > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //izni aldık

                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            secilenGorsel = data.data

            try{
                context?.let {
                    if(secilenGorsel!=null  ){
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)

                        }else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }




    fun kucukBitmapOlustur(kullanicininSectigiBitmap : Bitmap, maximumBoyut : Int): Bitmap{

        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if(bitmapOrani > 1){
            //görsel yatay
            width = maximumBoyut
            val kisaltilmisHeight = width/bitmapOrani
            height = kisaltilmisHeight.toInt()
        }else{
            //görsel dikey
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()
        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}