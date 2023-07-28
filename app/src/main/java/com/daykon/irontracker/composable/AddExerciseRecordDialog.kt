package com.daykon.irontracker.composable

import android.util.Log
import com.daykon.irontracker.viewModels.state.ExerciseState

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daykon.irontracker.viewModels.events.ExerciseRecordEvent
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import kotlin.math.roundToInt

@ExperimentalMaterial3Api
@Composable
fun AddExerciseRecordDialog(
    state: ExerciseState,
    onEvent: (ExerciseRecordEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember {
        mutableFloatStateOf(1f)
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            Log.d("INFO","Ayuda no puedo escapar de aca")
            onEvent(ExerciseRecordEvent.HideExerciseRecordDialog)
        },
        title = { Text(text = "Add exercise record") },
        tonalElevation = 50.dp,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Weight")
                TextField(
                    value = state.maxWeight,
                    onValueChange = {
                        onEvent(ExerciseRecordEvent.SetMaxWeight(it))
                    },
                    placeholder = {
                        Text(text = "Weight (kg)")
                    }
                )
                Text("Reps")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 0.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.maxReps.roundToInt().toString())
                }
                Slider(value = state.maxReps,
                    onValueChange = {
                        onEvent(ExerciseRecordEvent.SetReps(it))
                        sliderValue = it
                    },
                    valueRange = 1f..20f,
                    steps = 18
                )

                //DropdownMenu(expanded = expanded,
                //    onDismissRequest = { onEvent(ExerciseRecordEvent.HideDialog) }) {
                //    state.exercises.forEach { exercise ->
                //        DropdownMenuItem(text = {exercise.name}, onClick = {
                //            ExerciseRecordEvent.SetExerciseId(exercise.id)
                //        })
                //    }
                //}

            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(onClick = {
                    onEvent(ExerciseRecordEvent.SaveExerciseRecord)
                }) {
                    Text(text = "Save")
                }
            }
        }
    )
}