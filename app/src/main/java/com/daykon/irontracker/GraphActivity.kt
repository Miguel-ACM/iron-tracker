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
import androidx.room.Room
import com.daykon.irontracker.db.Database
import com.daykon.irontracker.db.GraphViewModel
import com.daykon.irontracker.ui.theme.IronTrackerTheme


class GraphActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            Database::class.java,
            "ironTracker.db"
        ).createFromAsset("ironTracker.db").build()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b: Bundle? = intent.extras
        var muscleId = 0
        if (b != null) {
            muscleId = b.getInt("exerciseId")
        }
        val viewModel by viewModels<GraphViewModel>(
            factoryProducer = {
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return GraphViewModel(db.exerciseDao,
                            db.exerciseRecordDao,
                            muscleId
                        ) as T
                    }
                }
            }
        )

        setContent {
            IronTrackerTheme {
                val state by viewModel.state.collectAsState()
                GraphScreen(state = state, onEvent = viewModel::onEvent)

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}