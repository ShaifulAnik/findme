package com.example.findme.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationRepository(context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): Location? {
        return try {
            // ১. প্রথমে লাস্ট জানা লোকেশন চেক করা
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                lastLocation
            } else {
                // ২. ক্যাশে না থাকলে সাথে সাথে বর্তমান লোকেশন ফেচ করা
                val priority = Priority.PRIORITY_HIGH_ACCURACY
                fusedLocationClient.getCurrentLocation(priority, CancellationTokenSource().token).await()
            }
        } catch (e: Exception) {
            null
        }
    }
}