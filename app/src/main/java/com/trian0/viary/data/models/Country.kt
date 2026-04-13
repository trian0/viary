package com.trian0.viary.data.models

data class Country(
    val currency: String = "USD",
    val countryCode: String = "US",
    val symbol: String = "$",
    val locale: String = "en-US",
    val loading: Boolean = true,
)