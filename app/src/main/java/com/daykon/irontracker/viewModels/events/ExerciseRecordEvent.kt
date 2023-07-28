package com.daykon.irontracker.viewModels.events

import com.daykon.irontracker.db.Exercise
import com.daykon.irontracker.db.ExerciseRecord

sealed interface ExerciseRecordEvent {
    object SaveExerciseRecord: ExerciseRecordEvent
    object SaveExercise: ExerciseRecordEvent
    data class SetNewExerciseName(val newExerciseName: String): ExerciseRecordEvent
    data class SetExerciseId(val id: Int): ExerciseRecordEvent
    data class SetReps(val reps: Float): ExerciseRecordEvent
    data class SetMaxWeight(val maxWeight: String): ExerciseRecordEvent
    data class ShowExerciseRecordDialog(val exercise: Exercise, val exerciseRecord: ExerciseRecord):
        ExerciseRecordEvent
    data class ShowDeleteDialog(val exerciseId: Int): ExerciseRecordEvent
    object HideDeleteDialog: ExerciseRecordEvent
    data class DeleteExercise(val exercise: Exercise): ExerciseRecordEvent
    data class SetNewExerciseMuscleGroupId(val muscleGroupId: Int): ExerciseRecordEvent
    object HideExerciseRecordDialog: ExerciseRecordEvent
    object ShowExerciseDialog: ExerciseRecordEvent
    object HideExerciseDialog: ExerciseRecordEvent
    data class UpdateSearch(val term: String): ExerciseRecordEvent

    //data class SetRecordEvent(val ):
}