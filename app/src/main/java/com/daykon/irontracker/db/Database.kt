package com.daykon.irontracker.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


val MIGRATION_2_3: Migration = object : Migration(2, 3) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("CREATE INDEX IF NOT EXISTS index_ExerciseRecord_date ON ExerciseRecord(date)")
  }
}

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("ALTER TABLE ExerciseRecord ADD COLUMN isFav INTEGER NOT NULL DEFAULT 0")
  }
}

@Database(
    entities = [MuscleGroup::class, Exercise::class, ExerciseRecord::class, ProgressPic::class],
    version = 4, exportSchema = true, autoMigrations = [AutoMigration(from = 1, to = 2)]
)

@TypeConverters(Converter::class)
abstract class Database : RoomDatabase() {

  abstract val muscleGroupDao: MuscleGroupDao
  abstract val exerciseDao: ExerciseDao
  abstract val exerciseRecordDao: ExerciseRecordDao
  abstract val progressPicDao: ProgressPicDao
}