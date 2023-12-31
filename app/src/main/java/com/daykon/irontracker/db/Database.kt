package com.daykon.irontracker.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MuscleGroup::class, Exercise::class, ExerciseRecord::class, ProgressPic::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [ AutoMigration (
        from = 1,
        to = 2
    )]

    )
@TypeConverters(Converter::class)
abstract class Database: RoomDatabase() {

    abstract val muscleGroupDao: MuscleGroupDao
    abstract val exerciseDao: ExerciseDao
    abstract val exerciseRecordDao: ExerciseRecordDao
    abstract val progressPicDao: ProgressPicDao
}