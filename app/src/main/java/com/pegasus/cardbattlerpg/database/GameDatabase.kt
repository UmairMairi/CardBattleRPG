package com.pegasus.cardbattlerpg.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pegasus.cardbattlerpg.entity.CardEntity

@Database(
    entities = [CardEntity::class],
    version = 1
)
abstract class GameDatabase : RoomDatabase()