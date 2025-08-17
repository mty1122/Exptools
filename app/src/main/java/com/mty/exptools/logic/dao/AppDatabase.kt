package com.mty.exptools.logic.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mty.exptools.ExptoolsApp
import com.mty.exptools.logic.model.photo.PhotoDraftEntity
import com.mty.exptools.logic.model.photo.PhotoStepEntity
import com.mty.exptools.logic.model.syn.SynthesisDraftEntity
import com.mty.exptools.logic.model.syn.SynthesisStepEntity

@Database(
    entities = [
        SynthesisDraftEntity::class,
        SynthesisStepEntity::class,
        PhotoDraftEntity::class,
        PhotoStepEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun synthesisDao(): SynthesisDao
    abstract fun photoDao(): PhotoDao

    companion object {

        private var instance: AppDatabase? = null

        @Synchronized
        fun get(): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(ExptoolsApp.context,
                AppDatabase::class.java, "exptools")
                .build().apply {
                    instance = this
                }
        }

    }

}