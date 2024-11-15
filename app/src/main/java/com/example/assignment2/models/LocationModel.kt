package com.example.assignment2.models;

import android.location.Location
import kotlin.properties.Delegates

public data class LocationModel(private var studentID: Int, private var latitude : Double,
                                private var longitude : Double,
                                private var velocity : Float) {

    private val id : Int get() = hashCode()

    companion object {
        fun toLocationModel(location: Location, studentID: Int) : LocationModel {
            return LocationModel(studentID, location.latitude, location.longitude, location.speed)
        }
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
