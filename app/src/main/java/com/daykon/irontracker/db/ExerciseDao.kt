package com.daykon.irontracker.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Upsert
    suspend fun insertExercise(exercise: Exercise)

    @Query("SELECT e.* FROM Exercise e JOIN muscleGroup m ON e.muscleGroupId=m.id ORDER BY m.orderIndex ASC")
    fun getExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM Exercise WHERE id = :id")
    fun getExercise(id: Int): Flow<Exercise>

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Transaction
    @Query("SELECT * FROM Exercise WHERE id = :id")
    fun getExerciseWithMuscleGroup(id: Int): Flow<ExerciseWithMuscleGroup>

    @Transaction
    @Query("SELECT * FROM Exercise WHERE id = :id")
    fun getExerciseWithMuscleGroupNoFlow(id: Int): ExerciseWithMuscleGroup
}