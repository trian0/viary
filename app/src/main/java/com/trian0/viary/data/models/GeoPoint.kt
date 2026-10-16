package com.trian0.viary.data.models

data class GeoPoint(val latitude: Double, val longitude: Double) {
    val isSet: Boolean get() = latitude != 0.0 || longitude != 0.0
}
