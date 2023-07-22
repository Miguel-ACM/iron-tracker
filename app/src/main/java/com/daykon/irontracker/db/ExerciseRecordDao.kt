package com.daykon.irontracker.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ExerciseRecordDao {
    @Insert
    suspend fun insertExerciseRecord(exerciseRecord: ExerciseRecord)

    @Query("SELECT * FROM exerciseRecord WHERE exerciseId = :exerciseId ORDER BY date DESC")
    fun getExerciseRecords(exerciseId: Int): Flow<List<ExerciseRecord>>

    @Query("SELECT * FROM exerciseRecord WHERE exerciseId = :exerciseId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getExerciseRecordsBetweenDates(exerciseId: Int, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<ExerciseRecord>>

    @Query("SELECT * FROM exerciseRecord ORDER BY date DESC")
    fun getAllExerciseRecords(): Flow<List<ExerciseRecord>>

    @Query("SELECT * FROM exerciseRecord WHERE exerciseId = :exerciseId ORDER BY date ASC ")
    fun getAllExerciseRecordsForExercise(exerciseId: Int): Flow<List<ExerciseRecord>>
}