package com.example.miniproyecto02.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Tarea::class], version = 1, exportSchema = false)
abstract class TareasDatabase : RoomDatabase() {

    abstract fun tareaDao(): TareaDao

    companion object {
        @Volatile
        private var INSTANCE: TareasDatabase? = null

        fun getDatabase(context: Context): TareasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TareasDatabase::class.java,
                    "tareas_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
