package com.pegasus.cardbattlerpg.repository

import com.pegasus.cardbattlerpg.database.*
import com.pegasus.cardbattlerpg.entity.*
import com.pegasus.cardbattlerpg.data.SeedData
import com.pegasus.cardbattlerpg.model.SaveGameSummary
import com.pegasus.cardbattlerpg.model.RankingPlayer

class GameRepository(
    private val playerDao: PlayerDao,
    private val cardDao: CardDao,
    private val heroDao: HeroDao,
    private val equipmentDao: EquipmentDao,
    private val inventoryDao: InventoryDao,
    private val missionDao: MissionDao,
    private val achievementDao: AchievementDao,
    private val deckDao: DeckDao,
    private val storyDao: StoryDao,
    private val shopDao: ShopDao,
    private val guildDao: GuildDao
) {

    suspend fun seedInitialData() {
        if (cardDao.countCards() == 0) cardDao.insertCards(SeedData.cards())
        if (heroDao.countHeroes() == 0) heroDao.insertHeroes(SeedData.heroes())
        if (equipmentDao.countEquipments() == 0) equipmentDao.insertEquipments(SeedData.equipments())
        if (missionDao.countMissions() == 0) missionDao.insertMissions(SeedData.missions())
        if (achievementDao.countAchievements() == 0) achievementDao.insertAchievements(SeedData.achievements())
        if (deckDao.countDecks() == 0) deckDao.insertDeck(SeedData.defaultDeck())
        if (storyDao.countStages() == 0) storyDao.insertStages(SeedData.storyStages())
        if (shopDao.countItems() == 0) shopDao.insertItems(SeedData.shopItems())
    }

    suspend fun getPlayer(): PlayerEntity? = playerDao.getPlayer()

    suspend fun createGuestPlayer(name: String) {
        playerDao.savePlayer(PlayerEntity(id = 1, name = name.ifBlank { "Guest Player" }))
        touchSave()
    }

    suspend fun getAllCards(): List<CardEntity> = cardDao.getAllCards()
    suspend fun getOwnedCards(): List<CardEntity> = cardDao.getOwnedCards()
    suspend fun getOwnedCardCount(): Int = cardDao.countOwnedCards()
    suspend fun getAllHeroes(): List<HeroEntity> = heroDao.getAllHeroes()
    suspend fun getInventory(): List<InventoryEntity> = inventoryDao.getInventory().filter { it.quantity > 0 }
    suspend fun getMissions(): List<MissionEntity> = missionDao.getAllMissions()
    suspend fun getAchievements(): List<AchievementEntity> = achievementDao.getAllAchievements()
    suspend fun getDecks(): List<DeckEntity> = deckDao.getDecks()
    suspend fun getSelectedDeck(): DeckEntity? = deckDao.getSelectedDeck()
    suspend fun getDeckCards(deckId: Int): List<DeckCardEntity> = deckDao.getDeckCards(deckId)

    suspend fun addCardToDeck(deckId: Int, cardId: Int): String {
        if (deckDao.isCardInDeck(deckId, cardId) > 0) return "Kartu sudah ada di deck"
        if (deckDao.countDeckCards(deckId) >= 30) return "Deck maksimal 30 kartu"

        deckDao.insertDeckCard(DeckCardEntity(deckId = deckId, cardId = cardId))
        addMissionProgress("Deck Builder")
        touchSave()

        return "Kartu berhasil ditambahkan"
    }

    suspend fun removeCardFromDeck(deckId: Int, cardId: Int) {
        deckDao.removeCardFromDeck(deckId, cardId)
        touchSave()
    }

    suspend fun clearDeck(deckId: Int) {
        deckDao.clearDeck(deckId)
        touchSave()
    }

    suspend fun selectHero(heroId: Int) {
        val hero = heroDao.getHeroById(heroId)
        if (hero != null && hero.unlocked) {
            heroDao.setSelectedHero(heroId)
            touchSave()
        }
    }

    fun heroGoldPrice(hero: HeroEntity): Int {
        val rarityBase = when (hero.rarity) {
            "Legendary" -> 50000
            "Epic" -> 25000
            "Rare" -> 10000
            else -> 5000
        }

        val roleBonus = when (hero.role) {
            "Tank" -> 2500
            "Mage" -> 3500
            "Assassin" -> 4000
            "Support" -> 3000
            else -> 2000
        }

        return rarityBase + roleBonus + (hero.level * 500)
    }

    suspend fun buyHeroWithGold(hero: HeroEntity): String {
        if (hero.unlocked) return "${hero.name} sudah terbuka"

        val player = playerDao.getPlayer() ?: return "Player tidak ditemukan"
        val price = heroGoldPrice(hero)

        if (player.gold < price) return "Gold tidak cukup. Butuh $price Gold"

        playerDao.updatePlayer(
            player.copy(
                gold = player.gold - price,
                lastSavedAt = System.currentTimeMillis()
            )
        )

        heroDao.updateHero(hero.copy(unlocked = true, updatedAt = System.currentTimeMillis()))
        addHeroTrainingMission()
        touchSave()
        return "${hero.name} berhasil dibuka"
    }

    suspend fun levelUpHero(hero: HeroEntity): String {
        val player = playerDao.getPlayer() ?: return "Player tidak ditemukan"
        val safeMaxExp = hero.maxExp.coerceAtLeast(100)

        if (hero.exp < safeMaxExp) return "EXP belum cukup. Butuh $safeMaxExp EXP"

        val cost = hero.level * 250
        if (player.gold < cost) return "Gold tidak cukup. Butuh $cost gold"

        val updatedHero = hero.copy(
            level = hero.level + 1,
            exp = (hero.exp - safeMaxExp).coerceAtLeast(0),
            maxExp = (safeMaxExp * 1.25f).toInt().coerceAtLeast(safeMaxExp + 25),
            hp = hero.hp + 40,
            attack = hero.attack + 8,
            defense = hero.defense + 5,
            updatedAt = System.currentTimeMillis()
        )

        heroDao.updateHero(updatedHero)
        playerDao.updatePlayer(player.copy(gold = player.gold - cost, lastSavedAt = System.currentTimeMillis()))

        addHeroTrainingMission()
        touchSave()

        return "Hero naik ke level ${updatedHero.level}"
    }

    suspend fun getAllEquipments(): List<EquipmentEntity> = equipmentDao.getAllEquipments()
    suspend fun getEquippedItems(): List<EquipmentEntity> = equipmentDao.getEquippedItems()

    suspend fun equipItem(equipment: EquipmentEntity): String {
        if (!equipment.unlocked) {
            return "${equipment.name} masih terkunci. Buka dulu dengan ${equipment.unlockPriceGold} Gold"
        }

        equipmentDao.unequipByType(equipment.type)
        equipmentDao.equipItem(equipment.id)
        touchSave()

        return "${equipment.name} berhasil dipasang"
    }

    suspend fun unequipItem(equipment: EquipmentEntity): String {
        equipmentDao.unequipItem(equipment.id)
        touchSave()
        return "${equipment.name} dilepas"
    }

    suspend fun unlockEquipment(equipment: EquipmentEntity): String {
        val player = playerDao.getPlayer() ?: return "Player tidak ditemukan"

        if (equipment.unlocked) return "${equipment.name} sudah terbuka"

        val price = equipment.unlockPriceGold.coerceAtLeast(0)
        if (player.gold < price) return "Gold tidak cukup. Butuh $price Gold"

        playerDao.updatePlayer(
            player.copy(
                gold = player.gold - price,
                lastSavedAt = System.currentTimeMillis()
            )
        )

        equipmentDao.unlockEquipment(equipment.id)

        addEquipmentMissionProgress()
        touchSave()

        return "${equipment.name} berhasil dibuka"
    }

    suspend fun getSelectedHero(): HeroEntity? = heroDao.getSelectedHero()

    suspend fun getBattleDeckCards(): List<CardEntity> {
        val deck = deckDao.getSelectedDeck() ?: return emptyList()
        val deckCards = deckDao.getDeckCards(deck.id)
        val ownedCards = cardDao.getOwnedCards()
        val deckIds = deckCards.map { it.cardId }

        return ownedCards.filter { deckIds.contains(it.id) }
    }

    suspend fun giveBattleReward(heroId: Int, gold: Int, exp: Int) {
        playerDao.addGold(gold)
        heroDao.addExp(heroId, exp)
        addBattleMissionProgress()
        touchSave()
    }

    suspend fun getStoryStages(): List<StoryStageEntity> = storyDao.getAllStages()
    suspend fun getStoryStage(stageId: Int): StoryStageEntity? = storyDao.getStageById(stageId)

    suspend fun completeStoryStage(stageId: Int) {
        storyDao.completeStage(stageId)
        storyDao.unlockStage(stageId + 1)
        addStoryMissionProgress()
        touchSave()
    }

    suspend fun summonCard(): Pair<String, CardEntity?> {
        val player = playerDao.getPlayer() ?: return Pair("Player tidak ditemukan", null)
        val cost = com.pegasus.cardbattlerpg.ui.summon.SummonManager.summonCost()

        if (player.diamond < cost) return Pair("Diamond tidak cukup. Butuh $cost diamond", null)

        val rarity = com.pegasus.cardbattlerpg.ui.summon.SummonManager.rollRarity(
            legendaryPity = player.legendaryPity,
            epicPity = player.epicPity
        )

        val cards = cardDao.getCardsByRarity(rarity)
        if (cards.isEmpty()) return Pair("Kartu rarity $rarity belum tersedia", null)

        val result = cards.random()

        val newLegendaryPity = if (rarity == "Legendary") 0 else player.legendaryPity + 1
        val newEpicPity = if (rarity == "Legendary" || rarity == "Epic") 0 else player.epicPity + 1

        playerDao.updatePlayer(
            player.copy(
                diamond = player.diamond - cost,
                legendaryPity = newLegendaryPity,
                epicPity = newEpicPity,
                lastSavedAt = System.currentTimeMillis()
            )
        )

        cardDao.markOwned(result.id)
        addSummonMissionProgress()

        touchSave()

        val pityInfo =
            if (rarity == "Legendary")
                "Legendary Pity reset!"
            else
                "Pity Legendary: $newLegendaryPity/90 • Epic: $newEpicPity/10"

        return Pair(
            "Berhasil mendapatkan ${result.name} [$rarity]. $pityInfo",
            result.copy(owned = true)
        )
    }

    suspend fun summonCards(amount: Int): Pair<String, List<CardEntity>> {
        val results = mutableListOf<CardEntity>()
        var lastMessage = ""

        for (i in 1..amount) {
            val (msg, card) = summonCard()
            if (card != null) {
                results.add(card)
                lastMessage = msg
            } else {
                return Pair(msg, results)
            }
        }

        val legendaryCount = results.count { it.rarity == "Legendary" }
        val epicCount = results.count { it.rarity == "Epic" }

        return Pair(
            "Mendapatkan ${results.size} kartu! (Legendary: $legendaryCount, Epic: $epicCount)",
            results
        )
    }


    suspend fun useBattlePotion(item: InventoryEntity, currentHp: Int, maxHp: Int): Pair<String, Int> {
        if (item.quantity <= 0) return Pair("Item sudah habis", currentHp)
        if (item.itemType != "Consumable" || !item.itemName.contains("Potion", ignoreCase = true)) {
            return Pair("${item.itemName} tidak bisa dipakai saat battle", currentHp)
        }

        val healAmount = when {
            item.itemName.contains("Mega", ignoreCase = true) -> (maxHp * 0.75f).toInt()
            item.itemName.contains("Large", ignoreCase = true) -> (maxHp * 0.45f).toInt()
            else -> (maxHp * 0.25f).toInt()
        }.coerceAtLeast(50)

        val newHp = (currentHp + healAmount).coerceAtMost(maxHp)
        consumeInventoryItem(item)
        touchSave()
        return Pair("${item.itemName} digunakan. HP pulih +${newHp - currentHp}", newHp)
    }

    private suspend fun consumeInventoryItem(item: InventoryEntity) {
        val newQuantity = item.quantity - 1
        if (newQuantity <= 0) inventoryDao.deleteItemById(item.id) else inventoryDao.updateItem(item.copy(quantity = newQuantity))
    }

    private suspend fun addOrUpdateInventoryItem(
        itemName: String,
        itemType: String,
        image: String,
        amount: Int,
        rarity: String = "Common",
        description: String = "",
        source: String = "Reward",
        sellGold: Int = 0,
        badge: String = ""
    ) {
        if (amount <= 0) return
        val existing = inventoryDao.getItemByName(itemName)
        if (existing == null) {
            inventoryDao.insertItem(
                InventoryEntity(
                    itemName = itemName,
                    itemType = itemType,
                    image = image,
                    quantity = amount,
                    rarity = rarity,
                    description = description,
                    source = source,
                    sellGold = sellGold,
                    badge = badge
                )
            )
        } else {
            inventoryDao.updateItem(
                existing.copy(
                    quantity = existing.quantity + amount,
                    itemType = itemType,
                    image = image,
                    rarity = rarity,
                    description = if (description.isNotBlank()) description else existing.description,
                    source = source,
                    sellGold = sellGold,
                    badge = if (badge.isNotBlank()) badge else existing.badge
                )
            )
        }
    }

    private suspend fun openChest(item: InventoryEntity): String {
        val chestName = item.itemName
        val rewardCount = when {
            chestName.contains("Legendary", true) -> 5
            chestName.contains("Gold", true) -> 4
            chestName.contains("Silver", true) -> 3
            else -> 2
        }
        val pool = when {
            chestName.contains("Legendary", true) -> listOf(
                ChestReward("Awaken Stone", "Upgrade", "item_awaken_stone", 1, "Legendary"),
                ChestReward("Legendary Ticket", "Ticket", "item_legend_ticket", 1, "Legendary"),
                ChestReward("Mythril Ore", "Material", "item_mythril_ore", 3, "Epic"),
                ChestReward("Hero Shard", "Upgrade", "item_hero_shard", 15, "Epic"),
                ChestReward("Mega Potion", "Consumable", "item_mega_potion", 2, "Epic")
            )
            chestName.contains("Gold", true) -> listOf(
                ChestReward("Epic Ticket", "Ticket", "item_epic_ticket", 1, "Epic"),
                ChestReward("Hero Scroll", "Upgrade", "item_hero_scroll", 1, "Epic"),
                ChestReward("Hero Shard", "Upgrade", "item_hero_shard", 8, "Epic"),
                ChestReward("Mythril Ore", "Material", "item_mythril_ore", 2, "Epic"),
                ChestReward("Mega Potion", "Consumable", "item_mega_potion", 1, "Epic")
            )
            chestName.contains("Silver", true) -> listOf(
                ChestReward("Summon Ticket", "Ticket", "item_summon_ticket", 1, "Rare"),
                ChestReward("EXP Book", "Upgrade", "item_exp_book", 2, "Rare"),
                ChestReward("Large Potion", "Consumable", "item_large_potion", 2, "Rare"),
                ChestReward("Fire Crystal", "Material", "item_fire_crystal", 3, "Rare"),
                ChestReward("Water Crystal", "Material", "item_water_crystal", 3, "Rare"),
                ChestReward("Iron Ore", "Material", "item_iron_ore", 5, "Common")
            )
            else -> listOf(
                ChestReward("Small Potion", "Consumable", "item_small_potion", 2, "Common"),
                ChestReward("EXP Book", "Upgrade", "item_exp_book", 1, "Rare"),
                ChestReward("Iron Ore", "Material", "item_iron_ore", 3, "Common"),
                ChestReward("Summon Ticket", "Ticket", "item_summon_ticket", 1, "Rare"),
                ChestReward("Hero Shard", "Upgrade", "item_hero_shard", 3, "Epic")
            )
        }

        consumeInventoryItem(item)
        val rewards = (1..rewardCount).map { pool.random() }
        rewards.forEach { reward ->
            addOrUpdateInventoryItem(
                itemName = reward.name,
                itemType = reward.type,
                image = reward.image,
                amount = reward.amount,
                rarity = reward.rarity,
                source = "Chest",
                description = "Reward dari $chestName"
            )
        }
        touchSave()
        return "$chestName dibuka: " + rewards.joinToString { "${it.name} x${it.amount}" }
    }

    private data class ChestReward(
        val name: String,
        val type: String,
        val image: String,
        val amount: Int,
        val rarity: String
    )


    suspend fun useMaterialForEquipment(item: InventoryEntity): String {
        if (item.quantity <= 0) return "${item.itemName} sudah habis"
        val target = equipmentDao.getEquippedItems().firstOrNull()
            ?: equipmentDao.getAllEquipments().firstOrNull { it.unlocked }
            ?: return "Tidak ada equipment yang bisa di-upgrade"

        val power = when {
            item.itemName.contains("Mythril", true) -> 3
            item.itemName.contains("Crystal", true) -> 2
            else -> 1
        }

        val upgraded = target.copy(
            attackBonus = target.attackBonus + if (target.type == "Weapon" || item.itemName.contains("Crystal", true)) 4 * power else 1 * power,
            defenseBonus = target.defenseBonus + if (target.type == "Armor" || target.type == "Ring") 3 * power else 1 * power,
            hpBonus = target.hpBonus + if (target.type == "Armor" || target.type == "Amulet") 35 * power else 10 * power
        )

        equipmentDao.updateEquipment(upgraded)
        consumeInventoryItem(item)
        addEquipmentMissionProgress()
        touchSave()
        return "${item.itemName} digunakan. ${target.name} berhasil diperkuat."
    }

    suspend fun useUpgradeItemForSelectedHero(item: InventoryEntity): String {
        if (item.quantity <= 0) return "${item.itemName} sudah habis"

        if (item.itemName.equals("Hero Shard", true)) {
            val lockedHero = heroDao.getAllHeroes().firstOrNull { !it.unlocked }
            if (lockedHero != null) {
                if (item.quantity < 25) return "Butuh 25 Hero Shard untuk membuka hero"
                val remaining = item.quantity - 25
                if (remaining <= 0) inventoryDao.deleteItemById(item.id) else inventoryDao.updateItem(item.copy(quantity = remaining))
                heroDao.updateHero(lockedHero.copy(unlocked = true, updatedAt = System.currentTimeMillis()))
                touchSave()
                return "${lockedHero.name} berhasil dibuka dengan Hero Shard"
            }
        }

        val hero = heroDao.getSelectedHero() ?: return "Pilih hero terlebih dahulu"

        val updatedHero = when {
            item.itemName.equals("EXP Book", true) -> hero.copy(
                exp = hero.exp + 100,
                updatedAt = System.currentTimeMillis()
            )
            item.itemName.equals("Hero Scroll", true) -> hero.copy(
                level = hero.level + 1,
                hp = hero.hp + 55,
                attack = hero.attack + 10,
                defense = hero.defense + 7,
                updatedAt = System.currentTimeMillis()
            )
            item.itemName.equals("Hero Shard", true) -> {
                if (item.quantity < 10) return "Butuh 10 Hero Shard untuk upgrade hero"
                val remaining = item.quantity - 10
                if (remaining <= 0) inventoryDao.deleteItemById(item.id) else inventoryDao.updateItem(item.copy(quantity = remaining))
                hero.copy(
                    hp = hero.hp + 90,
                    attack = hero.attack + 14,
                    defense = hero.defense + 10,
                    updatedAt = System.currentTimeMillis()
                )
            }
            item.itemName.equals("Awaken Stone", true) -> hero.copy(
                hp = hero.hp + 180,
                attack = hero.attack + 25,
                defense = hero.defense + 18,
                maxExp = (hero.maxExp * 1.35f).toInt(),
                updatedAt = System.currentTimeMillis()
            )
            else -> return "${item.itemName} bukan item upgrade hero"
        }

        heroDao.updateHero(updatedHero)
        if (!item.itemName.equals("Hero Shard", true)) consumeInventoryItem(item)
        addHeroTrainingMission()
        touchSave()
        return "${item.itemName} digunakan untuk ${hero.name}. Power hero meningkat."
    }

    suspend fun useInventoryItem(item: InventoryEntity): String {
        if (item.quantity <= 0) return "Item sudah habis"

        return when (item.itemType) {
            "Consumable" -> {
                if (item.itemName.contains("Potion", ignoreCase = true)) {
                    "${item.itemName} dipakai melalui panel Potion di Battle agar efek HP masuk saat bertarung."
                } else {
                    consumeInventoryItem(item)
                    touchSave()
                    "Menggunakan ${item.itemName}."
                }
            }
            "Ticket" -> "Gunakan ${item.itemName} di menu Summon."
            "Material" -> useMaterialForEquipment(item)
            "Upgrade" -> useUpgradeItemForSelectedHero(item)
            "Chest" -> openChest(item)
            else -> "${item.itemName} belum memiliki efek."
        }
    }

    suspend fun claimMission(mission: MissionEntity): String {
        if (!mission.completed) return "Misi belum selesai"
        if (mission.claimed) return "Reward sudah diambil"

        playerDao.addReward(
            gold = mission.rewardGold,
            diamond = mission.rewardDiamond
        )

        missionDao.updateMission(mission.copy(claimed = true))
        touchSave()

        return "Reward berhasil diambil: ${mission.rewardGold} Gold, ${mission.rewardDiamond} Diamond"
    }

    suspend fun addMissionProgress(title: String, amount: Int = 1) {
        missionDao.addProgressSafe(title = title, amount = amount)
        missionDao.refreshCompletedMissions()
    }

    suspend fun addBattleMissionProgress() {
        addMissionProgress("First Battle")
        addMissionProgress("Battle Warrior")
        addAchievementProgress("Battle Master", 1)
    }

    suspend fun addSummonMissionProgress(amount: Int = 1) {
        addMissionProgress("Summon Spirit", amount)
        addMissionProgress("Summon Master", amount)
        addAchievementProgress("Card Master Beginner", amount)
    }

    suspend fun addShopMissionProgress() {
        addMissionProgress("Buy Item")
        addMissionProgress("Shop Lover")
        addAchievementProgress("Shop Buyer", 1)
    }

    suspend fun addEquipmentMissionProgress() {
        addMissionProgress("Equipment Buyer")
        addAchievementProgress("Equipment Collector", 1)
    }

    suspend fun addStoryMissionProgress() {
        addMissionProgress("Story Beginner")
        addAchievementProgress("Story Explorer", 1)
    }

    suspend fun addHeroTrainingMission() {
        addMissionProgress("Hero Training")
        addAchievementProgress("Hero Trainer", 1)
    }

    suspend fun addGuildMissionProgress() {
        addMissionProgress("Guild Donation")
        addMissionProgress("Guild Supporter")
    }

    suspend fun getClaimableMissionCount(): Int {
        return missionDao.countClaimableMissions()
    }

    suspend fun resetDailyMission() {
        missionDao.resetDailyMissions()
    }

    suspend fun resetWeeklyMission() {
        missionDao.resetWeeklyMissions()
    }

    suspend fun claimAchievement(achievement: AchievementEntity): String {
        if (!achievement.unlocked) return "Achievement belum terbuka"
        if (achievement.claimed) return "Reward sudah diambil"

        playerDao.addReward(gold = 0, diamond = achievement.rewardDiamond)
        achievementDao.updateAchievement(achievement.copy(claimed = true))
        touchSave()

        return "Reward berhasil diambil: ${achievement.rewardDiamond} Diamond"
    }

    suspend fun addAchievementProgress(title: String, amount: Int = 1) {
        achievementDao.addProgress(title, amount)
        achievementDao.refreshUnlockedAchievements()
    }

    suspend fun touchSave() {
        playerDao.updateLastSavedAt(System.currentTimeMillis())
    }

    suspend fun getSaveSummary(): SaveGameSummary? {
        val player = playerDao.getPlayer() ?: return null

        return SaveGameSummary(
            playerName = player.name,
            level = player.level,
            gold = player.gold,
            diamond = player.diamond,
            ownedCards = cardDao.countOwnedCards(),
            heroes = heroDao.countUnlockedHeroes(),
            equipments = equipmentDao.countAllEquipments(),
            completedStages = storyDao.countCompletedStages(),
            lastSavedAt = player.lastSavedAt
        )
    }

    suspend fun resetSaveData() {
        playerDao.clearPlayer()
    }

    suspend fun calculatePlayerPower(): Int {
        val hero = heroDao.getSelectedHero()
        val equipments = equipmentDao.getEquippedItems()
        val ownedCards = cardDao.countOwnedCards()
        val completedStages = storyDao.countCompletedStages()

        val heroPower =
            if (hero != null)
                hero.hp + hero.attack * 10 + hero.defense * 8 + hero.level * 100
            else
                0

        val equipmentPower = equipments.sumOf {
            it.attackBonus * 10 + it.defenseBonus * 8 + it.hpBonus
        }

        return heroPower + equipmentPower + ownedCards * 75 + completedStages * 150
    }

    suspend fun buildRankingPlayer(): RankingPlayer? {
        val player = playerDao.getPlayer() ?: return null
        val hero = heroDao.getSelectedHero()

        return RankingPlayer(
            playerName = player.name,
            heroName = hero?.name ?: "No Hero",
            heroImage = hero?.image ?: "hero_unknown",
            level = player.level,
            power = calculatePlayerPower(),
            gold = player.gold,
            diamond = player.diamond,
            ownedCards = cardDao.countOwnedCards(),
            completedStages = storyDao.countCompletedStages(),
            arenaPoint = player.arenaPoint,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun giveArenaReward(gold: Int, point: Int) {
        playerDao.addArenaReward(gold, point)
        touchSave()
    }

    suspend fun getShopItems(): List<ShopItemEntity> = shopDao.getAllItems()

    suspend fun buyShopItem(item: ShopItemEntity): String {
        val player = playerDao.getPlayer() ?: return "Player tidak ditemukan"

        if (!item.active) return "Item ini tidak tersedia"
        if (item.stock == 0) return "${item.itemName} sudah habis"

        val finalGoldPrice =
            if (item.discountPercent > 0)
                item.priceGold - ((item.priceGold * item.discountPercent) / 100)
            else
                item.priceGold

        val finalDiamondPrice =
            if (item.discountPercent > 0)
                item.priceDiamond - ((item.priceDiamond * item.discountPercent) / 100)
            else
                item.priceDiamond

        if (item.isDiamondShop) {
            if (player.diamond < finalDiamondPrice) return "Diamond tidak cukup"
            playerDao.updatePlayer(player.copy(diamond = player.diamond - finalDiamondPrice, lastSavedAt = System.currentTimeMillis()))
        } else {
            if (player.gold < finalGoldPrice) return "Gold tidak cukup"
            playerDao.updatePlayer(player.copy(gold = player.gold - finalGoldPrice, lastSavedAt = System.currentTimeMillis()))
        }

        if (item.itemType == "Diamond Pack") {
            val currentPlayer = playerDao.getPlayer()
            if (currentPlayer != null) {
                playerDao.updatePlayer(
                    currentPlayer.copy(
                        diamond = currentPlayer.diamond + item.quantity,
                        lastSavedAt = System.currentTimeMillis()
                    )
                )
            }
        } else {
            addOrUpdateInventoryFromShop(item)
        }

        if (item.stock > 0) {
            shopDao.updateItem(item.copy(stock = item.stock - 1))
        }

        addShopMissionProgress()
        touchSave()

        return if (item.itemType == "Diamond Pack") {
            "${item.itemName} berhasil dibeli +${item.quantity} Diamond"
        } else {
            "${item.itemName} berhasil dibeli +${item.quantity}"
        }
    }

    private suspend fun addOrUpdateInventoryFromShop(item: ShopItemEntity) {
        val existing = inventoryDao.getItemByName(item.itemName)

        if (existing == null) {
            inventoryDao.insertItem(
                InventoryEntity(
                    itemName = item.itemName,
                    itemType = item.itemType,
                    quantity = item.quantity,
                    image = item.itemImage,
                    rarity = item.rarity,
                    description = item.description,
                    badge = item.badge,
                    source = "Shop",
                    sellGold = item.sellGold,
                    locked = false,
                    favorite = false
                )
            )
        } else {
            inventoryDao.updateItem(
                existing.copy(
                    quantity = existing.quantity + item.quantity,
                    itemType = item.itemType,
                    image = item.itemImage,
                    rarity = item.rarity,
                    description = item.description,
                    badge = item.badge,
                    source = "Shop",
                    sellGold = item.sellGold,
                    locked = false
                )
            )
        }
    }

    suspend fun updatePlayerProfile(name: String, avatar: String): String {
        val player = playerDao.getPlayer() ?: return "Player tidak ditemukan"

        playerDao.updatePlayer(
            player.copy(
                name = name.ifBlank { player.name },
                avatar = avatar,
                lastSavedAt = System.currentTimeMillis()
            )
        )

        return "Profile berhasil diperbarui"
    }

    suspend fun getGuilds(): List<GuildEntity> = guildDao.getAllGuilds()

    suspend fun getMyGuild(): GuildEntity? {
        val member = guildDao.getMyGuildMember() ?: return null
        return guildDao.getGuildById(member.guildId)
    }

    suspend fun getGuildMembers(guildId: Int): List<GuildMemberEntity> = guildDao.getMembers(guildId)
    suspend fun getGuildChats(guildId: Int): List<GuildChatEntity> = guildDao.getChats(guildId)

    suspend fun createGuild(guildName: String, description: String): String {
        val player = playerDao.getPlayer() ?: return "Player tidak ditemukan"

        if (guildDao.getMyGuildMember() != null) return "Anda sudah memiliki guild"

        val guildId = guildDao.insertGuild(
            GuildEntity(
                guildName = guildName.ifBlank { "New Guild" },
                description = description.ifBlank { "Spirit Card Guild" },
                leaderName = player.name
            )
        ).toInt()

        guildDao.insertMember(
            GuildMemberEntity(
                guildId = guildId,
                playerName = player.name,
                role = "Leader",
                power = calculatePlayerPower()
            )
        )

        guildDao.insertChat(
            GuildChatEntity(
                guildId = guildId,
                senderName = "System",
                message = "${player.name} membuat guild."
            )
        )

        touchSave()
        return "Guild berhasil dibuat"
    }

    suspend fun joinGuild(guild: GuildEntity): String {
        val player = playerDao.getPlayer() ?: return "Player tidak ditemukan"

        if (guildDao.getMyGuildMember() != null) return "Anda sudah bergabung di guild"

        guildDao.insertMember(
            GuildMemberEntity(
                guildId = guild.id,
                playerName = player.name,
                role = "Member",
                power = calculatePlayerPower()
            )
        )

        guildDao.increaseMember(guild.id)

        guildDao.insertChat(
            GuildChatEntity(
                guildId = guild.id,
                senderName = "System",
                message = "${player.name} bergabung ke guild."
            )
        )

        touchSave()
        return "Berhasil join ${guild.guildName}"
    }

    suspend fun leaveGuild(): String {
        val member = guildDao.getMyGuildMember() ?: return "Anda belum masuk guild"

        guildDao.decreaseMember(member.guildId)
        guildDao.leaveGuild()
        touchSave()

        return "Berhasil keluar dari guild"
    }

    suspend fun donateGuildGold(amount: Int): String {
        val player = playerDao.getPlayer() ?: return "Player tidak ditemukan"
        val member = guildDao.getMyGuildMember() ?: return "Anda belum masuk guild"

        if (amount <= 0) return "Nominal donasi tidak valid"
        if (player.gold < amount) return "Gold tidak cukup"

        playerDao.updatePlayer(
            player.copy(
                gold = player.gold - amount,
                lastSavedAt = System.currentTimeMillis()
            )
        )

        guildDao.addDonation(member.guildId, amount, amount / 10)
        guildDao.addMemberDonation(member.id, amount)
        guildDao.levelUpGuild(member.guildId, 1000)

        guildDao.insertChat(
            GuildChatEntity(
                guildId = member.guildId,
                senderName = player.name,
                message = "Donasi $amount Gold ke guild."
            )
        )

        addGuildMissionProgress()
        touchSave()

        return "Donasi berhasil"
    }

    suspend fun sendGuildChat(message: String): String {
        val player = playerDao.getPlayer() ?: return "Player tidak ditemukan"
        val member = guildDao.getMyGuildMember() ?: return "Anda belum masuk guild"

        if (message.isBlank()) return "Pesan tidak boleh kosong"

        guildDao.insertChat(
            GuildChatEntity(
                guildId = member.guildId,
                senderName = player.name,
                message = message
            )
        )

        return "Pesan terkirim"
    }

    suspend fun getTicketAmount(ticketName: String): Int {
        return inventoryDao.getItemByName(ticketName)?.quantity ?: 0
    }

    private suspend fun consumeTicket(ticketName: String): Boolean {
        val ticket = inventoryDao.getItemByName(ticketName) ?: return false
        if (ticket.quantity <= 0) return false

        val newQty = ticket.quantity - 1

        if (newQty <= 0) {
            inventoryDao.deleteItemById(ticket.id)
        } else {
            inventoryDao.updateItem(ticket.copy(quantity = newQty))
        }

        return true
    }

    private suspend fun summonByFixedRarity(rarity: String, ticketName: String): Pair<String, CardEntity?> {
        if (!consumeTicket(ticketName)) return Pair("$ticketName tidak tersedia di inventory", null)

        val cards = cardDao.getCardsByRarity(rarity)
        if (cards.isEmpty()) return Pair("Kartu rarity $rarity belum tersedia", null)

        val result = cards.random()

        cardDao.markOwned(result.id)
        addSummonMissionProgress()
        touchSave()

        return Pair(
            "Berhasil menggunakan $ticketName dan mendapatkan ${result.name} [$rarity]",
            result.copy(owned = true)
        )
    }

    suspend fun summonWithTicket(ticketName: String): Pair<String, CardEntity?> {
        return when (ticketName) {
            "Summon Ticket" -> summonNormalWithTicket()
            "Epic Ticket" -> summonByFixedRarity("Epic", "Epic Ticket")
            "Legendary Ticket" -> summonByFixedRarity("Legendary", "Legendary Ticket")
            else -> Pair("Ticket tidak dikenal", null)
        }
    }

    private suspend fun summonNormalWithTicket(): Pair<String, CardEntity?> {
        val player = playerDao.getPlayer() ?: return Pair("Player tidak ditemukan", null)

        if (!consumeTicket("Summon Ticket")) {
            return Pair("Summon Ticket tidak tersedia di inventory", null)
        }

        val rarity = com.pegasus.cardbattlerpg.ui.summon.SummonManager.rollRarity(
            legendaryPity = player.legendaryPity,
            epicPity = player.epicPity
        )

        val cards = cardDao.getCardsByRarity(rarity)
        if (cards.isEmpty()) return Pair("Kartu rarity $rarity belum tersedia", null)

        val result = cards.random()

        val newLegendaryPity = if (rarity == "Legendary") 0 else player.legendaryPity + 1
        val newEpicPity = if (rarity == "Legendary" || rarity == "Epic") 0 else player.epicPity + 1

        playerDao.updatePlayer(
            player.copy(
                legendaryPity = newLegendaryPity,
                epicPity = newEpicPity,
                lastSavedAt = System.currentTimeMillis()
            )
        )

        cardDao.markOwned(result.id)
        addSummonMissionProgress()
        touchSave()

        return Pair(
            "Berhasil menggunakan Summon Ticket dan mendapatkan ${result.name} [$rarity]",
            result.copy(owned = true)
        )
    }
}