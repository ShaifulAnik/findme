package com.example.findme.ui.map

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.findme.R
import com.example.findme.data.model.AppUser
import com.example.findme.databinding.ActivityGoogleMapBinding
import com.example.findme.util.Resource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth

class GoogleMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGoogleMapBinding
    private lateinit var mMap: GoogleMap
    private val viewModel: MapViewModel by viewModels()

    private var targetLat: Double = 0.0
    private var targetLng: Double = 0.0
    private var targetName: String? = null
    private var showSingleUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        targetLat = intent.getDoubleExtra("LATITUDE", 0.0)
        targetLng = intent.getDoubleExtra("LONGITUDE", 0.0)
        targetName = intent.getStringExtra("USER_NAME")
        showSingleUser = intent.getBooleanExtra("SHOW_SINGLE_USER", false)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (showSingleUser && targetLat != 0.0 && targetLng != 0.0) {
            // শুধুমাত্র ১ জন ইউজারের লোকেশন দেখাবে (নিজের কালার বা অন্যের)
            mMap.clear()
            val point = LatLng(targetLat, targetLng)
            val markerName = targetName ?: "User Location"

            // ১ জন দেখালে যদি সে নিজে হয় তবে ব্লু মার্কার দেবে
            val isMe = markerName.contains("(You)")
            val markerHue = if (isMe) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_RED

            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(point)
                    .title(markerName)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerHue))
            )
            marker?.showInfoWindow()
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15f))
        } else {
            // সব ইউজারের লোকেশন একসাথে দেখাবে
            setupObservers()
            viewModel.loadAllUsers()
        }
    }

    private fun setupObservers() {
        viewModel.mapUsersState.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> showAllUsersOnMap(resource.data)
                is Resource.Error -> Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    private fun showAllUsersOnMap(users: List<AppUser>) {
        mMap.clear()
        if (users.isEmpty()) return

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val builder = LatLngBounds.Builder()
        var hasValidPoint = false

        for (user in users) {
            val point = LatLng(user.latitude, user.longitude)
            val isCurrent = (user.userId == currentUserId)

            val name = user.displayName ?: if (user.userEmail.contains("@")) user.userEmail.substringBefore("@") else "User"
            val title = if (isCurrent) "$name (You)" else name

            // ৩. নিজের মার্কার BLUE এবং অন্যদের মার্কার RED হবে
            val markerColor = if (isCurrent) {
                BitmapDescriptorFactory.HUE_AZURE // Blue Marker
            } else {
                BitmapDescriptorFactory.HUE_RED   // Red Marker
            }

            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(point)
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            )

            if (isCurrent) {
                marker?.showInfoWindow() // নিজের নামের ইনফো অটো দেখা যাবে
            }

            builder.include(point)
            hasValidPoint = true
        }

        if (hasValidPoint) {
            try {
                val bounds = builder.build()
                val padding = 120
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                mMap.animateCamera(cu)
            } catch (e: Exception) {
                val firstUser = users.first()
                val firstLocation = LatLng(firstUser.latitude, firstUser.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12f))
            }
        }
    }
}