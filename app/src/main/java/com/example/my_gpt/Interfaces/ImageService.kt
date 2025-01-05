package com.example.my_gpt.Interfaces

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class NewImageRequest(val inputs: String)
data class NewImageResponse(val url: String)


interface ImageService {

    @Headers(
        "x-rapidapi-key: 61########################################23a4",
        "x-rapidapi-host: ai-text-to-image-generator-api.p.rapidapi.com",
        "Content-Type: application/json"
    )
    @POST("realistic")
    fun generateImage(@Body request: NewImageRequest): Call<NewImageResponse>

}
