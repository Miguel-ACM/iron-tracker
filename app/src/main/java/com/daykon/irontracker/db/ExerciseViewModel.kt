package com.daykon.irontracker.db

import android.util.Log
import androidx.lifecycle.MutableLiveData
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
import kotlin.math.roundToInt
import java.text.DecimalFormat

fun roundOffDecimal(number: Float): String? {
    val df = DecimalFormat("#.##")
    return df.format(number)
}
class Action(val value: Int, _args: Map<String, String>) {
    companion object Action {
        const val NONE = 0
        const val SHOW_GRAPH = 1
    }

    var args: Map<String, String> = _args

}

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseViewModel (
    private val exerciseDao: ExerciseDao,
    //private val exercises =
    private val exerciseRecordDao: ExerciseRecordDao,
    muscleGroupDao: MuscleGroupDao
        ): ViewModel() {
    private val _state = MutableStateFlow(ExerciseState())
    private val _exercises = exerciseDao.getExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _exerciseRecords = exerciseRecordDao.getAllExerciseRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _muscleGroups = muscleGroupDao.getMuscleGroups()

    val state = combine(_state, _exercises, _exerciseRecords, _muscleGroups) { state, exercises, exerciseRecords, muscleGroups ->
        state.copy(
            exercises = exercises,
            exerciseRecords = exerciseRecords,
            muscleGroups = muscleGroups
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExerciseState())

    val mAction: MutableLiveData<Action> = MutableLiveData<Action>()

    fun onEvent(event: ExerciseRecordEvent) {
        Log.d("INFO", "EVENT")
        when (event) {
            ExerciseRecordEvent.HideExerciseRecordDialog -> {
                _state.update {
                    it.copy(
                        isAddingExerciseRecord = false
                    )
                }
            }

            ExerciseRecordEvent.SaveExerciseRecord -> {
                val exerciseId: Int = state.value.exerciseId
                val maxReps: Float = state.value.maxReps
                val maxWeight: String = state.value.maxWeight

                if (maxReps < 0.6f || maxWeight.toFloatOrNull() == null){
                    return
                }

                val exerciseRecord = ExerciseRecord(
                    exerciseId = exerciseId,
                    weight = maxWeight.toFloat(),
                    reps = maxReps.roundToInt(),
                    date = LocalDateTime.now()
                )

                viewModelScope.launch {
                    exerciseRecordDao.insertExerciseRecord(exerciseRecord)
                }

                _state.update {
                    it.copy(
                        isAddingExerciseRecord = false,
                        maxWeight = "",
                        maxReps = 0f,
                        exerciseId = 0
                    )
                }
            }

            is ExerciseRecordEvent.ShowExerciseRecordDialog -> {
                val maxWeight = event.exerciseRecord.weight

                val maxWeightString =
                    if (maxWeight <= 0f) {
                        ""
                    } else {
                        roundOffDecimal(maxWeight)
                    }

                    _state.update {
                        it.copy(
                            exerciseId = event.exercise.id,
                            maxReps = event.exerciseRecord.reps.toFloat(),
                            maxWeight = maxWeightString ?: "",
                            isAddingExerciseRecord = true
                        )
                    }
                }


            is ExerciseRecordEvent.SetExerciseId -> {
                _state.update {
                    it.copy(
                        exerciseId = event.id
                    )
                }
            }

            is ExerciseRecordEvent.SetMaxWeight -> {
                _state.update {
                    it.copy(
                        maxWeight = event.maxWeight
                    )
                }

            }

            is ExerciseRecordEvent.SetReps -> {
                _state.update {
                    it.copy(
                        maxReps = event.reps
                    )
                }
            }

            is ExerciseRecordEvent.ShowGraph -> {
                mAction.setValue(Action(Action.SHOW_GRAPH, mapOf("exerciseId" to event.id.toString())))
                mAction.setValue(Action(Action.NONE, mapOf("exerciseIdj" to event.id.toString())))
            }

            is ExerciseRecordEvent.UpdateSearch -> {
                _state.update {
                    it.copy(
                        searchTerm = event.term
                    )
                }
            }
            ExerciseRecordEvent.ShowExerciseDialog -> {
                _state.update {
                    it.copy(
                        isAddingExercise = true
                    )
                }
            }
            ExerciseRecordEvent.HideExerciseDialog -> {
                _state.update {
                    it.copy(
                        isAddingExercise = false
                    )
                }
            }
            ExerciseRecordEvent.SaveExercise -> {
                val newExerciseName: String = state.value.newExerciseName
                val muscleGroupId: Int = state.value.newExerciseMuscleGroupId

                if (newExerciseName == ""){
                    return
                }

                val exercise = Exercise(
                    name = newExerciseName,
                    muscleGroupId = muscleGroupId
                )

                viewModelScope.launch {
                    exerciseDao.insertExercise(exercise)
                }

                _state.update {
                    it.copy(
                        isAddingExercise = false,
                        newExerciseName = "",
                        newExerciseMuscleGroupId = 0
                    )
                }
            }
            is ExerciseRecordEvent.SetNewExerciseName -> {
                _state.update {
                    it.copy(
                        newExerciseName = event.newExerciseName
                    )
                }
            }

            is ExerciseRecordEvent.SetNewExerciseMuscleGroupId -> {
                _state.update {
                    it.copy(
                        newExerciseMuscleGroupId = event.muscleGroupId
                    )
                }
            }

            ExerciseRecordEvent.HideDeleteDialog -> {
                _state.update {
                    it.copy(
                        isShowingDeleteDialog = false
                    )
                }
            }
            is ExerciseRecordEvent.ShowDeleteDialog -> {
                _state.update {
                    it.copy(
                        exerciseId = event.exerciseId,
                        isShowingDeleteDialog = true
                    )
                }
            }

            is ExerciseRecordEvent.DeleteExercise -> {
                viewModelScope.launch {
                    exerciseDao.deleteExercise(event.exercise)
                }
            }
        }
    }
}