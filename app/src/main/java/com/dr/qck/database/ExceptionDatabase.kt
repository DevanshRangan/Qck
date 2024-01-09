package com.dr.qck.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExceptionMessage::class], version = 1)
abstract class ExceptionDatabase: RoomDatabase() {
    abstract fun exceptionDao(): ExceptionDao
}