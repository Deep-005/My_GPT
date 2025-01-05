package com.example.my_gpt.Interfaces

import com.example.my_gpt.DataClass.SummaryResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @GET("summarize")
    fun getSummary(
        @Query("url") url: String,
        @Query("lang") lang: String = "en",
        @Query("engine") engine: Int = 2,
        @Header("x-rapidapi-key") apiKey: String,
        @Header("x-rapidapi-host") apiHost: String = "article-extractor-and-summarizer.p.rapidapi.com"
    ): Call<SummaryResponse>
}
