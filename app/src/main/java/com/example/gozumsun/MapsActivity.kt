package com.example.gozumsun

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.speech.tts.TextToSpeech
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_maps.*
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, TextToSpeech.OnInitListener {

    var TextToSpeechVar: TextToSpeech? = null
    private lateinit var mMap: GoogleMap
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener : LocationListener
    private lateinit var db: FirebaseFirestore


    var karsiyaGecmeSuresi = 10 // Saniye cinsinden minimum karşıya geçiş süresi.
    var yayaGecidiMesafe = 80 // Yaya geçidine kaç metre yaklaşınca veri çeksin.

    // Kullanıcının lokasyon değişkenleri.
    var userLocationX:Double = 0.0
    var userLocationY:Double = 0.0

    // Yaya geçidi lokasyonu.
    val latitude    =   39.876141
    val longitude   =   41.241202


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        TextToSpeechVar = TextToSpeech(this, this)

      val mapFragment = supportFragmentManager
        .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
       mMap = googleMap

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationChanged(location: Location) {

                if (location != null ) {

                    mMap.clear()

                    //userLocation değişkenine anlık konum değerleri yazılıyor.
                    val userLocation = LatLng(location.latitude,location.longitude) //anlık konum

                    //burada x ve y koordinatları ilgili değişkenlere atanıyor.
                    userLocationX = userLocation.latitude
                    userLocationY = userLocation.longitude
                    print("kullanici x")
                    println(userLocationX)

                    kullanicixdeger.text = userLocationX.toString()
                    kullaniciydeger.text = userLocationY.toString()
                    //anlık konum ve yaya geçidi konumu için haritaya marker ekleniyor.
                    //mMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                    mMap.addMarker(MarkerOptions().position(LatLng(latitude,longitude)).title("Yaya Geçidi"))
                    //kamera kullanıcının konumunun olduğu yere yakınlaşıyor.
                    if(userLocation != null){
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,17f))
                    }

                    // Yaya geçidine olan uzaklığı yuvarlamak için kullanıyoruz.
                    val df = DecimalFormat("#.##")
                    yayagecidimesafedeger.text = df.format(mesafe(userLocationX,userLocationY,latitude,longitude)).toString()
                    //Toast.makeText(applicationContext,mesafe(userLocationX,userLocationY,latitude,longitude).toString() , Toast.LENGTH_SHORT).show()

                    //eğer kullanıcının konumu ile yaya geçidi arasındaki mesafe yayaGecidiMesafe değerinden kısa ise veri tabanını
                    //dinlemeye başlayacak.
                    if (mesafe(userLocationX,userLocationY,latitude,longitude) < yayaGecidiMesafe){
                        veriCek()
                    }


                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }


        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),1)

        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener)
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false
            if ( lastLocation != null) {
               // val lastKnownLatLng = LatLng(lastLocation.latitude,lastLocation.longitude)
              //  mMap.addMarker(MarkerOptions().position(lastKnownLatLng).title("Your Location"))
               // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng,17f))

            }
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == 1) {
            if (grantResults.size > 0) {

                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener)
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = false

                }
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun veriCek(){
            //Firebase veritabanından ilgili veriler çekiliyor.
            db = FirebaseFirestore.getInstance()
            db.collection("Koordinatlar").addSnapshotListener { snapshot, e ->
                if (e != null) {
                    System.out.println("Hata var " + e)
                    return@addSnapshotListener
                }
                else{
                    if(snapshot != null){
                        if(!snapshot.isEmpty){
                            val documents = snapshot.documents
                            var ID:MutableList<String> = mutableListOf<String>()
                            var guvenliGecisSuresi:MutableList<String> = mutableListOf<String>()
                            var zaman:MutableList<String> = mutableListOf<String>()

                            for (document in documents){

                                ID.add(document.get("ID") as String)
                                guvenliGecisSuresi.add(document.get("guvenliGecisSuresi") as String)
                                zaman.add(document.get("zaman") as String)

                            }

                            var sonuc1 =  guvenliMi(ID[0],guvenliGecisSuresi[0],zaman[0])
                            gecissuresi1deger.text = sonuc1.toString()
                            var sonuc2 =  guvenliMi(ID[1],guvenliGecisSuresi[1],zaman[1])
                            gecissuresi2deger.text = sonuc1.toString()

                            if(sonuc1 > sonuc2){
                                if(sonuc2 > karsiyaGecmeSuresi){
                                    sesliOku("geçiş güvenli $sonuc2 saniye süre var");
                                }
                            }

                            if(sonuc2 > sonuc1){
                                if(sonuc1 > karsiyaGecmeSuresi){
                                    sesliOku("geçiş güvenli $sonuc1 saniye süre var");
                                }
                            }

                        }
                    }
                }
            }
    }



    //Veritabanından alınan tarih ile sistem tarihi kıyaslanır, güvenli geçiş süresi tekrar hesaplanır.
    @RequiresApi(Build.VERSION_CODES.O)
    fun guvenliMi(id: String, gecisSuresi: String, zaman: String):Int {
        var songecissuresi:Int = 0
        var zamanfarki:Int = 0
        //Yerel saati anlık olarak çekiyor.
        val londonZone = ZoneId.of("Europe/Istanbul")
        val londonCurrentDateTime = ZonedDateTime.now(londonZone)

        //Yerel tarih ve saati ilgili değişkenlere bölüyor.
        var yil = londonCurrentDateTime.year
        var ay  = londonCurrentDateTime.monthValue
        var gun = londonCurrentDateTime.dayOfMonth
        var saat = londonCurrentDateTime.hour
        var dakika = londonCurrentDateTime.minute
        var saniye = londonCurrentDateTime.second

        //Veritabanından gelen tarih ve saati uygun pattern'e göre ayrıştırıyor.
        val formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss", Locale.ENGLISH)
        val date = LocalDateTime.parse(zaman, formatter)

        //İlgili değişkenlere dağıtılıyor.
        var alinanYil = date.year
        var alinanAy = date.monthValue
        var alinanGun = date.dayOfMonth
        var alinanSaat = date.hour
        var alinanDakika = date.minute
        var alinanSaniye= date.second

        if(yil == alinanYil && ay== alinanAy && gun == alinanGun){
           var alinanToplamSaniye= (alinanSaat*60*60)+(alinanDakika*60) + alinanSaniye
           var gercekToplamSaniye= (saat*60*60)+(dakika*60) + saniye
            if(alinanToplamSaniye > gercekToplamSaniye){
                println("Sistem saatinde yanlışlık var.")
            }
            else{
                zamanfarki =  gercekToplamSaniye - alinanToplamSaniye
                songecissuresi = gecisSuresi.toInt() - zamanfarki
            }
        }
            return songecissuresi

    }


    //Fonksiyona gelen metni sesli olarak okur.
    fun sesliOku(metin: String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TextToSpeechVar!!.speak(metin,TextToSpeech.QUEUE_FLUSH,null,"")
        }
    }


    //Bu fonksiyon ile kullanıcının konumu ile yaya geçidi koordinatı arasındaki dikey mesafe hesaplanıyor.
    fun mesafe(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = sin(deg2rad(lat1)) * sin(deg2rad(lat2)) + cos(deg2rad(lat1)) * cos(deg2rad(lat2)) * cos(deg2rad(theta))
        dist = acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515
        dist = dist * 1.609344
        return dist*1000
    }


    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }


    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }


    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS){
            TextToSpeechVar!!.setLanguage(Locale("TR"))
        }

    }


    override fun onDestroy() {
        if(TextToSpeechVar!=null){
            TextToSpeechVar!!.stop()
            TextToSpeechVar!!.shutdown()
        }
        super.onDestroy()
    }

}
