package com.hllbr.kotlintravelbook

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.hllbr.kotlintravelbook.databinding.ActivityMapsBinding
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener : LocationListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intentToMain = Intent(this,MainActivity::class.java)
        startActivity(intentToMain)
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(myListener)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                val sharedPreferences = this@MapsActivity.getSharedPreferences("com.hllbr.kotlintravelbook",Context.MODE_PRIVATE)
                val firstTimeCheck = sharedPreferences.getBoolean("notFirstTime",false)
                if(!firstTimeCheck){
                    mMap.clear()
                    val newUserLocation = LatLng(location.latitude,location.longitude)
                    mMap.addMarker(MarkerOptions().position(newUserLocation))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newUserLocation,12f))
                    sharedPreferences.edit().putBoolean("notFirstTime",true).apply()
                }

            }
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }else{
          locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)
            val intent = intent
            val info = intent.getStringExtra("info")

            if (info.equals("new")){
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(lastLocation != null){
                    val lastLocationLatLng = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.addMarker(MarkerOptions().position(lastLocationLatLng).title("User Location"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatLng,15f))
            }else{
                    mMap.clear()
                    val selectedPlace = intent.getSerializableExtra("selectedPlace") as Place
                    val selectedLocation = LatLng(selectedPlace.latitude!!,selectedPlace.longitude!!)
                    mMap.addMarker(MarkerOptions().title(selectedPlace.address).position(selectedLocation))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation,18f))
                }

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1 && grantResults.isNotEmpty()){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    var address = ""
    val myListener = object : GoogleMap.OnMapLongClickListener{
        override fun onMapLongClick(p0: LatLng) {
            val geocoder =  Geocoder(this@MapsActivity, Locale.getDefault())
            try {


                if (p0 != null) {
                    val addressList = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                    if (addressList != null && addressList.size > 0) {
                        if (addressList[0].thoroughfare != null) {
                            address += addressList[0].thoroughfare
                            if (addressList[0].subThoroughfare != null) {
                                address += addressList[0].subThoroughfare
                            }
                        }
                    } else {
                        address = "New Palace"
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
                Toast.makeText(this@MapsActivity,e.localizedMessage.toString(),Toast.LENGTH_LONG).show()
            }
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(p0).title(address))
            val newPlace = Place(address,p0.latitude,p0.longitude)
            val dialog = AlertDialog.Builder(this@MapsActivity)
            dialog.setCancelable(false)//Seçimlerden birini yapmak zorunda seçim yapmama gibi bir şansı yok
            dialog.setTitle("Are You Sure ?¿")
            dialog.setMessage(newPlace.address)
            dialog.setPositiveButton("Yes") {dialog,which->
                //SQLite Save / Şuan SQLite içerisinde ben enlem boylam ve address verilerini kaydetmek istiyorum oluşturulan zmananıda kaydedebilirim vb....

                try{
                    val database = openOrCreateDatabase("Places",Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS places(address VARCHAR,latitude Double,longitude Double)")
                    /*
                    Değerleri koymak için insert into kullanarak yapıyoruz fakat değerleri başka biryerden aldığımızda SQLiteStament ifade ile yapıyoruz işlemlerimizi

                     */
                    val toCompile = "INSERT INTO places (address,latitude,longitude) VALUES (?,?,?)"
                    val sqliteStatement = database.compileStatement(toCompile)
                    sqliteStatement.bindString(1,newPlace.address)
                    sqliteStatement.bindDouble(2,newPlace.latitude!!)
                    sqliteStatement.bindDouble(3,newPlace.longitude!!)
                    sqliteStatement.execute()

                }catch (e : Exception){
                    e.printStackTrace()
                    Toast.makeText(this@MapsActivity,e.localizedMessage.toString(),Toast.LENGTH_LONG).show()
                }
                Toast.makeText(this@MapsActivity,"New Place Created!",Toast.LENGTH_LONG).show()
            }.setNegativeButton("No") {dialog,which->
                Toast.makeText(this@MapsActivity,"Canceled!",Toast.LENGTH_LONG).show()
            }
            dialog.show()
        }

    }
}