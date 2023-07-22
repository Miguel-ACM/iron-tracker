package com.daykon.irontracker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MuscleGroup::class, Exercise::class, ExerciseRecord::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converter::class)
abstract class Database: RoomDatabase() {

    abstract val muscleGroupDao: MuscleGroupDao
    abstract val exerciseDao: ExerciseDao
    abstract val exerciseRecordDao: ExerciseRecordDao
}