package com.daykon.irontracker.db

import java.time.LocalDateTime

data class GraphState(
    val exerciseRecords: List<ExerciseRecord> = emptyList(),
    val exerciseId: Int = 1,
    val exercise: ExerciseWithMuscleGroup = ExerciseWithMuscleGroup(Exercise(1,"", 0),
        MuscleGroup(0, "", 0, extraSearch = "", orderIndex = 0f)),
            //ExerciseWithMuscleGroup = ,
    val startDateTime: LocalDateTime = LocalDateTime.now().minusYears(10),
    val endDateTime: LocalDateTime = LocalDateTime.now().plusYears(1),
    val muscleGroupId: Int = 0
)
