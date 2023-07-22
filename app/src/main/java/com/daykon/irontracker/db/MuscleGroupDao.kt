package com.daykon.irontracker.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MuscleGroupDao {
    @Upsert
    suspend fun insertMuscleGroup(muscleGroup: MuscleGroup)

    @Query("SELECT * FROM MuscleGroup")
    fun getMuscleGroups(): Flow<List<MuscleGroup>>

    @Query("SELECT * FROM MuscleGroup WHERE id = :id")
    fun getMuscleGroup(id: Int): Flow<MuscleGroup>
}