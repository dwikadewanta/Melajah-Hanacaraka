package com.example.capstoneproject.util

interface DataCallback {
    fun onDataReceived(apiResult: Boolean, apiProbability: Float)
    fun onError(apiMessage: String)
}

interface DataCallback2{
    fun onDataReceived(apiResult : List<Boolean>)
    fun onError(apiMessage : String)
}