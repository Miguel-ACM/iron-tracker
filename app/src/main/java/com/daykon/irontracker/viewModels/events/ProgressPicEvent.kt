package com.daykon.irontracker.viewModels.events

import com.daykon.irontracker.db.ProgressPic

sealed interface ProgressPicEvent {
    data class AddProgressPic(val progressPic: ProgressPic): ProgressPicEvent
}