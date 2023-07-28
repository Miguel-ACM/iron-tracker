package com.daykon.irontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.daykon.irontracker.db.Database
import com.daykon.irontracker.screens.CameraScreen
import com.daykon.irontracker.viewModels.ExerciseViewModel
import com.daykon.irontracker.screens.GraphScreen
import com.daykon.irontracker.screens.MainScreen
import com.daykon.irontracker.ui.theme.IronTrackerTheme


class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            Database::class.java,
            "ironTracker.db"
        ).createFromAsset("ironTracker.db").build()
    }

    private val exerciseViewModel by viewModels<ExerciseViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExerciseViewModel(db.exerciseDao, db.exerciseRecordDao, db.muscleGroupDao) as T
                }
            }
        }
    )



    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IronTrackerTheme {
                val exerciseState by exerciseViewModel.state.collectAsState()
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main",
                        enterTransition = {slideIn(initialOffset = { IntOffset(-it.width, 0) }) },
                        exitTransition = {slideOut(targetOffset = { IntOffset(-it.width, 0)  }) }) {
                        MainScreen(state = exerciseState, onEvent = exerciseViewModel::onEvent, navController = navController)

                    }
                    composable("graph/{exerciseId}",
                        enterTransition = {slideIn(initialOffset = { IntOffset(it.width, 0) }) },
                        exitTransition = {slideOut(targetOffset = { IntOffset(it.width, 0)  }) }) {
                        GraphScreen(db = db, exerciseId = it.arguments?.getString("exerciseId") ?: "1")
                    }
                    composable("camera",
                        enterTransition = {slideIn(initialOffset = { IntOffset(it.width, 0) }) },
                        exitTransition = {slideOut(targetOffset = { IntOffset(-it.width, 0)  }) }) {
                        CameraScreen()

                    }
                }
            }
        }
    }
}