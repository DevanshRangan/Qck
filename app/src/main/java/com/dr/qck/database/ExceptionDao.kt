package com.dr.qck.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExceptionDao {

    @Insert
    fun addToException(exceptionMessage: ExceptionMessage)

    @Delete
    fun removeException(exceptionMessage: ExceptionMessage)

    @Query("SELECT * FROM ExceptionMessage")
    fun getExceptionList(): List<ExceptionMessage>
}