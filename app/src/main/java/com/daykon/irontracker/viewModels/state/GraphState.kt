package com.daykon.irontracker.viewModels.state

import com.daykon.irontracker.db.Exercise
import com.daykon.irontracker.db.ExerciseRecord
import com.daykon.irontracker.db.ExerciseWithMuscleGroup
import com.daykon.irontracker.db.MuscleGroup
import java.time.LocalDateTime

data class GraphState(
    val exerciseRecords: List<ExerciseRecord> = emptyList(),
    val exerciseId: Int = 1,
    val exercise: ExerciseWithMuscleGroup = ExerciseWithMuscleGroup(
        Exercise(1, "", 0),
        MuscleGroup(0, "", 0, extraSearch = "", orderIndex = 0f)
    ),
    val selectedPoint: Int = -1,
    val selectedPointWeight: String = "",
    val selectedPointReps: String = "",
    //ExerciseWithMuscleGroup = ,
    val startDateTime: LocalDateTime = LocalDateTime.now().minusYears(10),
    val endDateTime: LocalDateTime = LocalDateTime.now().plusYears(1),
    val muscleGroupId: Int = 0,
    val isBoxVisible: Boolean = false
)
