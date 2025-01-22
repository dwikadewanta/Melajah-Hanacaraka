package com.example.capstoneproject.util

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    //endpoint
    @POST("/predict")
    fun sendDataMenulis(@Body data : SendDataRequest): Call<ApiResponse.getDataMenulisApiResponse>

    @POST("/predict-kuis")
    fun sendDataKuis(@Body data : SendDataKuis) : Call<ApiResponse.getDataKuisApiResponse>

    @GET("/trigger")
    fun triggerServer() : Call<Void>

}

data class SendDataRequest(
    val userID: String?,
    val penganggeSuara: String,
    val hanacarakaLabel: String,
    val imageFilename: String
)

data class SendDataKuis(
    val userID: String?,
    val penganggeSuara: List<String>,
    val hanacarakaLabel: List<String>,
    val imageFilename: List<String>
)