package com.daykon.irontracker.db

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime



@OptIn(ExperimentalCoroutinesApi::class)
class GraphViewModel (
    exerciseDao: ExerciseDao,
    private val exerciseRecordDao: ExerciseRecordDao,
    exerciseId: Int
    ): ViewModel() {
    private val _state = MutableStateFlow(GraphState())

    private var startDateTime: LocalDateTime = LocalDateTime.now().minusYears(10)
    private var endDateTime: LocalDateTime = LocalDateTime.now().plusYears(1)

    private val _exerciseRecords = exerciseRecordDao.getAllExerciseRecordsForExercise(exerciseId)
    private val _exerciseWithMuscleGroup = exerciseDao.getExerciseWithMuscleGroup(exerciseId)




    val state = combine(_state, _exerciseRecords, _exerciseWithMuscleGroup) { state,
                                                                                 exerciseRecords,
                                                                                 exercise,
                                                                                  ->
        state.copy(
            exerciseRecords = exerciseRecords,
            exercise = exercise,

        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GraphState())

    fun onEvent(event: GraphEvent) {
        when (event) {
            is GraphEvent.SetEndDate -> {
                _state.update {
                    it.copy(
                        endDateTime = event.date
                    )
                }
                endDateTime = event.date
                viewModelScope.launch {
                    exerciseRecordDao.getExerciseRecordsBetweenDates(
                        exerciseId = _state.value.exercise.exercise.id,
                        startDate = startDateTime,
                        endDate = endDateTime
                    )
                }

            }
            is GraphEvent.SetStartDate -> {
                _state.update {
                    it.copy(
                        startDateTime = event.date
                    )
                }
                startDateTime = event.date
                viewModelScope.launch {
                    exerciseRecordDao.getExerciseRecordsBetweenDates(
                        exerciseId = _state.value.exercise.exercise.id,
                        startDate = startDateTime,
                        endDate = endDateTime
                    )
                }
            }
        }
    }
}