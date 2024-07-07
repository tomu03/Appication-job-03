package com.example.myapplication.activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.viewModel.FirestoreViewModel

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var firestoreViewModel: FirestoreViewModel
    private lateinit var btnZoomIn :ImageButton
    private lateinit var btnZoomOut :ImageButton
    private lateinit var btnFocusMarker: ImageButton
    private var markerPosition: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)

        btnZoomIn=findViewById(R.id.btnZoomIn)
        btnZoomOut=findViewById(R.id.btnZoomOut)
        btnFocusMarker = findViewById(R.id.btnFocusMarker)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        // Set up click listeners for zoom buttons
        btnZoomIn.setOnClickListener { zoomIn() }
        btnZoomOut.setOnClickListener { zoomOut() }
        btnFocusMarker.setOnClickListener { focusMarker() }
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map

        // Fetch user locations from Firestore and add markers
        firestoreViewModel.getAllUsers { userList ->
            for (user in userList) {
                val userLocation = user.location
                if (userLocation.isNotEmpty()) {
                    val latLng = parseLocation(userLocation)
                    val markerOptions = MarkerOptions().position(latLng).title(user.displayName)
                    mMap.addMarker(markerOptions)

                    // Assuming the first marker should be focused initially
                    if (markerPosition == null) {
                        markerPosition = latLng
                    }
                }
            }

            // Move camera to the marker position if available
            markerPosition?.let {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 13f))
            }
        }
    }

    private fun parseLocation(location: String): LatLng {
        val latLngSplit = location.split(", ")
        val latitude = latLngSplit[0].substringAfter("Lat: ").toDouble()
        val longitude = latLngSplit[1].substringAfter("Long: ").toDouble()
        return LatLng(latitude, longitude)
    }

    private fun zoomIn() {
        mMap.animateCamera(CameraUpdateFactory.zoomIn())
    }

    private fun zoomOut() {
        mMap.animateCamera(CameraUpdateFactory.zoomOut())
    }

    private fun focusMarker() {
        markerPosition?.let {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

}