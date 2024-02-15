package edu.wcu.cs.thomas_kay.gpskotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import edu.wcu.cs.thomas_kay.gpskotlin.databinding.ActivityMapsBinding

const val DEMO:String = "trailDemo1.db"
const val WIDTH:Float = 15f
const val PADDING:Int = 115
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var databaseHelper: PathDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = PathDatabaseHelper(this, DEMO)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val list:ArrayList<LatLng> = databaseHelper.getCoordinates()
        if(list.size != 0) {
            val origin:LatLng = list[0]
            val destination:LatLng = list[list.size - 1]
            val path = PolylineOptions()
            path.addAll(list)
            path.width(WIDTH)
            path.color(ContextCompat.getColor(this, R.color.gps_color))
            path.geodesic(true)
            mMap.addPolyline(path)
            mMap.addMarker(MarkerOptions().position(origin).title("Starting Position"))
            mMap.addMarker(MarkerOptions().position(destination).title("Ending Position"))
            val bounds = LatLngBounds.builder()
                .include(origin)
                .include(destination)
                .build()
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, PADDING))
        } else {
            Toast.makeText(this, "Demo is not available for this device",
                Toast.LENGTH_LONG).show()
        }

        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}