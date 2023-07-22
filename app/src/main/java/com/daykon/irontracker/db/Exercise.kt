package com.daykon.irontracker.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Relation


@Entity(foreignKeys = [ForeignKey(entity = MuscleGroup::class,
    parentColumns = ["id"],
    childColumns = ["muscleGroupId"],
    onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["muscleGroupId"])])
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val muscleGroupId: Int,

)

data class ExerciseWithMuscleGroup(
    @Embedded
    val exercise: Exercise,
    @Relation(
        parentColumn = "muscleGroupId",
        entityColumn = "id"
    )
    val muscleGroup: MuscleGroup,


)


