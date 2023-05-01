@file:Suppress("DEPRECATION")

package com.example.rutacasa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*


class MainActivity : AppCompatActivity() {
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var btnVerRuta: Button
    private var map: MapView? = null
    private var line = Polyline()
    private var start: String = ""
    private var end: String = ""
    private val apiService: ApiService by lazy {
        Directions.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        setContentView(R.layout.activity_main)

        //
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        //
        //Map Provider
        map = findViewById<View>(R.id.map) as MapView
        map!!.setTileSource(TileSourceFactory.MAPNIK)


        //MapController
        val mapController = map!!.controller
        mapController.setZoom(14)
        val startPoint =GeoPoint(20.208786, -101.136080)
        mapController.setCenter(startPoint)

        //Add markers
        val markerStart = Marker(map)
        markerStart.isDraggable = true
        markerStart.position = GeoPoint(20.208786, -101.136080)
        markerStart.title = "CASA"
        map!!.overlays.add(markerStart)

        val markerEnd = Marker(map)
        markerEnd.isDraggable = true
        markerEnd.position = GeoPoint(20.139398208378335, -101.15073143396242)
        markerEnd.title = "DESTINO"
        map?.overlays?.add(markerEnd)


        btnVerRuta = findViewById(R.id.btnCalcularRuta)
        var aux = false

        btnVerRuta.setOnClickListener{
            fecthLocation()
            if (!aux) {
                line = Polyline()
                drawRoute(markerStart.position, markerEnd.position)
                btnVerRuta.text = "Limpiar"
                aux=true
            } else {
                btnVerRuta.text = "Marcar Camino"
                map?.overlays?.remove(line)
                aux =false
            }
        }

        map?.invalidate()
    }

    private fun fecthLocation() {
        val task= fusedLocationProviderClient.lastLocation
       if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=
               PackageManager.PERMISSION_GRANTED &&
           ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
               PackageManager.PERMISSION_GRANTED){
           ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)
           return
       }
        task.addOnSuccessListener {
            if(it!=null){
                Toast.makeText(applicationContext, "${it.latitude} ${it.longitude}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //DATOS DEL RETROFIT
    private fun drawRoute(startPoint: GeoPoint, endPoint: GeoPoint){
        CoroutineScope(Dispatchers.IO).launch {
            end = "${endPoint.longitude},${endPoint.latitude}"
            start = "${startPoint.longitude},${startPoint.latitude}"

            val points = apiService.getRoute("5b3ce3597851110001cf62488d38aa048bea4519ae3177df424c06de", start, end)
            val features = points.features

            for (feature in features) {
                val geometry = feature.geometry
                val coordinates = geometry.coordinates
                for (coordinate in coordinates) {
                    val point = GeoPoint(coordinate[1], coordinate[0])
                    line.addPoint(point)
                }
                map?.overlays?.add(line)
            }
        }
    }

}