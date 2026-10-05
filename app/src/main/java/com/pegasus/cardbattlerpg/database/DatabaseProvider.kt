package com.pegasus.cardbattlerpg.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    private const val DB_NAME = "spirit_card_game_safe_v17.db"

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
        }
    }

    private fun buildDatabase(context: Context): AppDatabase {
        // Hapus database lama yang sering menyebabkan force close karena schema Room berubah saat fase ditambah.
        context.deleteDatabase("spirit_card_game.db")
        context.deleteDatabase("spirit_card_game_safe_2026.db")
        context.deleteDatabase("spirit_card_game_safe_v16.db")

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        )
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }
}
