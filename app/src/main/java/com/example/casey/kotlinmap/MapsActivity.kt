package com.example.casey.kotlinmap

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_maps.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val REQUEST_LOCATION_PERMISSIONS = 0x1

    private lateinit var googleMap: GoogleMap

    private val locationRequest by lazy {
        LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    // Different way of implementing an interface for callbacks
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        sampleTextView.text = "Look mom, no findViewById!"

        // Gets the google map instance from the map fragment
        (map as SupportMapFragment).getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            when {
                // Permission granted.
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> startMapLocationUpdates()

                // Permission denied.
                else -> {
                    System.console()?.printf("Denied!")
                }
            }
        }
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
    // This is the old school way of implementing a delegate callback.
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        if (checkPermissions()) {
            startMapLocationUpdates()
        }

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED


    @SuppressLint("MissingPermission")
    private fun startMapLocationUpdates() {
        googleMap.isMyLocationEnabled = true

        locationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude,location.longitude)))
                    }
                }
                .addOnFailureListener {
                    System.console()?.printf("failed $it")
                }

        locationClient.requestLocationUpdates(locationRequest,locationCallback,null)
    }

}
