package com.example.practicum7.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.practicum7.Ticket
import java.util.UUID

@Database (entities = [Ticket::class], version =1)
@TypeConverters(TicketTypeConverter::class)



abstract class TicketDatabase: RoomDatabase() {

    abstract fun ticketDAO(): TicketDAO



}

val migration_1_2 = object : Migration(1,2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Ticket ADD COLUMN assignee TEXT NOT NULL DEFAULT ''"
        )
    }
}