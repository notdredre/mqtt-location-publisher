package com.example.assignment2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.concurrent.TimeUnit
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private var mqttClient : Mqtt5BlockingClient? = null
    private var clientID : String = ""
    private val topic : String = "assignment/location"
    private var hasPermissions : Boolean = false
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            results : Map<String, Boolean> ->
        hasPermissions = true
        for (result in results.keys) {
            if (results[result] == false)
                hasPermissions = false
        updateUI()
        }
    }
    private lateinit var fusedLocationProvider : FusedLocationProviderClient
    private lateinit var locationRequest : LocationRequest
    private lateinit var  locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(5)
            fastestInterval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(10)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                var currentLocation = p0.lastLocation
                Log.e("Location", currentLocation.toString())
                if (currentLocation != null) {
                    sendToBroker(currentLocation)
                }
            }
        }
        hasPermissions = checkPermissions()

        updateUI()
    }

    private fun updateUI() {
        val publishLayout : ConstraintLayout = findViewById(R.id.publish)
        val permissionLayout : ConstraintLayout = findViewById(R.id.permissions)

        publishLayout.visibility = if (hasPermissions) View.VISIBLE else View.GONE
        permissionLayout.visibility = if (!hasPermissions) View.VISIBLE else View.GONE
    }

    fun checkPermissions() : Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun getPermissions(view: View) {
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
    }

    @SuppressLint("MissingPermission")
    fun startPublishing(view: View) {
        val studentIDField : EditText = findViewById(R.id.studentIDET)
        val studentIDText = studentIDField.text.toString()
        if (studentIDText.length != 9) {
            // Toast
            return
        }
        if (studentIDText.toInt() !in 816000000..816999999) {
            return
        }

        clientID = studentIDText
        mqttClient = Mqtt5Client.builder()
            .identifier(clientID)
            .serverHost("broker-816036749.sundaebytestt.com")
            .serverPort(1883)
            .buildBlocking()
        fusedLocationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        try {
            mqttClient?.connect()
            Log.i("MQTT", "Connected")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MQTT", "Could not connect to MQTT broker")
        }
    }

    fun disconnect(view: View) {
        try {
            mqttClient?.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendToBroker(location : Location) {
        try {
            Log.i("SEND", "Trying to send")
            val payload: String = Gson().toJson(location)
            mqttClient?.publishWith()?.topic("hello")?.payload(payload.toByteArray())?.send()
        } catch (e: Exception) {
            Log.e("MQTT", "Something went wrong :(")
        }
    }
}