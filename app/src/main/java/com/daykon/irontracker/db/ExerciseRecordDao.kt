package com.daykon.irontracker.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Insert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ExerciseRecordDao {
  @Insert
  suspend fun insertExerciseRecord(exerciseRecord: ExerciseRecord)

  @Query("UPDATE ExerciseRecord SET weight =:weight WHERE id = :exerciseId")
  fun updateWeight(exerciseId: Int, weight: Float)

  @Query("UPDATE ExerciseRecord SET reps = :reps WHERE id = :exerciseId")
  fun updateReps(exerciseId: Int, reps: Int)

  @Query("UPDATE ExerciseRecord SET reps = :reps, weight = :weight  WHERE id = :exerciseId")
  fun update(exerciseId: Int, reps: Int, weight: Float)

  @Query("SELECT * FROM exerciseRecord WHERE exerciseId = :exerciseId ORDER BY date DESC")
  fun getExerciseRecords(exerciseId: Int): Flow<List<ExerciseRecord>>

  @Query(
      "SELECT * FROM exerciseRecord WHERE exerciseId = :exerciseId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
  fun getExerciseRecordsBetweenDates(exerciseId: Int, startDate: LocalDateTime,
                                     endDate: LocalDateTime): Flow<List<ExerciseRecord>>

  @Query(
      "SELECT * FROM exerciseRecord WHERE exerciseId = :exerciseId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
  fun getExerciseRecordsBetweenDatesNoFlow(exerciseId: Int, startDate: LocalDateTime,
                                           endDate: LocalDateTime): List<ExerciseRecord>

  @Query("SELECT * FROM exerciseRecord ORDER BY date DESC")
  fun getAllExerciseRecords(): Flow<List<ExerciseRecord>>

  @Query("SELECT *" +
      "FROM exerciseRecord " +
      "INNER JOIN (" +
      "    SELECT exerciseId, MAX(date) as MaxDate" +
      "    FROM exerciseRecord" +
      "    GROUP BY exerciseId" +
      ") AS LatestRecords " +
      "ON exerciseRecord.exerciseId = LatestRecords.exerciseId " +
      "AND exerciseRecord.date = LatestRecords.MaxDate;")
  fun getLatestExerciseRecords(): Flow<List<ExerciseRecord>>

  @Query("SELECT * FROM exerciseRecord WHERE exerciseId = :exerciseId ORDER BY date ASC ")
  fun getAllExerciseRecordsForExercise(exerciseId: Int): Flow<List<ExerciseRecord>>

  @Delete
  suspend fun deleteRecord(exerciseRecord: ExerciseRecord)
}