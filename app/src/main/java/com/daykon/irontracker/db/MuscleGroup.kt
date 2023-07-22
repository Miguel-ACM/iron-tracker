package com.daykon.irontracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MuscleGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: Int,
    val extraSearch: String,
    val orderIndex: Float
)
