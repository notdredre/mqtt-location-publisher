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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.assignment2.models.LocationModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.TimeUnit
import com.google.gson.Gson
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    private var mqttClient : Mqtt5AsyncClient? = null
    private var clientID : String = ""
    private var studentID : Int = 0
    private var brokerAddress : String = "broker-816036749.sundaebytestt.com"
    private val topic : String = "notthatguy"
    private var publishing : Boolean = false
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

        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mqttClient?.disconnect()
                fusedLocationProvider.removeLocationUpdates(locationCallback)
                finish()
            }
        })

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 100).setMaxUpdateDelayMillis(120).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                updateUI()
                super.onLocationResult(p0)
                val currentLocation = p0.lastLocation?.let{LocationModel.toLocationModel(it, studentID)}
                Log.e("Location", currentLocation.toString())
                if (currentLocation != null) {
                    sendToBroker(currentLocation)
                    val statusText : TextView = findViewById(R.id.status)
                    val current : String = statusText.text.toString()
                    val locationText = current + "Last location sent was [Latitude:${currentLocation.getLat()}, Longitude: ${currentLocation.getLong()}]\n"
                    statusText.text = locationText
                }
            }
        }

        hasPermissions = checkPermissions()

        updateUI()
    }

    private fun updateUI() {
        val publishLayout : ConstraintLayout = findViewById(R.id.publish)
        val permissionLayout : ConstraintLayout = findViewById(R.id.permissions)
        val statusText : TextView = findViewById(R.id.status)

        publishLayout.visibility = if (hasPermissions) View.VISIBLE else View.GONE
        permissionLayout.visibility = if (!hasPermissions) View.VISIBLE else View.GONE
        statusText.text = when {
            publishing -> "Connected to broker at $brokerAddress and publishing location on topic $topic\n"
            else -> "Not connected to broker."
        }
    }

    private fun checkPermissions() : Boolean {
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
        val toast : Toast = Toast.makeText(this, "Please enter a valid student ID", Toast.LENGTH_LONG)
        if (studentIDText.length != 9) {
            toast.show()
            return
        }
        studentID = studentIDText.toInt()
        if (studentID !in 816000000..816999999) {
            toast.show()
            return
        }

        clientID = studentIDText
        mqttClient = Mqtt5Client.builder()
            .identifier(clientID)
            .serverHost("broker-816036749.sundaebytestt.com")
            .serverPort(1883)
            .buildAsync()

        try {
            mqttClient?.connect()
            publishing = true
            Log.i("MQTT", "Connected")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MQTT", "Could not connect to MQTT broker")
        }

        fusedLocationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        updateUI()
    }

    fun disconnect(view: View) {
        try {
            mqttClient?.disconnect()
            fusedLocationProvider.removeLocationUpdates(locationCallback)
            publishing = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateUI()
    }

    fun sendToBroker(content: Any) {
        try {
            val toSend: String = Gson().toJson(content)
            mqttClient?.publishWith()?.topic(topic)?.payload(toSend.toByteArray())?.send()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MQTT", "Something went wrong :(")
        }
    }
}