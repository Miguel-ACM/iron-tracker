package com.daykon.irontracker.viewModels.state

import com.daykon.irontracker.db.ProgressPic

data class ProgressPicState(
    val progressPics: List<ProgressPic> = emptyList(),
)
