package com.sajda.app.service

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("v2/sholat/kota/semua")
    suspend fun getAllCities(): MyQuranCityResponse

    @GET("v2/sholat/jadwal/{cityId}/{year}/{month}/{day}")
    suspend fun getPrayerSchedule(
        @Path("cityId") cityId: String,
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Path("day") day: Int
    ): MyQuranScheduleResponse
}

data class MyQuranCityResponse(
    @SerializedName("data") val data: List<MyQuranCity> = emptyList()
)

data class MyQuranCity(
    @SerializedName("id") val id: String = "",
    @SerializedName("lokasi") val lokasi: String = ""
)

data class MyQuranScheduleResponse(
    @SerializedName("data") val data: MyQuranScheduleData? = null
)

data class MyQuranScheduleData(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("lokasi") val lokasi: String = "",
    @SerializedName("daerah") val daerah: String = "",
    @SerializedName("jadwal") val jadwal: MyQuranDailySchedule? = null
)

data class MyQuranDailySchedule(
    @SerializedName("tanggal") val tanggal: String = "",
    @SerializedName("subuh") val subuh: String = "",
    @SerializedName("dzuhur") val dzuhur: String = "",
    @SerializedName("ashar") val ashar: String = "",
    @SerializedName("maghrib") val maghrib: String = "",
    @SerializedName("isya") val isya: String = ""
)
