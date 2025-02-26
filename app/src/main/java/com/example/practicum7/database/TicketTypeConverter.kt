package com.example.practicum7.database

import androidx.room.TypeConverter
import java.util.Date
import java.util.UUID

class TicketTypeConverter {

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }


    @TypeConverter
    fun toDate(millisSinceEpoch: Long): Date {
        return Date(millisSinceEpoch)
    }

    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String): UUID {
        return UUID.fromString(uuid)
    }}