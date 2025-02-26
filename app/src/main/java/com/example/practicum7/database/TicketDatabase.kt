package com.example.practicum7.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.practicum7.Ticket
import java.util.UUID

@Database (entities = [Ticket::class], version =1)
@TypeConverters(TicketTypeConverter::class)



abstract class TicketDatabase: RoomDatabase() {

    abstract fun ticketDAO(): TicketDAO



}