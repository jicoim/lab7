package com.example.practicum7

import android.app.Application

class TicketApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        TicketRepository.initialize(this)
    }
}