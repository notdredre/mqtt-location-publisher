package com.example.assignment2.models;

import android.location.Location

public data class LocationModel(private var long : Double,
    private var lat : Double,
    private var velocity : Float) {

    companion object {
        fun toLocationModel(location: Location) : LocationModel {
            return LocationModel(location.longitude, location.latitude, location.speed)
        }
    }

    public fun getLong(): Double {
        return long
    }

    public fun getLat(): Double {
        return lat
    }

    public fun getVelocity() : Float {
        return velocity
    }
}
