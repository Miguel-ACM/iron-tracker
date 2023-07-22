package com.daykon.irontracker.db

import java.time.LocalDateTime

data class GraphState(
    val exerciseRecords: List<ExerciseRecord> = emptyList(),
    val exercise: ExerciseWithMuscleGroup = ExerciseWithMuscleGroup(Exercise(0,"", 0),
        MuscleGroup(0, "", 0, extraSearch = "", orderIndex = 0f)),
    val startDateTime: LocalDateTime = LocalDateTime.now().minusYears(10),
    val endDateTime: LocalDateTime = LocalDateTime.now().plusYears(1),
    val muscleGroupId: Int = 0
)
