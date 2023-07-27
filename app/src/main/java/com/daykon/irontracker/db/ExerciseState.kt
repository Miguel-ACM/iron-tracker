package com.daykon.irontracker.db

data class ExerciseState(
    val exercises: List<Exercise> = emptyList(),
    val exerciseRecords: List<ExerciseRecord> = emptyList(),
    val muscleGroups: List<MuscleGroup> = emptyList(),
    val newExerciseName: String = "",
    val exerciseName: String = "",
    val exerciseColor: String = "",
    val maxReps: Float = 2f,
    val maxWeight: String = "",
    val maxWeightFloat: Float = 0f,
    val exerciseId: Int = 0,
    val searchTerm: String = "",
    val newExerciseMuscleGroupId: Int = 0,
    val isAddingExercise: Boolean = false,
    val isAddingExerciseRecord: Boolean = false,
    val isShowingDeleteDialog: Boolean = false

)
