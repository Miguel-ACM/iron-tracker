package com.daykon.irontracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime


@Entity(foreignKeys = [ForeignKey(entity = Exercise::class,
    parentColumns = ["id"],
    childColumns = ["exerciseId"],
    onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["exerciseId"])])
data class ExerciseRecord(
    val exerciseId: Int,
    val weight: Float,
    val reps: Int,
    val date: LocalDateTime,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)
