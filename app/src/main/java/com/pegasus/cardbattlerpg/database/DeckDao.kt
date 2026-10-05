package com.pegasus.cardbattlerpg.database

import androidx.room.*
import com.pegasus.cardbattlerpg.entity.DeckCardEntity
import com.pegasus.cardbattlerpg.entity.DeckEntity

@Dao
interface DeckDao {

    @Query("SELECT COUNT(*) FROM decks")
    suspend fun countDecks(): Int

    @Query("""
        SELECT *
        FROM decks
        ORDER BY id ASC
    """)
    suspend fun getDecks(): List<DeckEntity>

    @Query("""
        SELECT *
        FROM decks
        WHERE selected = 1
        LIMIT 1
    """)
    suspend fun getSelectedDeck(): DeckEntity?

    @Query("""
        SELECT id
        FROM decks
        WHERE selected = 1
        LIMIT 1
    """)
    suspend fun getSelectedDeckId(): Int?

    @Query("""
        SELECT *
        FROM deck_cards
        WHERE deckId = :deckId
        ORDER BY id ASC
    """)
    suspend fun getDeckCards(
        deckId: Int
    ): List<DeckCardEntity>

    @Query("""
        SELECT COUNT(*)
        FROM deck_cards
        WHERE deckId = :deckId
    """)
    suspend fun countDeckCards(
        deckId: Int
    ): Int

    @Query("""
        SELECT COUNT(*)
        FROM deck_cards
        WHERE deckId = :deckId
        AND cardId = :cardId
    """)
    suspend fun isCardInDeck(
        deckId: Int,
        cardId: Int
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(
        deck: DeckEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeckCard(
        deckCard: DeckCardEntity
    )

    @Update
    suspend fun updateDeck(
        deck: DeckEntity
    )

    @Delete
    suspend fun deleteDeck(
        deck: DeckEntity
    )

    @Query("""
        UPDATE decks
        SET selected = 0
    """)
    suspend fun clearSelectedDeck()

    @Query("""
        UPDATE decks
        SET selected = 1
        WHERE id = :deckId
    """)
    suspend fun selectDeck(
        deckId: Int
    )

    @Query("""
        DELETE FROM deck_cards
        WHERE deckId = :deckId
        AND cardId = :cardId
    """)
    suspend fun removeCardFromDeck(
        deckId: Int,
        cardId: Int
    )

    @Query("""
        DELETE FROM deck_cards
        WHERE deckId = :deckId
    """)
    suspend fun clearDeck(
        deckId: Int
    )

    @Query("""
        DELETE FROM decks
        WHERE id = :deckId
    """)
    suspend fun deleteDeckById(
        deckId: Int
    )

    @Query("""
        SELECT COUNT(*)
        FROM deck_cards
        WHERE deckId = :deckId
    """)
    suspend fun getDeckSize(
        deckId: Int
    ): Int
}