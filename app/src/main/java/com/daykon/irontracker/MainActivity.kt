package com.daykon.irontracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.daykon.irontracker.db.Action
import com.daykon.irontracker.db.Database
import com.daykon.irontracker.db.ExerciseViewModel
import com.daykon.irontracker.ui.theme.IronTrackerTheme


class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            Database::class.java,
            "ironTracker.db"
        ).createFromAsset("ironTracker.db").build()
    }

    private val viewModel by viewModels<ExerciseViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExerciseViewModel(db.exerciseDao, db.exerciseRecordDao, db.muscleGroupDao) as T
                }
            }
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IronTrackerTheme {
                val state by viewModel.state.collectAsState()
                MainScreen(state = state, onEvent = viewModel::onEvent)
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
                viewModel.mAction.observe(this, actionObserver)
            }
        }
    }
}