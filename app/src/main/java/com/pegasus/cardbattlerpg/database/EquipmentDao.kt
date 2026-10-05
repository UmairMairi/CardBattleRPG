package com.pegasus.cardbattlerpg.database

import androidx.room.*
import com.pegasus.cardbattlerpg.entity.EquipmentEntity

@Dao
interface EquipmentDao {

    @Query("SELECT COUNT(*) FROM equipments")
    suspend fun countEquipments(): Int

    @Query("SELECT COUNT(*) FROM equipments")
    suspend fun countAllEquipments(): Int

    @Query("""
        SELECT * FROM equipments
        ORDER BY unlocked DESC, equipped DESC, type ASC, id ASC
    """)
    suspend fun getAllEquipments(): List<EquipmentEntity>

    @Query("SELECT * FROM equipments WHERE equipped = 1")
    suspend fun getEquippedItems(): List<EquipmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipments(items: List<EquipmentEntity>)

    @Update
    suspend fun updateEquipment(item: EquipmentEntity)

    @Query("UPDATE equipments SET equipped = 0 WHERE type = :type")
    suspend fun unequipByType(type: String)

    @Query("UPDATE equipments SET equipped = 1 WHERE id = :id")
    suspend fun equipItem(id: Int)

    @Query("UPDATE equipments SET equipped = 0 WHERE id = :id")
    suspend fun unequipItem(id: Int)

    @Query("UPDATE equipments SET unlocked = 1 WHERE id = :id")
    suspend fun unlockEquipment(id: Int)
}