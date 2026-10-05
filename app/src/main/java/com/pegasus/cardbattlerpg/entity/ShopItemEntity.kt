package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_items")
data class ShopItemEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ==========================
    // BASIC
    // ==========================

    val itemName: String,

    val itemType: String,

    val itemImage: String,

    val description: String = "",

    // ==========================
    // RARITY
    // Common
    // Rare
    // Epic
    // Legendary
    // Mythic
    // ==========================

    val rarity: String = "Common",

    // ==========================
    // PRICE
    // ==========================

    val priceGold: Int = 0,

    val priceDiamond: Int = 0,

    val sellGold: Int = 0,

    val quantity: Int = 1,

    // ==========================
    // SHOP TYPE
    // ==========================

    val isDiamondShop: Boolean = false,

    // ==========================
    // FEATURED ITEM
    // ==========================

    val featured: Boolean = false,

    // ==========================
    // FLASH SALE
    // ==========================

    val discountPercent: Int = 0,

    // unix timestamp
    val saleEndTime: Long = 0L,

    // ==========================
    // STOCK
    // -1 = unlimited
    // ==========================

    val stock: Int = -1,

    // ==========================
    // BADGE
    // NEW
    // HOT
    // EVENT
    // LIMITED
    // ==========================

    val badge: String = "",

    // ==========================
    // ICON EFFECT
    // glow color
    // ==========================

    val glowColor: String = "#FFD66B",

    // ==========================
    // SORTING
    // ==========================

    val sortOrder: Int = 0,

    // ==========================
    // ACTIVE
    // ==========================

    val active: Boolean = true
)