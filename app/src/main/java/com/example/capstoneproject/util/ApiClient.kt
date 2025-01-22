package com.example.capstoneproject.util

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ApiClient {
    private val BASE_URL = "https://flask-prediction-app-k3u6xopzkq-as.a.run.app"
    private val apiService : ApiService

    init{
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    fun sendDataMenulis(userID : String?, penganggeSuara : String, hanacarakaLabel : String, imageFilename : String, callback: DataCallback){
        val data = SendDataRequest(userID, penganggeSuara, hanacarakaLabel, imageFilename)
        val call = apiService.sendDataMenulis(data)
        call.enqueue(object : Callback<ApiResponse.getDataMenulisApiResponse> {
            override fun onResponse(call: Call<ApiResponse.getDataMenulisApiResponse>, response: Response<ApiResponse.getDataMenulisApiResponse>) {
                if(response.isSuccessful){
                    val apiResponse = response.body()
                    if(apiResponse != null){
                        callback.onDataReceived(apiResponse.result, apiResponse.probabilty)
                    }else{
                        callback.onError("Data is null")
                    }
                }else{
                   callback.onError("Failed to get message")
                }
            }

            override fun onFailure(call: Call<ApiResponse.getDataMenulisApiResponse>, t: Throwable) {
                callback.onError("Unknown error: ${t.message}")
            }
        })
    }

    fun sendDataKuis(userID : String?, penganggeSuara: List<String>, hanacarakaLabel: List<String>, imageFilename: List<String>, callback : DataCallback2){
        val data = SendDataKuis(userID, penganggeSuara, hanacarakaLabel, imageFilename)
        val call = apiService.sendDataKuis(data)
        call.enqueue(object : Callback<ApiResponse.getDataKuisApiResponse>{
            override fun onResponse(
                call: Call<ApiResponse.getDataKuisApiResponse>,
                response: Response<ApiResponse.getDataKuisApiResponse>
            ) {
                if(response.isSuccessful){
                    val apiResponse = response.body()
                    if(apiResponse != null){
                        callback.onDataReceived(apiResponse.result)
                    }else{
                        callback.onError("Data is null")
                    }
                }else{
                    callback.onError("Failed to get message")
                }
            }

            override fun onFailure(call: Call<ApiResponse.getDataKuisApiResponse>, t: Throwable) {
                callback.onError("Unknown error: ${t.message}")
            }
        })
    }

    fun triggerServer(){
        val call = apiService.triggerServer()
        call.enqueue(object : Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful){
                    println("Server triggered sucessfully")
                }else{
                    println("Failed to trigger server: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                println("Error triggered server")
            }
        })

    }
}