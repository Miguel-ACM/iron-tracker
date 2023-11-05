package com.daykon.irontracker.viewModels.events

import java.time.LocalDateTime

sealed interface GraphEvent {
    data class SetStartDate(val date: LocalDateTime): GraphEvent
    data class SetEndDate(val date: LocalDateTime): GraphEvent
    data class SetExerciseId(val exerciseId: Int): GraphEvent
    data class SetSelectedPoint(val point: Int): GraphEvent


    //data class SetRecordEvent(val ):
}