package com.daykon.irontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.daykon.irontracker.db.Database
import com.daykon.irontracker.db.ExerciseViewModel
import com.daykon.irontracker.db.GraphViewModel
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

    private val graphViewModel by viewModels<GraphViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GraphViewModel(db.exerciseDao, db.exerciseRecordDao, 1) as T
                }
            }
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IronTrackerTheme {
                val exerciseState by exerciseViewModel.state.collectAsState()
                val graphState by graphViewModel.state.collectAsState()
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(state = exerciseState, onEvent = exerciseViewModel::onEvent, navController = navController)
                    }
                    composable("graph/{exerciseId}") {
                        GraphScreen(state = graphState, onEvent = graphViewModel::onEvent, exerciseId = it.arguments?.getString("exerciseId") ?: "1")
                    }
                }

                //MainScreen(state = state, onEvent = viewModel::onEvent, navController = navController)
                /*
                val actionObserver: Observer<Action> = Observer<Action> { action ->
                    when(action.value) {
                        Action.SHOW_GRAPH -> {
                            val i = Intent(this, GraphActivity::class.java)
                            val b = Bundle()
                            var exerciseId = 0
                            var exerciseIdArg = action.args["exerciseId"]
                            if (exerciseIdArg != null) {
                                exerciseId = exerciseIdArg.toInt()
                            }
                            b.putInt("exerciseId", exerciseId)
                            i.putExtras(b)
                            startActivity(i)
                        }
                    }
                }
                viewModel.mAction.removeObservers(this)
                viewModel.mAction.observe(this, actionObserver)*/
            }
        }
    }
}