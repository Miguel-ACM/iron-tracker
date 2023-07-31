package com.daykon.irontracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime


@Entity
data class ProgressPic(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: LocalDateTime,
    val path: String,
    val weight: Float? = null
)
