package com.daykon.irontracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressPicDao {
  @Insert
  suspend fun insertProgressPic(progressPic: ProgressPic)

  @Query("SELECT * FROM ProgressPic ORDER BY date DESC")
  fun getProgressPics(): Flow<List<ProgressPic>>

  @Query("DELETE FROM ProgressPic")
  suspend fun deleteAll()
}