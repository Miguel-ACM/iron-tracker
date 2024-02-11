package com.daykon.irontracker.viewModels.events

import com.daykon.irontracker.db.ExerciseRecord
import java.time.LocalDateTime

sealed interface GraphEvent {
  data class SetStartDate(val date: LocalDateTime) : GraphEvent
  data class SetEndDate(val date: LocalDateTime) : GraphEvent
  data class SetExerciseId(val exerciseId: Int) : GraphEvent
  data class SetSelectedPoint(val point: Int) : GraphEvent
  data class SetWeight(val weight: String) : GraphEvent
  data class SetReps(val reps: String) : GraphEvent
  data class SetBoxVisibility(val isVisible: Boolean) : GraphEvent
  data class DeleteRecord(val event: ExerciseRecord) : GraphEvent
  object UpdateRecord : GraphEvent


  //data class SetRecordEvent(val ):
}