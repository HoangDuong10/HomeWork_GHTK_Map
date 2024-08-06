package com.example.homework_ghtk_map

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.homework_ghtk_map.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions[ACCESS_FINE_LOCATION] == true
            if (allPermissionsGranted) {
                getCurrentLocation()
                // Quyền đã được cấp, bạn có thể thực hiện các hành động yêu cầu quyền truy cập vị trí
                showToast("Location permission granted.")
            } else {
                // Quyền chưa được cấp
                showToast("Location permission is not granted.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
       checkAndRequestLocationPermission()
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
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val currentLocation = LatLng(latitude, longitude)
                    val specificLocation = LatLng(21.014007826514074, 105.78438394043823)
                    addMarkersAndDrawLine(currentLocation, specificLocation)
                } else {
                    showToast("Location not available")
                }
            }
        } else {
            showToast("Location permission is not granted.")
        }
    }



    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
        return results[0].toDouble()
    }
    private fun addMarkersAndDrawLine(location1: LatLng, location2: LatLng) {
        // Thêm marker cho vị trí hiện tại
        mMap.addMarker(MarkerOptions().position(location1).title("Current Location"))

        // Thêm marker cho vị trí cụ thể
        mMap.addMarker(MarkerOptions().position(location2).title("GHTK"))

        // Vẽ đường nối giữa hai marker
        val polylineOptions = PolylineOptions()
            .add(location1)
            .add(location2)
            .color(android.graphics.Color.RED)
            .width(20f)
            .clickable(true)
        val polyline = mMap.addPolyline(polylineOptions)

        // Đặt OnPolylineClickListener
        mMap.setOnPolylineClickListener { clickedPolyline ->
            if (clickedPolyline == polyline) {
                val distance = calculateDistance(location1, location2)
                showToast("Distance between 2 locations: ${distance / 1000} km")
            }
        }

        // Tạo LatLngBounds để chứa cả hai điểm
        val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
            .include(location1)
            .include(location2)
            .build()

        // Di chuyển camera đến khu vực chứa cả hai điểm
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }


    private fun checkAndRequestLocationPermission() {
        val locationPermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu quyền nếu chưa được cấp
            requestPermissionsLauncher.launch(arrayOf(ACCESS_FINE_LOCATION))
        } else {
            // Quyền đã được cấp, thực hiện hành động cần thiết
            showToast("Location permission is already granted.")
            getCurrentLocation()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}