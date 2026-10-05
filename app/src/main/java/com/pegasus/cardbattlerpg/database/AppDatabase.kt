package com.pegasus.cardbattlerpg.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pegasus.cardbattlerpg.entity.*

@Database(
    entities = [
        PlayerEntity::class,
        CardEntity::class,
        HeroEntity::class,
        EquipmentEntity::class,
        InventoryEntity::class,
        MissionEntity::class,
        AchievementEntity::class,
        DeckEntity::class,
        DeckCardEntity::class,
        StoryStageEntity::class,
        ShopItemEntity::class,
        GuildEntity::class,
        GuildMemberEntity::class,
        GuildChatEntity::class

    ],
    version = 15,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao

    abstract fun cardDao(): CardDao

    abstract fun heroDao(): HeroDao

    abstract fun equipmentDao(): EquipmentDao

    abstract fun inventoryDao(): InventoryDao

    abstract fun missionDao(): MissionDao

    abstract fun achievementDao(): AchievementDao

    abstract fun deckDao(): DeckDao

    abstract fun storyDao(): StoryDao

    abstract fun shopDao(): ShopDao

    abstract fun guildDao(): GuildDao

}