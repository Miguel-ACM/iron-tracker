package com.daykon.irontracker.composable

import android.widget.Toast
import com.daykon.irontracker.viewModels.state.ExerciseState

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daykon.irontracker.viewModels.events.ExerciseRecordEvent
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

@ExperimentalMaterial3Api
@Composable
fun AddExerciseDialog(
    state: ExerciseState,
    onEvent: (ExerciseRecordEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedDropbox by remember { mutableStateOf(state.muscleGroups[state.newExerciseMuscleGroupId].name) }
    val context = LocalContext.current
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onEvent(ExerciseRecordEvent.HideExerciseDialog)
        },
        title = { Text(text = "Add exercise") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Name")
                TextField(
                    value = state.newExerciseName,
                    onValueChange = {
                        onEvent(ExerciseRecordEvent.SetNewExerciseName(it))
                    },
                    placeholder = {
                        Text(text = "Exercise name")
                    }
                )
                Text("Muscle group")
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                       expanded = !expanded
                    }) {
                    TextField(
                        value = selectedDropbox,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        state.muscleGroups.forEach { muscleGroup ->
                            DropdownMenuItem(
                                text = { Text(text = muscleGroup.name) },
                                onClick = {
                                    selectedDropbox = muscleGroup.name
                                    expanded = false
                                    onEvent(ExerciseRecordEvent.SetNewExerciseMuscleGroupId(muscleGroup.id))
                                },

                                )
                        }
                    }
                }

            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(onClick = {
                    onEvent(ExerciseRecordEvent.SaveExercise)
                    Toast.makeText(context, "${state.newExerciseName} created", Toast.LENGTH_SHORT).show()
                }) {
                    Text(text = "Save")
                }
            }
        }
    )
}