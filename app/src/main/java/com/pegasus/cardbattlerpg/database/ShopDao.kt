package com.pegasus.cardbattlerpg.database

import androidx.room.*
import com.pegasus.cardbattlerpg.entity.ShopItemEntity

@Dao
interface ShopDao {

    // ==========================
    // COUNT
    // ==========================

    @Query("SELECT COUNT(*) FROM shop_items")
    suspend fun countItems(): Int

    // ==========================
    // ALL ITEM
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        ORDER BY
        itemType ASC,
        itemName ASC
    """)
    suspend fun getAllItems(): List<ShopItemEntity>

    // ==========================
    // BY ID
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getItemById(id: Int): ShopItemEntity?

    // ==========================
    // BY TYPE
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        WHERE itemType = :type
        ORDER BY itemName ASC
    """)
    suspend fun getItemsByType(
        type: String
    ): List<ShopItemEntity>

    // ==========================
    // DIAMOND SHOP
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        WHERE isDiamondShop = 1
        ORDER BY itemName ASC
    """)
    suspend fun getDiamondItems(): List<ShopItemEntity>

    // ==========================
    // GOLD SHOP
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        WHERE isDiamondShop = 0
        ORDER BY itemName ASC
    """)
    suspend fun getGoldItems(): List<ShopItemEntity>

    // ==========================
    // SEARCH
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        WHERE
            itemName LIKE '%' || :keyword || '%'
            OR itemType LIKE '%' || :keyword || '%'
        ORDER BY itemName ASC
    """)
    suspend fun searchItems(
        keyword: String
    ): List<ShopItemEntity>

    // ==========================
    // INSERT SINGLE
    // ==========================

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertItem(
        item: ShopItemEntity
    )

    // ==========================
    // INSERT MULTI
    // ==========================

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertItems(
        items: List<ShopItemEntity>
    )

    // ==========================
    // UPDATE
    // ==========================

    @Update
    suspend fun updateItem(
        item: ShopItemEntity
    )

    // ==========================
    // DELETE
    // ==========================

    @Delete
    suspend fun deleteItem(
        item: ShopItemEntity
    )

    // ==========================
    // CLEAR
    // ==========================

    @Query("DELETE FROM shop_items")
    suspend fun clearShop()

    // ==========================
    // CATEGORY COUNT
    // ==========================

    @Query("""
        SELECT COUNT(*)
        FROM shop_items
        WHERE itemType = :type
    """)
    suspend fun countByType(
        type: String
    ): Int

    // ==========================
    // RANDOM FEATURED ITEM
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun getFeaturedItem(): ShopItemEntity?

    // ==========================
    // CHEST
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        WHERE itemType = 'Chest'
        ORDER BY id ASC
    """)
    suspend fun getChestItems(): List<ShopItemEntity>

    // ==========================
    // TICKET
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        WHERE itemType = 'Ticket'
        ORDER BY id ASC
    """)
    suspend fun getTicketItems(): List<ShopItemEntity>

    // ==========================
    // MATERIAL
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        WHERE itemType = 'Material'
        ORDER BY id ASC
    """)
    suspend fun getMaterialItems(): List<ShopItemEntity>

    // ==========================
    // UPGRADE
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        WHERE itemType = 'Upgrade'
        ORDER BY id ASC
    """)
    suspend fun getUpgradeItems(): List<ShopItemEntity>

    // ==========================
    // CONSUMABLE
    // ==========================

    @Query("""
        SELECT *
        FROM shop_items
        WHERE itemType = 'Consumable'
        ORDER BY id ASC
    """)
    suspend fun getConsumableItems(): List<ShopItemEntity>
}