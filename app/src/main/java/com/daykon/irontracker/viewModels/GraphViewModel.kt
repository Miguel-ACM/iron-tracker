package com.daykon.irontracker.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daykon.irontracker.db.Exercise
import com.daykon.irontracker.db.ExerciseDao
import com.daykon.irontracker.db.ExerciseRecordDao
import com.daykon.irontracker.db.ExerciseWithMuscleGroup
import com.daykon.irontracker.db.MuscleGroup
import com.daykon.irontracker.viewModels.events.GraphEvent
import com.daykon.irontracker.viewModels.state.GraphState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime


@OptIn(ExperimentalCoroutinesApi::class)
class GraphViewModel(private val exerciseDao: ExerciseDao,
                     private val exerciseRecordDao: ExerciseRecordDao) :
    ViewModel() {
  private val _exerciseId = MutableStateFlow(0) // Default exerciseId
  val exerciseId: StateFlow<Int> = _exerciseId.asStateFlow()
  private val _state = MutableStateFlow(GraphState())

  private var startDateTime: LocalDateTime = LocalDateTime.now().minusYears(10)
  private var endDateTime: LocalDateTime = LocalDateTime.now().plusYears(1)

  // Updated to use functions for dynamic fetching
  val defaultExerciseValueWithMatchGroup = ExerciseWithMuscleGroup(
    Exercise(
        id = 0,
        name = "Exercise",
        muscleGroupId = 0
    ),
      MuscleGroup(
          id = 0,
          name = "Muscle Group",
          color = 0,
          extraSearch = "",
          orderIndex = 0f
      )
  )
  init {
    viewModelScope.launch {
      _state.collect {
        updateExerciseAndRecords()
      }
    }
  }

  private val _exerciseRecords = _exerciseId.flatMapLatest { id ->
    flow {
      emit(exerciseRecordDao.getExerciseRecordsBetweenDatesNoFlow(id, startDateTime, endDateTime))
    }.flowOn(Dispatchers.IO)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _exerciseWithMuscleGroup = _exerciseId.flatMapLatest { id ->
    flow {
      emit(exerciseDao.getExerciseWithMuscleGroupNoFlow(id))
    }.flowOn(Dispatchers.IO)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultExerciseValueWithMatchGroup)

  val state = combine(_state, _exerciseRecords, _exerciseWithMuscleGroup) { state, exerciseRecords, exerciseWithMuscleGroup ->
    state.copy(
        exerciseRecords = exerciseRecords,
        exercise = exerciseWithMuscleGroup,
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GraphState())


  private suspend fun updateExerciseAndRecords() {
    // This function now just triggers a state update to refresh the flows
    _state.emit(_state.value)
  }

  fun onEvent(event: GraphEvent) {
    when (event) {
      is GraphEvent.SetEndDate -> {
        _state.update {
          it.copy(endDateTime = event.date)
        }
        endDateTime = event.date
        viewModelScope.launch {
          exerciseRecordDao.getExerciseRecordsBetweenDates(
              exerciseId = _state.value.exercise.exercise.id, startDate = startDateTime,
              endDate = endDateTime)
        }
      }

      is GraphEvent.SetStartDate -> {
        _state.update {
          it.copy(startDateTime = event.date)
        }
        startDateTime = event.date
        viewModelScope.launch {
          exerciseRecordDao.getExerciseRecordsBetweenDates(
              exerciseId = _state.value.exercise.exercise.id, startDate = startDateTime,
              endDate = endDateTime)
        }
      }

      is GraphEvent.SetExerciseId -> {
        _exerciseId.value = event.exerciseId // Update exerciseId which triggers flows
      }

      is GraphEvent.SetSelectedPoint -> {
        var reps = 0
        var weight = 0f
        if (event.point >= 0 && event.point < state.value.exerciseRecords.size) {
          reps = state.value.exerciseRecords[event.point].reps
          weight = state.value.exerciseRecords[event.point].weight
        }
        _state.update {
          it.copy(
              selectedPoint = event.point,
              selectedPointReps = reps.toString(),
              selectedPointWeight = weight.toString(),
          )
        }
      }

      is GraphEvent.SetWeight -> {
        val weight: String = event.weight.replace(Regex("[^0-9.,]"), "")
        _state.update {
          it.copy(
              selectedPointWeight = weight,
          )
        }
      }

      is GraphEvent.SetReps -> {
        val reps: String = event.reps.replace(Regex("[^0-9]"), "")
        _state.update {
          it.copy(
              selectedPointReps = reps,
          )
        }
      }

      GraphEvent.UpdateRecord -> {
        val maxReps: String = state.value.selectedPointReps
        val maxWeight: String = state.value.selectedPointWeight.replace(",", ".")
        val exerciseRecordId = state.value.exerciseRecords[state.value.selectedPoint].id
        if (maxReps.toFloatOrNull() == null || maxReps.toFloat() < 0.6f || maxWeight.toFloatOrNull() == null) {
          return
        }

        viewModelScope.launch(Dispatchers.IO) {
          exerciseRecordDao.update(exerciseId = exerciseRecordId, weight = maxWeight.toFloat(),
              reps = maxReps.toInt())
        }
      }

      is GraphEvent.SetBoxVisibility -> {
        _state.update {
          it.copy(isBoxVisible = event.isVisible)
        }

      }

      is GraphEvent.DeleteRecord -> {
        viewModelScope.launch {
          exerciseRecordDao.deleteRecord(event.event)
        }
      }
    }
  }
}