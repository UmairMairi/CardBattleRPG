package com.pegasus.cardbattlerpg.database

import androidx.room.*
import com.pegasus.cardbattlerpg.entity.HeroEntity

@Dao
interface HeroDao {

    // ==========================
    // COUNT
    // ==========================

    @Query("SELECT COUNT(*) FROM heroes")
    suspend fun countHeroes(): Int

    @Query("SELECT COUNT(*) FROM heroes WHERE selected = 1")
    suspend fun countSelectedHeroes(): Int

    @Query("SELECT COUNT(*) FROM heroes WHERE element = :element")
    suspend fun countHeroesByElement(element: String): Int

    // ==========================
    // GET HERO
    // ==========================

    @Query("""
        SELECT * FROM heroes
        ORDER BY selected DESC, level DESC, attack DESC, name ASC
    """)
    suspend fun getAllHeroes(): List<HeroEntity>

    @Query("""
        SELECT * FROM heroes
        WHERE selected = 1
        LIMIT 1
    """)
    suspend fun getSelectedHero(): HeroEntity?

    @Query("""
        SELECT * FROM heroes
        WHERE id = :heroId
        LIMIT 1
    """)
    suspend fun getHeroById(heroId: Int): HeroEntity?

    @Query("""
        SELECT * FROM heroes
        WHERE element = :element
        ORDER BY level DESC, attack DESC
    """)
    suspend fun getHeroesByElement(element: String): List<HeroEntity>

    @Query("""
        SELECT * FROM heroes
        WHERE name LIKE '%' || :keyword || '%'
        ORDER BY selected DESC, level DESC, name ASC
    """)
    suspend fun searchHeroes(keyword: String): List<HeroEntity>

    // ==========================
    // SELECT HERO
    // ==========================

    @Query("UPDATE heroes SET selected = 0")
    suspend fun clearSelectedHero()

    @Query("""
        UPDATE heroes
        SET selected = 1
        WHERE id = :heroId
    """)
    suspend fun selectHero(heroId: Int)

    @Transaction
    suspend fun setSelectedHero(heroId: Int) {
        clearSelectedHero()
        selectHero(heroId)
    }

    // ==========================
    // INSERT
    // ==========================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHero(hero: HeroEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeroes(heroes: List<HeroEntity>)

    // ==========================
    // UPDATE
    // ==========================

    @Update
    suspend fun updateHero(hero: HeroEntity)

    @Query("""
        UPDATE heroes
        SET exp = exp + :amount
        WHERE id = :heroId
    """)
    suspend fun addExp(heroId: Int, amount: Int)

    @Query("""
        UPDATE heroes
        SET level = :level,
            exp = :exp,
            hp = :hp,
            attack = :attack,
            defense = :defense
        WHERE id = :heroId
    """)
    suspend fun updateHeroStats(
        heroId: Int,
        level: Int,
        exp: Int,
        hp: Int,
        attack: Int,
        defense: Int
    )

    @Query("""
        UPDATE heroes
        SET image = :image
        WHERE id = :heroId
    """)
    suspend fun updateHeroImage(
        heroId: Int,
        image: String
    )

    @Query("""
        UPDATE heroes
        SET unlocked = 1,
            updatedAt = :time
        WHERE id = :heroId
    """)
    suspend fun unlockHero(
        heroId: Int,
        time: Long = System.currentTimeMillis()
    )

    // ==========================
    // DELETE
    // ==========================

    @Delete
    suspend fun deleteHero(hero: HeroEntity)

    @Query("DELETE FROM heroes")
    suspend fun deleteAllHeroes()

    @Query("SELECT COUNT(*) FROM heroes WHERE unlocked = 1")
    suspend fun countUnlockedHeroes(): Int
}