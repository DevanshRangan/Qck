package com.dr.qck.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExceptionMessage(
    @PrimaryKey val id: Long, val senderName: String
)