package com.hllbr.kotlintravelbook

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var placesArray = ArrayList<Place>()
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_place,menu)
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add_place_option){
            val intent = Intent(applicationContext,MapsActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try{
            val database = openOrCreateDatabase("Places", Context.MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM places",null)//seçim sırasında bir filtreleme yapmak istersek burada ekleyebiliyorduk
            val addressIndex = cursor.getColumnIndex("address")
            val latitudeIndex = cursor.getColumnIndex("latitude")
            val longitudeIndex = cursor.getColumnIndex("longitude")
            while (cursor.moveToNext()){
                val addressFromDatabase = cursor.getString(addressIndex)
                val latitudeFromDatabase = cursor.getDouble(latitudeIndex)
                val longitudeFromDatabase = cursor.getDouble(longitudeIndex)

                val myPlace = Place(addressFromDatabase,latitudeFromDatabase,longitudeFromDatabase)
                println(myPlace.address)
                Toast.makeText(this,myPlace.address,Toast.LENGTH_LONG).show()

                placesArray.add(myPlace)
            }
            cursor.close()
        }catch (e : Exception){
            e.printStackTrace()
            Toast.makeText(this,e.localizedMessage.toString(),Toast.LENGTH_LONG).show()
        }
        val customAdapter = CustomAdapter(placesArray,this)
        listView.adapter = customAdapter
        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this@MainActivity,MapsActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("selectedPlace",placesArray.get(position))
            startActivity(intent)
        }
    }
}