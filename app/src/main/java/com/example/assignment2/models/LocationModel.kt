package com.example.assignment2.models;

import android.location.Location

public data class LocationModel(private var studentID: Int, private var latitude : Double,
                                private var longitude : Double,
                                private var velocity : Float) {


    companion object {
        var id = 0
        fun toLocationModel(location: Location, studentID: Int) : LocationModel {
            return LocationModel(studentID, location.latitude, location.longitude, location.speed)
        }
    }

    init {
        id++
    }

    fun getID() : Int {
        return id
    }

    public fun getLong(): Double {
        return longitude
    }

    public fun getLat(): Double {
        return latitude
    }

    public fun getVelocity() : Float {
        return velocity
    }
}
