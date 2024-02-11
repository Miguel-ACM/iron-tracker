package com.daykon.irontracker.composable

import android.widget.Toast
import com.daykon.irontracker.viewModels.state.ExerciseState

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daykon.irontracker.viewModels.events.ExerciseRecordEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.daykon.irontracker.db.Exercise

@ExperimentalMaterial3Api
@Composable
fun DeleteDialog(state: ExerciseState, onEvent: (ExerciseRecordEvent) -> Unit,
                 modifier: Modifier = Modifier) {
  val exerciseId = state.exerciseId
  var exercise = Exercise(0, "", 0)
  for (e in state.exercises) {
    if (e.id == exerciseId) {
      exercise = e
      break
    }

  }
  val context = LocalContext.current
  AlertDialog(modifier = modifier, onDismissRequest = {
    onEvent(ExerciseRecordEvent.HideDeleteDialog)
  }, title = { Text(text = "Warning!") }, text = {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("Are you sure you want to delete ${exercise.name}?")

    }
  }, confirmButton = {
    Box(

        contentAlignment = Alignment.CenterEnd) {
      Button(onClick = {
        onEvent(ExerciseRecordEvent.DeleteExercise(exercise))
        onEvent(ExerciseRecordEvent.HideDeleteDialog)
        Toast.makeText(context, "${exercise.name} deleted", Toast.LENGTH_SHORT).show()
      }) {
        Text(text = "Delete", color = Color.Red)
      }
    }
  }, dismissButton = {
    Box(contentAlignment = Alignment.CenterEnd) {
      Button(onClick = {
        onEvent(ExerciseRecordEvent.HideDeleteDialog)
      }) {
        Text(text = "Cancel")
      }
    }
  })
}