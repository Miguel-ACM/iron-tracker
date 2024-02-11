package com.daykon.irontracker.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daykon.irontracker.db.ProgressPicDao
import com.daykon.irontracker.viewModels.events.ProgressPicEvent
import com.daykon.irontracker.viewModels.state.ProgressPicState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ProgressPicViewModel(
    private val progressPicDao: ProgressPicDao
) : ViewModel() {
  private val _state = MutableStateFlow(ProgressPicState())

  private val _progressPics = progressPicDao.getProgressPics()

  val state = combine(_state, _progressPics) { state,
                                               progressPics ->
    state.copy(
        progressPics = progressPics,

        )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressPicState())


  fun onEvent(event: ProgressPicEvent) {
    when (event) {
      is ProgressPicEvent.AddProgressPic -> {
        viewModelScope.launch {
          //progressPicDao.deleteAll()
          progressPicDao.insertProgressPic(event.progressPic)
        }
      }
    }
  }
}