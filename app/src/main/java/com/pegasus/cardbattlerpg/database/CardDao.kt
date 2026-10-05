package com.pegasus.cardbattlerpg.database

import androidx.room.*
import com.pegasus.cardbattlerpg.entity.CardEntity

@Dao
interface CardDao {

    // =====================================
    // COUNT
    // =====================================

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun countCards(): Int

    @Query("SELECT COUNT(*) FROM cards WHERE owned = 1")
    suspend fun countOwnedCards(): Int

    @Query("SELECT COUNT(*) FROM cards WHERE rarity = :rarity")
    suspend fun countCardsByRarity(
        rarity: String
    ): Int

    // =====================================
    // GET ALL
    // =====================================

    @Query("""
        SELECT * FROM cards
        ORDER BY rarity DESC,
        star DESC,
        level DESC,
        name ASC
    """)
    suspend fun getAllCards(): List<CardEntity>

    @Query("""
        SELECT * FROM cards
        WHERE owned = 1
        ORDER BY rarity DESC,
        star DESC
    """)
    suspend fun getOwnedCards(): List<CardEntity>

    @Query("""
        SELECT * FROM cards
        WHERE favorite = 1
        ORDER BY rarity DESC
    """)
    suspend fun getFavoriteCards(): List<CardEntity>

    // =====================================
    // GET SINGLE
    // =====================================

    @Query("""
        SELECT * FROM cards
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getCardById(
        id: Int
    ): CardEntity?

    // =====================================
    // SEARCH
    // =====================================

    @Query("""
        SELECT * FROM cards
        WHERE name LIKE '%' || :keyword || '%'
    """)
    suspend fun searchCards(
        keyword: String
    ): List<CardEntity>

    // =====================================
    // FILTER RARITY
    // =====================================

    @Query("""
        SELECT * FROM cards
        WHERE rarity = :rarity
        ORDER BY level DESC
    """)
    suspend fun getCardsByRarity(
        rarity: String
    ): List<CardEntity>

    // =====================================
    // FILTER ELEMENT
    // =====================================

    @Query("""
        SELECT * FROM cards
        WHERE element = :element
        ORDER BY attack DESC
    """)
    suspend fun getCardsByElement(
        element: String
    ): List<CardEntity>

    // =====================================
    // FILTER ROLE
    // =====================================

    @Query("""
        SELECT * FROM cards
        WHERE role = :role
    """)
    suspend fun getCardsByRole(
        role: String
    ): List<CardEntity>

    // =====================================
    // TOP CARD
    // =====================================

    @Query("""
        SELECT * FROM cards
        ORDER BY attack DESC
        LIMIT 10
    """)
    suspend fun getTopAttackCards(): List<CardEntity>

    @Query("""
        SELECT * FROM cards
        ORDER BY hp DESC
        LIMIT 10
    """)
    suspend fun getTopHpCards(): List<CardEntity>

    @Query("""
        SELECT * FROM cards
        ORDER BY pvpRating DESC
        LIMIT 20
    """)
    suspend fun getTopArenaCards(): List<CardEntity>

    // =====================================
    // GACHA
    // =====================================

    @Query("""
        SELECT * FROM cards
        WHERE obtainableFromGacha = 1
    """)
    suspend fun getGachaPool(): List<CardEntity>

    @Query("""
        SELECT * FROM cards
        WHERE featuredBanner = 1
    """)
    suspend fun getFeaturedCards(): List<CardEntity>

    // =====================================
    // INSERT
    // =====================================

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertCard(
        card: CardEntity
    )

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertCards(
        cards: List<CardEntity>
    )

    // =====================================
    // UPDATE
    // =====================================

    @Update
    suspend fun updateCard(
        card: CardEntity
    )

    // =====================================
    // LEVEL SYSTEM
    // =====================================

    @Query("""
        UPDATE cards
        SET level = :level
        WHERE id = :cardId
    """)
    suspend fun updateLevel(
        cardId: Int,
        level: Int
    )

    @Query("""
        UPDATE cards
        SET star = :star
        WHERE id = :cardId
    """)
    suspend fun updateStar(
        cardId: Int,
        star: Int
    )

    @Query("""
        UPDATE cards
        SET awakenLevel = :awakenLevel
        WHERE id = :cardId
    """)
    suspend fun updateAwaken(
        cardId: Int,
        awakenLevel: Int
    )

    // =====================================
    // OWNED
    // =====================================

    @Query("""
        UPDATE cards
        SET owned = 1
        WHERE id = :cardId
    """)
    suspend fun markOwned(
        cardId: Int
    )

    @Query("""
        UPDATE cards
        SET owned = 0
        WHERE id = :cardId
    """)
    suspend fun markUnowned(
        cardId: Int
    )

    // =====================================
    // FAVORITE
    // =====================================

    @Query("""
        UPDATE cards
        SET favorite = 1
        WHERE id = :cardId
    """)
    suspend fun setFavorite(
        cardId: Int
    )

    @Query("""
        UPDATE cards
        SET favorite = 0
        WHERE id = :cardId
    """)
    suspend fun removeFavorite(
        cardId: Int
    )

    // =====================================
    // LOCK
    // =====================================

    @Query("""
        UPDATE cards
        SET locked = 1
        WHERE id = :cardId
    """)
    suspend fun lockCard(
        cardId: Int
    )

    @Query("""
        UPDATE cards
        SET locked = 0
        WHERE id = :cardId
    """)
    suspend fun unlockCard(
        cardId: Int
    )

    // =====================================
    // EQUIPPED
    // =====================================

    @Query("""
        UPDATE cards
        SET equipped = 1
        WHERE id = :cardId
    """)
    suspend fun equipCard(
        cardId: Int
    )

    @Query("""
        UPDATE cards
        SET equipped = 0
        WHERE id = :cardId
    """)
    suspend fun unequipCard(
        cardId: Int
    )

    @Query("""
        SELECT * FROM cards
        WHERE equipped = 1
    """)
    suspend fun getEquippedCards():
            List<CardEntity>

    // =====================================
    // DELETE
    // =====================================

    @Delete
    suspend fun deleteCard(
        card: CardEntity
    )

    @Query("DELETE FROM cards")
    suspend fun deleteAllCards()
}