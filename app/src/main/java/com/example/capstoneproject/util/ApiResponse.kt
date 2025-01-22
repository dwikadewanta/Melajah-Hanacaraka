package com.example.capstoneproject.util

import com.google.gson.annotations.SerializedName

class ApiResponse{
    data class getDataMenulisApiResponse(
        @SerializedName("result") val result : Boolean,
        @SerializedName("probability") val probabilty : Float
    )

    data class getDataKuisApiResponse(
        @SerializedName("result") val result : List<Boolean>
    )

}
