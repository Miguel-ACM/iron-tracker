package com.daykon.irontracker.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.daykon.irontracker.composable.AddExerciseDialog
import com.daykon.irontracker.composable.AddExerciseRecordDialog
import com.daykon.irontracker.composable.DeleteDialog
import com.daykon.irontracker.composable.Drawer
import com.daykon.irontracker.composable.ExpandableSearchView
import com.daykon.irontracker.viewModels.events.ExerciseRecordEvent
import com.daykon.irontracker.viewModels.events.GraphEvent
import com.daykon.irontracker.viewModels.state.ExerciseState
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDateTime
import kotlin.math.roundToInt


@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterial3Api
@Composable
fun MainScreen(state: ExerciseState, onEvent: (ExerciseRecordEvent) -> Unit,
               onGraphEvent: (GraphEvent) -> Unit,
               navController: NavController) {
  val scope = rememberCoroutineScope()

  val drawerState = rememberDrawerState(DrawerValue.Closed)

  var navigating by remember {
    mutableStateOf(false)
  }

  Drawer(isSelected = 0, drawerState = drawerState, navController = navController) {
    Scaffold(
        floatingActionButton = {
          FloatingActionButton(onClick = {
            onEvent(ExerciseRecordEvent.ShowExerciseDialog)
          }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Exercise")
          }
        },
        topBar = {
          TopAppBar(title = {
            ExpandableSearchView(state, onEvent)

          }, navigationIcon = {
            IconButton(onClick = {
              scope.launch {
                drawerState.open()
              }
            }) {
              Icon(imageVector = Icons.Default.Menu, contentDescription = "Toggle drawer")
            }
          })

        },

        ) { padding ->


      val haptics = LocalHapticFeedback.current
      if (state.isAddingExerciseRecord) {
        AddExerciseRecordDialog(state = state, onEvent = onEvent)
      }
      if (state.isAddingExercise) {
        AddExerciseDialog(state = state, onEvent = onEvent)
      }
      if (state.isShowingDeleteDialog) {
        DeleteDialog(state = state, onEvent = onEvent)
      }

      fun CharSequence.unaccent(): String {
        val regexUnaccent = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return regexUnaccent.replace(temp, "")
      }

      val searchTermUnaccent = state.searchTerm.unaccent().lowercase()

      val processedExercises = remember(state.exercises, state.searchTerm, state.exerciseRecords) {
        state.exercises.mapNotNull { exercise ->
          val muscleGroup = state.muscleGroups.find { it.id == exercise.muscleGroupId }
          val latestRecord = state.exerciseRecords.find { it.exerciseId == exercise.id }
          if (muscleGroup != null) {
            Triple(exercise, muscleGroup, latestRecord)
          } else null
        }.filter { (exercise, muscleGroup, latestRecord) ->
          val isRelevantSearchTerm = searchTermUnaccent.isEmpty() ||
              muscleGroup.name.unaccent().contains(searchTermUnaccent, ignoreCase = true) ||
              muscleGroup.extraSearch.unaccent().contains(searchTermUnaccent, ignoreCase = true) ||
              exercise.name.unaccent().contains(searchTermUnaccent, ignoreCase = true)

          val isToday = latestRecord?.let {
            LocalDateTime.now().dayOfMonth == it.date.dayOfMonth &&
                LocalDateTime.now().month == it.date.month &&
                LocalDateTime.now().year == it.date.year
          } ?: false

          val matchesKeywordPartially = listOf("today", "hoy", "latest", "ultimo").any { keyword ->
            keyword.contains(searchTermUnaccent)
          }

          isRelevantSearchTerm || (isToday && matchesKeywordPartially)
        }
      }



      Column(modifier = Modifier.padding(PaddingValues(8.dp, 64.dp, 8.dp, 0.dp))) {
        // Map for Latest Exercise Records
        LazyColumn(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 64.dp)) {

          items(processedExercises) { (exercise, muscleGroup, latestRecord) ->

            val currentExercise = rememberUpdatedState(exercise)

            var pressOffset by remember {
              mutableStateOf(DpOffset.Zero)
            }

            var itemHeight by remember {
              mutableStateOf(0.dp)
            }

            var isContextDialogVisible by remember {
              mutableStateOf(false)
            }
            val density = LocalDensity.current
            val interactionSource = remember { MutableInteractionSource() }
            val isToday = latestRecord != null &&
                (LocalDateTime.now().dayOfMonth == latestRecord.date.dayOfMonth
                    && LocalDateTime.now().month == latestRecord.date.month
                    && LocalDateTime.now().year == latestRecord.date.year)


            Row(modifier = Modifier
                .fillMaxWidth()
                .pointerInput(currentExercise) {
                  detectTapGestures(onLongPress = {

                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                    isContextDialogVisible = true
                    val press = PressInteraction.Press(it)
                    interactionSource.tryEmit(press)
                    interactionSource.tryEmit(PressInteraction.Release(press))


                  }, onTap = {

                    if (!state.isAddingExerciseRecord && !state.isAddingExercise) {
                      val press = PressInteraction.Press(it)
                      interactionSource.tryEmit(press)
                      interactionSource.tryEmit(PressInteraction.Release(press))

                      if (!navigating) {
                        navigating = true
                        onGraphEvent(GraphEvent.SetExerciseId(currentExercise.value.id))
                        navController.navigate("graph/${currentExercise.value.id}")
                      }

                    }
                  })

                }
                .onSizeChanged {
                  itemHeight = with(density) { it.height.toDp() }
                }
                .indication(interactionSource, rememberRipple())
                .padding(PaddingValues(4.dp, 8.dp, 8.dp, 8.dp))) {
              Column(modifier = Modifier.weight(0.9f)) {
                Button(onClick = {
                  onEvent(ExerciseRecordEvent.ShowExerciseRecordDialog(exercise, latestRecord))

                }, shape = CutCornerShape(6.dp, 0.dp, 6.dp, 0.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(muscleGroup.color),
                        contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background
                        else MaterialTheme.colorScheme.onBackground)) {
                  Icon(imageVector = Icons.Default.Add, contentDescription = "")
                }
              }

              Column(modifier = Modifier.weight(3f)) {
                Box(contentAlignment = Alignment.Center) {
                  Text(text = exercise.name, fontSize = 16.sp)
                }
                Box(contentAlignment = Alignment.Center) {
                  Text(text = muscleGroup.name, fontSize = 12.sp)
                }
              }

              Column(modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
                  .background(if (isToday) Color(muscleGroup.color) else Color(0x00000000),
                      shape = RoundedCornerShape(size = 16.dp))


              ) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                  var text = ""
                  if (latestRecord != null && (latestRecord.reps != 0 || latestRecord.weight.roundToInt() != 0)) {
                    text = "${latestRecord.reps}x${latestRecord.weight.roundToInt()}kg"
                  }
                  Text(
                    text = text, fontSize = 16.sp,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onBackground
                  )
                }
              }
              DropdownMenu(
                  expanded = isContextDialogVisible,
                  onDismissRequest = {
                    isContextDialogVisible = false
                  },
                  offset = pressOffset.copy(y = pressOffset.y - itemHeight),
              ) {
                DropdownMenuItem(text = { Text("Delete") }, leadingIcon = {
                  Icon(imageVector = Icons.Outlined.Delete, contentDescription = "",
                      tint = Color(0xffcc0000))
                }, onClick = {
                  onEvent(ExerciseRecordEvent.ShowDeleteDialog(exercise.id))
                  isContextDialogVisible = false
                })
                DropdownMenuItem(text = { Text("Favorite") }, leadingIcon = {
                  Icon(imageVector = Icons.Outlined.Star, contentDescription = "")
                }, onClick = {
                  //onEvent(ExerciseRecordEvent.Favorite(exercise.id))
                  isContextDialogVisible = false
                })
              }
            }
          }
        }
      }
    }
  }


}