package com.example.wheelgame.services

import com.example.wheelgame.models.Entry
import retrofit2.http.GET

interface WheelEntryService {
    @GET("/bin/539dc092-8367-414a-8892-ed3b2d666dbe/")
    suspend fun getData(): List<Entry>
}