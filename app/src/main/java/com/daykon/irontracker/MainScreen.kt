package com.daykon.irontracker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.daykon.irontracker.db.ExerciseRecord
import com.daykon.irontracker.db.ExerciseRecordEvent
import com.daykon.irontracker.db.ExerciseState
import com.daykon.irontracker.db.MuscleGroup
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.roundToInt




@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterial3Api
@Composable
fun MainScreen (
    state: ExerciseState,
    onEvent: (ExerciseRecordEvent) -> Unit,
    navController: NavController
    ) {
    val scope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var navigating by remember {
        mutableStateOf(false)
    }

    Drawer(isSelected = 0,
           drawerState = drawerState
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    onEvent(ExerciseRecordEvent.ShowExerciseDialog)
                }) {
                    Icon(imageVector = Icons.Default.Add,
                        contentDescription = "Add Exercise")
                }
            },
            topBar = {
                TopAppBar(title = {
                    TextField(
                        value = state.searchTerm,
                        singleLine = true,
                        onValueChange = {
                            onEvent(ExerciseRecordEvent.UpdateSearch(it))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(0.dp, 8.dp, 0.dp, 8.dp)),
                        placeholder = {
                            Text(text = "Search exercise or muscle")
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            disabledTextColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(100),
                        leadingIcon = {Icon(Icons.Filled.Search, contentDescription = null)}
                    )
                },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Toggle drawer"
                            )
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


            Column(modifier = Modifier.padding(PaddingValues(8.dp, 64.dp, 8.dp, 0.dp))) {



                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(0.dp),

                ) {

                    state.exercises.filter {
                        var muscleGroup = MuscleGroup(
                            name = "?",
                            color = 0x00ff00,
                            extraSearch = "",
                            orderIndex = 0f
                        )
                        var i = 0
                        while (i < state.muscleGroups.size) {
                            if (state.muscleGroups[i].id == it.muscleGroupId) {
                                muscleGroup = state.muscleGroups[i]
                                break
                            }
                            i += 1
                        }
                        state.searchTerm == "" ||
                                muscleGroup.name.contains(state.searchTerm, ignoreCase = true)  ||
                                muscleGroup.extraSearch.contains(state.searchTerm, ignoreCase = true)  ||
                                it.name.contains(state.searchTerm, ignoreCase = true)
                    }.forEach() { exercise ->
                        var muscleGroup = MuscleGroup(
                            name = "?",
                            color = 0x00ff00,
                            extraSearch = "",
                            orderIndex = 0f
                        )
                        var i = 0
                        while (i < state.muscleGroups.size) {
                            if (state.muscleGroups[i].id == exercise.muscleGroupId) {
                                muscleGroup = state.muscleGroups[i]
                                break
                            }
                            i += 1
                        }

                        var latestRecord = ExerciseRecord(
                            exerciseId = 0,
                            weight = 0f,
                            reps = 0,
                            date = LocalDateTime.now()
                        )
                        i = 0
                        while (i < state.exerciseRecords.size) {
                            if (state.exerciseRecords[i].exerciseId == exercise.id) {
                                latestRecord = state.exerciseRecords[i]
                                break
                            }
                            i += 1
                        }
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(true) {
                                    detectTapGestures(
                                        onLongPress = {

                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                                            isContextDialogVisible = true
                                            val press = PressInteraction.Press(it)
                                            interactionSource.tryEmit(press)
                                            interactionSource.tryEmit(PressInteraction.Release(press))


                                        },
                                        onTap = {

                                            if (!state.isAddingExerciseRecord && !state.isAddingExercise) {
                                                val press = PressInteraction.Press(it)
                                                interactionSource.tryEmit(press)
                                                interactionSource.tryEmit(
                                                    PressInteraction.Release(
                                                        press
                                                    )
                                                )

                                                if (!navigating) {
                                                    navigating = true
                                                    navController.navigate("graph/${exercise.id}")
                                                }

                                            }
                                        }
                                    )

                                }
                                .onSizeChanged {
                                    itemHeight = with(density) { it.height.toDp() }
                                }
                                .indication(interactionSource, rememberRipple())
                                .padding(PaddingValues(4.dp, 8.dp, 8.dp, 8.dp))
                        ) {
                            Column(
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Button(onClick = {
                                    onEvent(ExerciseRecordEvent.ShowExerciseRecordDialog(exercise, latestRecord))

                                },
                                    shape = CutCornerShape(6.dp, 0.dp, 6.dp, 0.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(muscleGroup.color))) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "")
                                }
                            }

                            Column(
                                modifier = Modifier.weight(3f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = exercise.name,
                                        fontSize = 16.sp)
                                }
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = muscleGroup.name, fontSize = 12.sp)
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()

                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    var text = ""
                                    if (latestRecord.reps != 0 || latestRecord.weight.roundToInt() != 0){
                                        text = "${latestRecord.reps}x${latestRecord.weight.roundToInt()}kg"
                                    }
                                    Text(
                                        text = text,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = isContextDialogVisible,
                                onDismissRequest = {
                                    isContextDialogVisible = false
                                },
                                offset = pressOffset.copy(
                                    y = pressOffset.y - itemHeight
                                ),
                            ) {
                                DropdownMenuItem(text = { Text("Delete") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "",
                                            tint = Color(0xffcc0000)
                                        )
                                    },
                                    onClick = {
                                        onEvent(ExerciseRecordEvent.ShowDeleteDialog(exercise.id))
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