package com.example.assignment2.models;

import android.location.Location

data class LocationModel(private var studentID: Int, private var latitude : Double,
                                private var longitude : Double,
                                private var velocity : Float,
                                private var timestamp: Long) {



    companion object {
        var id = 0
        fun toLocationModel(location: Location, studentID: Int) : LocationModel {
            return LocationModel(studentID, location.latitude, location.longitude, location.speed, System.currentTimeMillis())
        }
    }

    init {
        id++
    }

    fun getID() : Int {
        return id
    }

    fun getStudentID(): Int {
        return studentID
    }

    fun getLong(): Double {
        return longitude
    }

    fun getLat(): Double {
        return latitude
    }

    fun getVelocity() : Float {
        return velocity
    }

    fun getTimestamp() : Long {
        return timestamp
    }

    fun setTimestamp(value: Long) {
        timestamp = value
    }

    override fun toString(): String {
        return "StudentID: $studentID " +
                "Latitude: $latitude " +
                "Longitude: $longitude " +
                "Velocity: $velocity " +
                "Timestamp: $timestamp"
    }
}
