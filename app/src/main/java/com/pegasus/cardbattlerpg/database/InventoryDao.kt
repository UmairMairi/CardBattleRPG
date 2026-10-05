package com.pegasus.cardbattlerpg.database

import androidx.room.*
import com.pegasus.cardbattlerpg.entity.InventoryEntity

@Dao
interface InventoryDao {

    // ==========================
    // COUNT
    // ==========================

    @Query("SELECT COUNT(*) FROM inventory")
    suspend fun countInventory(): Int

    @Query("SELECT COUNT(*) FROM inventory WHERE quantity > 0")
    suspend fun countAvailableItems(): Int

    @Query("SELECT COUNT(*) FROM inventory WHERE itemType = :type")
    suspend fun countByType(type: String): Int

    // ==========================
    // GET
    // ==========================

    @Query("""
        SELECT *
        FROM inventory
        ORDER BY itemType ASC, itemName ASC
    """)
    suspend fun getInventory(): List<InventoryEntity>

    @Query("""
        SELECT *
        FROM inventory
        WHERE quantity > 0
        ORDER BY itemType ASC, itemName ASC
    """)
    suspend fun getAvailableInventory(): List<InventoryEntity>

    @Query("""
        SELECT *
        FROM inventory
        WHERE itemType = :type
        ORDER BY itemName ASC
    """)
    suspend fun getInventoryByType(type: String): List<InventoryEntity>

    @Query("""
        SELECT *
        FROM inventory
        WHERE id = :itemId
        LIMIT 1
    """)
    suspend fun getItemById(itemId: Int): InventoryEntity?

    @Query("""
        SELECT *
        FROM inventory
        WHERE itemName = :itemName
        LIMIT 1
    """)
    suspend fun getItemByName(itemName: String): InventoryEntity?

    @Query("""
        SELECT *
        FROM inventory
        WHERE itemName LIKE '%' || :keyword || '%'
           OR itemType LIKE '%' || :keyword || '%'
        ORDER BY itemName ASC
    """)
    suspend fun searchInventory(keyword: String): List<InventoryEntity>

    // ==========================
    // INSERT
    // ==========================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryEntity>)

    // ==========================
    // UPDATE
    // ==========================

    @Update
    suspend fun updateItem(item: InventoryEntity)

    @Query("""
        UPDATE inventory
        SET quantity = quantity + :amount,
            image = :image,
            itemType = :itemType
        WHERE itemName = :itemName
    """)
    suspend fun increaseQuantity(
        itemName: String,
        itemType: String,
        image: String,
        amount: Int
    )

    @Query("""
        UPDATE inventory
        SET quantity = quantity - :amount
        WHERE id = :itemId
    """)
    suspend fun decreaseQuantity(
        itemId: Int,
        amount: Int
    )

    @Query("""
        UPDATE inventory
        SET quantity = :quantity
        WHERE id = :itemId
    """)
    suspend fun setQuantity(
        itemId: Int,
        quantity: Int
    )

    // ==========================
    // DELETE
    // ==========================

    @Delete
    suspend fun deleteItem(item: InventoryEntity)

    @Query("DELETE FROM inventory WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Int)

    @Query("DELETE FROM inventory WHERE itemName = :itemName")
    suspend fun deleteItemByName(itemName: String)

    @Query("DELETE FROM inventory WHERE quantity <= 0")
    suspend fun deleteEmptyItems()

    @Query("DELETE FROM inventory")
    suspend fun clearInventory()

    // ==========================
    // SHOP SYNC HELPER
    // ==========================

    @Transaction
    suspend fun addOrUpdateItem(
        itemName: String,
        itemType: String,
        image: String,
        amount: Int
    ) {
        val existing = getItemByName(itemName)

        if (existing == null) {
            insertItem(
                InventoryEntity(
                    itemName = itemName,
                    itemType = itemType,
                    quantity = amount,
                    image = image
                )
            )
        } else {
            updateItem(
                existing.copy(
                    quantity = existing.quantity + amount,
                    itemType = itemType,
                    image = image
                )
            )
        }
    }
}