package com.pegasus.cardbattlerpg.data

import com.pegasus.cardbattlerpg.entity.*

object SeedData {

    fun cards(): List<CardEntity> {
        return listOf(
            // LEGENDARY
            CardEntity(
                name = "Fire Dragon",
                title = "King of Inferno",
                description = "Naga legendaris penguasa api abadi.",
                element = "Fire",
                rarity = "Legendary",
                role = "Warrior",
                race = "Dragon",
                star = 5,
                attack = 95,
                defense = 45,
                hp = 180,
                mana = 8,
                speed = 120,
                criticalRate = 20,
                criticalDamage = 200,
                skillName = "Inferno Breath",
                skillDescription = "Memberikan damage besar ke semua musuh.",
                passiveSkillName = "Dragon Blood",
                passiveSkillDescription = "Meningkatkan attack sebesar 15%.",
                ultimateSkillName = "Apocalypse Flame",
                ultimateSkillDescription = "Menghancurkan musuh dengan api naga.",
                image = "card_fire_dragon",
                attackGif = "gif_fire_attack",
                pveRating = 98,
                pvpRating = 95,
                featuredBanner = true,
                dropRate = 0.5,
                owned = true
            ),

            CardEntity(
                name = "Thunder Emperor",
                title = "Lord of Storm",
                description = "Penguasa petir dari langit kuno.",
                element = "Lightning",
                rarity = "Legendary",
                role = "Mage",
                race = "Human",
                star = 5,
                attack = 102,
                defense = 48,
                hp = 170,
                mana = 8,
                speed = 130,
                criticalRate = 22,
                criticalDamage = 190,
                skillName = "Thunder Judgment",
                skillDescription = "Petir raksasa menyerang seluruh musuh.",
                ultimateSkillName = "Heavenly Storm",
                ultimateSkillDescription = "Badai petir menghantam arena.",
                image = "card_thunder_emperor",
                attackGif = "gif_thunder_attack",
                pveRating = 96,
                pvpRating = 97,
                dropRate = 0.5
            ),

            CardEntity(
                name = "Ice Phoenix",
                title = "Frozen Rebirth",
                description = "Phoenix es yang hidup kembali dari badai salju.",
                element = "Ice",
                rarity = "Legendary",
                role = "Support",
                race = "Beast",
                star = 5,
                attack = 88,
                defense = 65,
                hp = 190,
                mana = 8,
                speed = 115,
                skillName = "Frozen Rebirth",
                skillDescription = "Bangkit kembali setelah kalah.",
                passiveSkillName = "Ice Feather",
                passiveSkillDescription = "Mengurangi damage yang diterima.",
                image = "card_ice_phoenix",
                attackGif = "gif_ice_attack",
                pveRating = 94,
                pvpRating = 94,
                dropRate = 0.5
            ),

            CardEntity(
                name = "Void Reaper",
                title = "Soul Harvester",
                description = "Malaikat kematian dari dimensi kosong.",
                element = "Dark",
                rarity = "Legendary",
                role = "Assassin",
                race = "Undead",
                star = 5,
                attack = 105,
                defense = 40,
                hp = 175,
                mana = 9,
                speed = 145,
                criticalRate = 28,
                criticalDamage = 220,
                lifesteal = 10,
                skillName = "Soul Harvest",
                skillDescription = "Menyerap HP lawan.",
                image = "card_void_reaper",
                attackGif = "gif_void_attack",
                pveRating = 95,
                pvpRating = 98,
                dropRate = 0.4
            ),

            CardEntity(
                name = "Celestial Angel",
                title = "Divine Guardian",
                description = "Malaikat suci pelindung dunia cahaya.",
                element = "Light",
                rarity = "Legendary",
                role = "Support",
                race = "Angel",
                star = 5,
                attack = 82,
                defense = 82,
                hp = 210,
                mana = 8,
                skillName = "Divine Blessing",
                skillDescription = "Heal seluruh tim.",
                passiveSkillName = "Holy Aura",
                passiveSkillDescription = "Meningkatkan defense tim.",
                image = "card_celestial_angel",
                attackGif = "gif_celestial_attack",
                pveRating = 97,
                pvpRating = 93,
                dropRate = 0.4
            ),

            CardEntity(
                name = "Dragon King",
                title = "Supreme Flame Ruler",
                description = "Raja seluruh naga api.",
                element = "Fire",
                rarity = "Legendary",
                role = "Warrior",
                race = "Dragon",
                star = 6,
                attack = 120,
                defense = 60,
                hp = 260,
                mana = 10,
                speed = 125,
                criticalRate = 25,
                criticalDamage = 230,
                skillName = "King's Inferno",
                skillDescription = "Ultimate api penghancur.",
                ultimateSkillName = "Dragon Cataclysm",
                ultimateSkillDescription = "Menghancurkan semua musuh dengan api raja naga.",
                image = "card_dragon_king",
                attackGif = "gif_fire_attack",
                pveRating = 100,
                pvpRating = 100,
                featuredBanner = true,
                dropRate = 0.2
            ),

            // EPIC
            CardEntity(
                name = "Aqua Priestess",
                title = "Ocean Healer",
                description = "Pendeta air yang mampu menyembuhkan luka berat.",
                element = "Water",
                rarity = "Epic",
                role = "Support",
                race = "Human",
                star = 4,
                attack = 55,
                defense = 70,
                hp = 150,
                mana = 5,
                skillName = "Healing Wave",
                skillDescription = "Memulihkan HP hero.",
                image = "card_aqua_priestess",
                attackGif = "gif_aqua_attack",
                pveRating = 88,
                pvpRating = 82,
                owned = true
            ),

            CardEntity(
                name = "Shadow Assassin",
                title = "Silent Blade",
                description = "Pembunuh bayangan dengan serangan critical tinggi.",
                element = "Dark",
                rarity = "Epic",
                role = "Assassin",
                race = "Human",
                star = 4,
                attack = 88,
                defense = 35,
                hp = 120,
                mana = 4,
                speed = 140,
                criticalRate = 25,
                skillName = "Silent Strike",
                skillDescription = "Critical tinggi.",
                image = "card_shadow_assassin",
                attackGif = "gif_void_attack",
                pveRating = 86,
                pvpRating = 90,
                owned = true
            ),

            CardEntity(
                name = "Flame Samurai",
                title = "Burning Blade",
                description = "Samurai api dengan pedang membara.",
                element = "Fire",
                rarity = "Epic",
                role = "Warrior",
                race = "Human",
                star = 4,
                attack = 84,
                defense = 52,
                hp = 145,
                mana = 5,
                skillName = "Burning Slash",
                skillDescription = "Menimbulkan burn.",
                image = "card_flame_samurai",
                attackGif = "gif_fire_attack",
                pveRating = 84,
                pvpRating = 86
            ),

            CardEntity(
                name = "Crystal Knight",
                title = "Shield of Crystal",
                description = "Ksatria kristal dengan pertahanan tinggi.",
                element = "Ice",
                rarity = "Epic",
                role = "Tank",
                race = "Human",
                star = 4,
                attack = 74,
                defense = 80,
                hp = 185,
                mana = 5,
                blockRate = 12,
                skillName = "Crystal Barrier",
                skillDescription = "Shield kristal.",
                image = "card_crystal_knight",
                attackGif = "gif_ice_attack",
                pveRating = 88,
                pvpRating = 84
            ),

            CardEntity(
                name = "Sea Emperor",
                title = "Ruler of Tides",
                description = "Penguasa laut yang mengendalikan tsunami.",
                element = "Water",
                rarity = "Epic",
                role = "Mage",
                race = "Human",
                star = 4,
                attack = 85,
                defense = 68,
                hp = 180,
                mana = 6,
                skillName = "Tsunami",
                skillDescription = "Damage area air.",
                image = "card_sea_emperor",
                attackGif = "gif_aqua_attack",
                pveRating = 90,
                pvpRating = 85
            ),

            // RARE
            CardEntity(
                name = "Forest Guardian",
                title = "Protector of Grove",
                description = "Penjaga hutan dengan kekuatan alam.",
                element = "Nature",
                rarity = "Rare",
                role = "Tank",
                race = "Beast",
                star = 3,
                attack = 60,
                defense = 80,
                hp = 170,
                mana = 6,
                skillName = "Nature Shield",
                skillDescription = "Meningkatkan defense.",
                image = "card_forest_guardian",
                attackGif = "gif_forest_attack",
                pveRating = 75,
                pvpRating = 72,
                owned = true
            ),

            CardEntity(
                name = "Holy Knight",
                title = "Sword of Light",
                description = "Ksatria suci pembawa cahaya.",
                element = "Light",
                rarity = "Rare",
                role = "Warrior",
                race = "Human",
                star = 3,
                attack = 70,
                defense = 75,
                hp = 160,
                mana = 5,
                skillName = "Divine Slash",
                skillDescription = "Serangan cahaya.",
                image = "card_holy_knight",
                attackGif = "gif_celestial_attack",
                pveRating = 78,
                pvpRating = 76,
                owned = true
            ),

            CardEntity(
                name = "Lava Warrior",
                element = "Fire",
                rarity = "Rare",
                role = "Warrior",
                race = "Human",
                star = 3,
                attack = 68,
                defense = 50,
                hp = 145,
                mana = 4,
                skillName = "Lava Smash",
                skillDescription = "Damage api sedang.",
                image = "card_lava_warrior",
                attackGif = "gif_fire_attack",
                pveRating = 72,
                pvpRating = 70
            ),

            CardEntity(
                name = "Thunder Mage",
                element = "Lightning",
                rarity = "Rare",
                role = "Mage",
                race = "Human",
                star = 3,
                attack = 75,
                defense = 35,
                hp = 110,
                mana = 5,
                skillName = "Lightning Bolt",
                skillDescription = "Petir tunggal.",
                image = "card_thunder_mage",
                attackGif = "gif_thunder_attack",
                pveRating = 76,
                pvpRating = 74
            ),

            CardEntity(
                name = "Stone Golem",
                element = "Earth",
                rarity = "Rare",
                role = "Tank",
                race = "Golem",
                star = 3,
                attack = 58,
                defense = 88,
                hp = 200,
                mana = 3,
                blockRate = 10,
                skillName = "Rock Armor",
                skillDescription = "Defense besar.",
                image = "card_stone_golem",
                attackGif = "gif_earth_attack",
                pveRating = 80,
                pvpRating = 73
            ),

            // COMMON
            CardEntity(
                name = "Fire Imp",
                element = "Fire",
                rarity = "Common",
                role = "Mage",
                race = "Demon",
                star = 1,
                attack = 35,
                defense = 15,
                hp = 90,
                mana = 2,
                skillName = "Small Flame",
                skillDescription = "Serangan api kecil.",
                attackGif = "gif_fire_attack",
                image = "card_fire_imp"

            ),

            CardEntity(
                name = "Water Sprite",
                element = "Water",
                rarity = "Common",
                role = "Support",
                race = "Fairy",
                star = 1,
                attack = 28,
                defense = 22,
                hp = 100,
                mana = 2,
                skillName = "Water Shot",
                skillDescription = "Serangan air kecil.",
                attackGif = "gif_aqua_attack",
                image = "card_water_sprite"
            ),

            CardEntity(
                name = "Earth Goblin",
                element = "Nature",
                rarity = "Common",
                role = "Warrior",
                race = "Goblin",
                star = 1,
                attack = 32,
                defense = 25,
                hp = 110,
                mana = 2,
                skillName = "Stone Hit",
                skillDescription = "Pukulan batu sederhana.",
                attackGif = "gif_forest_attack",
                image = "card_earth_goblin"
            ),

            CardEntity(
                name = "Skeleton Soldier",
                element = "Dark",
                rarity = "Common",
                role = "Warrior",
                race = "Undead",
                star = 1,
                attack = 34,
                defense = 20,
                hp = 100,
                mana = 2,
                skillName = "Bone Slash",
                skillDescription = "Serangan tulang.",
                attackGif = "gif_void_attack",
                image = "card_skeleton_soldier"
            ),
            CardEntity(
                name = "Ancient Titan",
                title = "Mountain Breaker",
                description = "Titan kuno yang lahir dari inti bumi.",
                element = "Earth",
                rarity = "Legendary",
                role = "Tank",
                race = "Giant",
                star = 5,
                attack = 90,
                defense = 95,
                hp = 260,
                mana = 9,
                speed = 80,
                blockRate = 20,
                skillName = "Earth Collapse",
                skillDescription = "Menghancurkan pertahanan musuh.",
                ultimateSkillName = "Titan Quake",
                ultimateSkillDescription = "Gempa besar menyerang seluruh arena.",
                image = "card_ancient_titan",
                attackGif = "gif_earth_attack",
                pveRating = 96,
                pvpRating = 92,
                dropRate = 0.4
            ),

            CardEntity(
                name = "Storm Leviathan",
                title = "Ocean Calamity",
                description = "Monster laut raksasa pembawa badai.",
                element = "Water",
                rarity = "Legendary",
                role = "Mage",
                race = "Beast",
                star = 5,
                attack = 98,
                defense = 70,
                hp = 240,
                mana = 8,
                speed = 105,
                skillName = "Ocean Wrath",
                skillDescription = "Gelombang besar menghantam semua musuh.",
                ultimateSkillName = "Abyssal Storm",
                ultimateSkillDescription = "Badai laut menghancurkan pertahanan musuh.",
                image = "card_storm_leviathan",
                attackGif = "gif_aqua_attack",
                pveRating = 97,
                pvpRating = 94,
                dropRate = 0.4
            ),

            CardEntity(
                name = "Dark Necromancer",
                title = "Caller of Bones",
                description = "Penyihir gelap yang memanggil prajurit tulang.",
                element = "Dark",
                rarity = "Epic",
                role = "Mage",
                race = "Human",
                star = 4,
                attack = 72,
                defense = 45,
                hp = 135,
                mana = 6,
                speed = 95,
                skillName = "Raise Skeleton",
                skillDescription = "Memanggil minion.",
                passiveSkillName = "Dark Ritual",
                passiveSkillDescription = "Meningkatkan damage dark.",
                image = "card_dark_necromancer",
                attackGif = "gif_void_attack",
                pveRating = 83,
                pvpRating = 87
            ),

            CardEntity(
                name = "Moon Witch",
                title = "Lunar Spellcaster",
                description = "Penyihir bulan yang menguasai sihir cahaya malam.",
                element = "Light",
                rarity = "Epic",
                role = "Mage",
                race = "Human",
                star = 4,
                attack = 78,
                defense = 48,
                hp = 140,
                mana = 6,
                speed = 110,
                skillName = "Moon Beam",
                skillDescription = "Magic damage besar.",
                passiveSkillName = "Lunar Blessing",
                passiveSkillDescription = "Mana bertambah saat battle.",
                image = "card_moon_witch",
                attackGif = "gif_celestial_attack",
                pveRating = 84,
                pvpRating = 85
            ),

            CardEntity(
                name = "Thunder Hunter",
                title = "Storm Archer",
                description = "Pemburu petir dengan panah listrik.",
                element = "Lightning",
                rarity = "Epic",
                role = "Archer",
                race = "Human",
                star = 4,
                attack = 90,
                defense = 40,
                hp = 130,
                mana = 5,
                speed = 135,
                criticalRate = 20,
                skillName = "Volt Arrow",
                skillDescription = "Serangan petir cepat.",
                image = "card_thunder_hunter",
                attackGif = "gif_thunder_attack",
                pveRating = 86,
                pvpRating = 89
            ),

            CardEntity(
                name = "Phoenix Guard",
                title = "Flame Defender",
                description = "Penjaga phoenix dengan perisai api.",
                element = "Fire",
                rarity = "Epic",
                role = "Tank",
                race = "Human",
                star = 4,
                attack = 81,
                defense = 72,
                hp = 175,
                mana = 5,
                speed = 95,
                blockRate = 12,
                skillName = "Phoenix Shield",
                skillDescription = "Shield api.",
                passiveSkillName = "Burning Guard",
                passiveSkillDescription = "Mengurangi damage api.",
                image = "card_phoenix_guard",
                attackGif = "gif_fire_attack",
                pveRating = 87,
                pvpRating = 83
            ),

            CardEntity(
                name = "Forest Spirit Queen",
                title = "Queen of Grove",
                description = "Ratu roh hutan yang memberi berkah alam.",
                element = "Nature",
                rarity = "Epic",
                role = "Support",
                race = "Spirit",
                star = 4,
                attack = 73,
                defense = 76,
                hp = 190,
                mana = 6,
                speed = 100,
                skillName = "Nature Blessing",
                skillDescription = "Heal dan buff tim.",
                passiveSkillName = "Forest Aura",
                passiveSkillDescription = "Meningkatkan HP tim nature.",
                image = "card_forest_spirit_queen",
                attackGif = "gif_forest_attack",
                pveRating = 89,
                pvpRating = 84
            ),

            CardEntity(
                name = "Ice Archer",
                title = "Frozen Shot",
                description = "Pemanah es yang dapat memperlambat musuh.",
                element = "Ice",
                rarity = "Rare",
                role = "Archer",
                race = "Human",
                star = 3,
                attack = 64,
                defense = 42,
                hp = 120,
                mana = 4,
                speed = 120,
                skillName = "Frozen Arrow",
                skillDescription = "Slow musuh.",
                image = "card_ice_archer",
                attackGif = "gif_ice_attack",
                pveRating = 70,
                pvpRating = 74
            ),

            CardEntity(
                name = "Water Knight",
                title = "Blue Shield",
                description = "Ksatria air dengan pertahanan stabil.",
                element = "Water",
                rarity = "Rare",
                role = "Tank",
                race = "Human",
                star = 3,
                attack = 62,
                defense = 72,
                hp = 165,
                mana = 4,
                blockRate = 8,
                skillName = "Water Shield",
                skillDescription = "Shield air.",
                image = "card_water_knight",
                attackGif = "gif_aqua_attack",
                pveRating = 74,
                pvpRating = 72
            ),

            CardEntity(
                name = "Dark Hunter",
                title = "Night Stalker",
                description = "Pemburu malam dengan serangan critical.",
                element = "Dark",
                rarity = "Rare",
                role = "Archer",
                race = "Human",
                star = 3,
                attack = 72,
                defense = 40,
                hp = 125,
                mana = 4,
                speed = 125,
                criticalRate = 16,
                skillName = "Night Arrow",
                skillDescription = "Critical meningkat.",
                image = "card_dark_hunter",
                attackGif = "gif_void_attack",
                pveRating = 73,
                pvpRating = 78
            ),

            CardEntity(
                name = "Wind Ranger",
                title = "Cyclone Runner",
                description = "Ranger angin dengan gerakan cepat.",
                element = "Wind",
                rarity = "Rare",
                role = "Archer",
                race = "Elf",
                star = 3,
                attack = 66,
                defense = 44,
                hp = 118,
                mana = 4,
                speed = 135,
                dodge = 8,
                skillName = "Cyclone Shot",
                skillDescription = "Serangan angin.",
                image = "card_wind_ranger",
                attackGif = "gif_wind_attack",
                pveRating = 72,
                pvpRating = 76
            ),

            CardEntity(
                name = "Elf Priest",
                title = "Forest Healer",
                description = "Pendeta elf dengan kekuatan penyembuhan.",
                element = "Nature",
                rarity = "Rare",
                role = "Support",
                race = "Elf",
                star = 3,
                attack = 45,
                defense = 62,
                hp = 145,
                mana = 5,
                speed = 90,
                skillName = "Nature Heal",
                skillDescription = "Memulihkan HP.",
                image = "card_elf_priest",
                attackGif = "gif_forest_attack",
                pveRating = 76,
                pvpRating = 70
            ),

            CardEntity(
                name = "Young Wolf",
                title = "Forest Cub",
                description = "Serigala muda dari hutan kuno.",
                element = "Nature",
                rarity = "Common",
                role = "Warrior",
                race = "Beast",
                star = 1,
                attack = 30,
                defense = 18,
                hp = 95,
                mana = 2,
                speed = 105,
                skillName = "Bite",
                skillDescription = "Gigitan sederhana.",
                attackGif = "gif_forest_attack",
                image = "card_young_wolf"
            ),

            CardEntity(
                name = "Tiny Fairy",
                title = "Little Light",
                description = "Peri kecil pembawa cahaya lembut.",
                element = "Light",
                rarity = "Common",
                role = "Support",
                race = "Fairy",
                star = 1,
                attack = 24,
                defense = 24,
                hp = 90,
                mana = 2,
                speed = 115,
                skillName = "Light Spark",
                skillDescription = "Percikan cahaya.",
                attackGif = "gif_celestial_attack",
                image = "card_tiny_fairy"
            ),

            CardEntity(
                name = "Wind Pixie",
                title = "Small Breeze",
                description = "Pixie angin dengan gerakan lincah.",
                element = "Wind",
                rarity = "Common",
                role = "Mage",
                race = "Fairy",
                star = 1,
                attack = 27,
                defense = 20,
                hp = 92,
                mana = 2,
                speed = 125,
                dodge = 5,
                skillName = "Wind Gust",
                skillDescription = "Angin kecil.",
                attackGif = "gif_wind_attack",
                image = "card_wind_pixie"
            ),

            CardEntity(
                name = "Rock Beetle",
                title = "Stone Shell",
                description = "Kumbang batu dengan cangkang keras.",
                element = "Earth",
                rarity = "Common",
                role = "Tank",
                race = "Beast",
                star = 1,
                attack = 26,
                defense = 35,
                hp = 115,
                mana = 1,
                blockRate = 5,
                skillName = "Shell Guard",
                skillDescription = "Defense meningkat.",
                attackGif = "gif_earth_attack",
                image = "card_rock_beetle"
            )
        )
    }

    fun heroes(): List<HeroEntity> {
        return listOf(

            // ==========================
            // STARTER HERO
            // ==========================

            HeroEntity(
                name = "Arka",
                element = "Light",
                level = 1,
                exp = 0,
                hp = 500,
                attack = 60,
                defense = 40,
                image = "hero_arka",
                selected = true
            ),

            HeroEntity(
                name = "Lyra",
                element = "Water",
                level = 1,
                exp = 0,
                hp = 420,
                attack = 70,
                defense = 35,
                image = "hero_lyra",
                selected = false
            ),

            // ==========================
            // FIRE
            // ==========================

            HeroEntity(
                name = "Ignis",
                element = "Fire",
                level = 1,
                exp = 0,
                hp = 620,
                attack = 95,
                defense = 40,
                image = "hero_ignis"
            ),

            HeroEntity(
                name = "Blaze Knight",
                element = "Fire",
                level = 1,
                exp = 0,
                hp = 750,
                attack = 88,
                defense = 60,
                image = "hero_blaze_knight"
            ),

            HeroEntity(
                name = "Phoenix Queen",
                element = "Fire",
                level = 1,
                exp = 0,
                hp = 900,
                attack = 120,
                defense = 75,
                image = "hero_phoenix_queen"
            ),

            // ==========================
            // WATER
            // ==========================

            HeroEntity(
                name = "Aqua Saint",
                element = "Water",
                level = 1,
                exp = 0,
                hp = 680,
                attack = 80,
                defense = 55,
                image = "hero_aqua_saint"
            ),

            HeroEntity(
                name = "Tidal Guardian",
                element = "Water",
                level = 1,
                exp = 0,
                hp = 850,
                attack = 72,
                defense = 85,
                image = "hero_tidal_guardian"
            ),

            HeroEntity(
                name = "Ocean Empress",
                element = "Water",
                level = 1,
                exp = 0,
                hp = 980,
                attack = 110,
                defense = 90,
                image = "hero_ocean_empress"
            ),

            // ==========================
            // NATURE
            // ==========================

            HeroEntity(
                name = "Sylvana",
                element = "Nature",
                level = 1,
                exp = 0,
                hp = 720,
                attack = 82,
                defense = 68,
                image = "hero_sylvana"
            ),

            HeroEntity(
                name = "Forest Archer",
                element = "Nature",
                level = 1,
                exp = 0,
                hp = 580,
                attack = 105,
                defense = 40,
                image = "hero_forest_archer"
            ),

            HeroEntity(
                name = "World Tree Guardian",
                element = "Nature",
                level = 1,
                exp = 0,
                hp = 1200,
                attack = 125,
                defense = 110,
                image = "hero_world_tree_guardian"
            ),

            // ==========================
            // DARK
            // ==========================

            HeroEntity(
                name = "Shadow Reaper",
                element = "Dark",
                level = 1,
                exp = 0,
                hp = 650,
                attack = 115,
                defense = 45,
                image = "hero_shadow_reaper"
            ),

            HeroEntity(
                name = "Dark Assassin",
                element = "Dark",
                level = 1,
                exp = 0,
                hp = 540,
                attack = 135,
                defense = 30,
                image = "hero_dark_assassin"
            ),

            HeroEntity(
                name = "Abyss Lord",
                element = "Dark",
                level = 1,
                exp = 0,
                hp = 1450,
                attack = 180,
                defense = 95,
                image = "hero_abyss_lord"
            ),

            // ==========================
            // LIGHT
            // ==========================

            HeroEntity(
                name = "Holy Priestess",
                element = "Light",
                level = 1,
                exp = 0,
                hp = 600,
                attack = 70,
                defense = 55,
                image = "hero_holy_priestess"
            ),

            HeroEntity(
                name = "Divine Paladin",
                element = "Light",
                level = 1,
                exp = 0,
                hp = 1100,
                attack = 110,
                defense = 120,
                image = "hero_divine_paladin"
            ),

            HeroEntity(
                name = "Celestial Angel",
                element = "Light",
                level = 1,
                exp = 0,
                hp = 1350,
                attack = 160,
                defense = 115,
                image = "hero_celestial_angel"
            ),

            // ==========================
            // THUNDER
            // ==========================

            HeroEntity(
                name = "Volt Hunter",
                element = "Thunder",
                level = 1,
                exp = 0,
                hp = 620,
                attack = 118,
                defense = 42,
                image = "hero_volt_hunter"
            ),

            HeroEntity(
                name = "Thunder Samurai",
                element = "Thunder",
                level = 1,
                exp = 0,
                hp = 850,
                attack = 135,
                defense = 65,
                image = "hero_thunder_samurai"
            ),

            HeroEntity(
                name = "Storm Emperor",
                element = "Thunder",
                level = 1,
                exp = 0,
                hp = 1550,
                attack = 190,
                defense = 105,
                image = "hero_storm_emperor"
            )
        ).map { hero ->
            when (hero.name) {
                "Arka" -> hero.copy(unlocked = true, selected = true, rarity = "Rare", role = "Warrior")
                "Lyra" -> hero.copy(unlocked = false, selected = false, rarity = "Rare", role = "Support")
                "Phoenix Queen", "Ocean Empress", "Earth Titan", "Shadow Reaper", "Celestial Angel", "Storm Emperor" -> hero.copy(unlocked = false, selected = false, rarity = "Legendary")
                "Blaze Knight", "Tidal Guardian", "Forest Druid", "Night Assassin", "Divine Paladin", "Thunder Samurai" -> hero.copy(unlocked = false, selected = false, rarity = "Epic")
                else -> hero.copy(unlocked = false, selected = false, rarity = "Rare")
            }
        }
    }

    fun equipments(): List<EquipmentEntity> {
        return listOf(

            // WEAPON
            EquipmentEntity(name = "Bronze Sword", type = "Weapon", rarity = "Common", attackBonus = 15, defenseBonus = 0, hpBonus = 0, image = "eq_bronze_sword", equipped = true, unlocked = true, unlockPriceGold = 0),
            EquipmentEntity(name = "Iron Sword", type = "Weapon", rarity = "Rare", attackBonus = 35, defenseBonus = 0, hpBonus = 20, image = "eq_iron_sword", unlocked = false, unlockPriceGold = 1500),
            EquipmentEntity(name = "Flame Blade", type = "Weapon", rarity = "Epic", attackBonus = 75, defenseBonus = 5, hpBonus = 40, image = "eq_flame_blade", unlocked = false, unlockPriceGold = 5000),
            EquipmentEntity(name = "Dragon Slayer", type = "Weapon", rarity = "Legendary", attackBonus = 140, defenseBonus = 15, hpBonus = 120, image = "eq_dragon_slayer", unlocked = false, unlockPriceGold = 15000),

            // ARMOR
            EquipmentEntity(name = "Apprentice Armor", type = "Armor", rarity = "Common", attackBonus = 0, defenseBonus = 12, hpBonus = 50, image = "eq_apprentice_armor", equipped = true, unlocked = true, unlockPriceGold = 0),
            EquipmentEntity(name = "Knight Armor", type = "Armor", rarity = "Rare", attackBonus = 0, defenseBonus = 35, hpBonus = 160, image = "eq_knight_armor", unlocked = false, unlockPriceGold = 1800),
            EquipmentEntity(name = "Crystal Armor", type = "Armor", rarity = "Epic", attackBonus = 10, defenseBonus = 75, hpBonus = 320, image = "eq_crystal_armor", unlocked = false, unlockPriceGold = 6500),
            EquipmentEntity(name = "Celestial Armor", type = "Armor", rarity = "Legendary", attackBonus = 25, defenseBonus = 135, hpBonus = 600, image = "eq_celestial_armor", unlocked = false, unlockPriceGold = 20000),

            // RING
            EquipmentEntity(name = "Magic Ring", type = "Ring", rarity = "Rare", attackBonus = 8, defenseBonus = 5, hpBonus = 30, image = "eq_magic_ring", unlocked = false, unlockPriceGold = 1200),
            EquipmentEntity(name = "Ring of Flame", type = "Ring", rarity = "Epic", attackBonus = 45, defenseBonus = 10, hpBonus = 80, image = "eq_ring_of_flame", unlocked = false, unlockPriceGold = 4500),
            EquipmentEntity(name = "Abyss Ring", type = "Ring", rarity = "Legendary", attackBonus = 85, defenseBonus = 25, hpBonus = 160, image = "eq_abyss_ring", unlocked = false, unlockPriceGold = 14000),

            // BOOTS
            EquipmentEntity(name = "Leather Boots", type = "Boots", rarity = "Common", attackBonus = 0, defenseBonus = 6, hpBonus = 25, image = "eq_leather_boots", unlocked = true, unlockPriceGold = 0),
            EquipmentEntity(name = "Wind Boots", type = "Boots", rarity = "Rare", attackBonus = 12, defenseBonus = 18, hpBonus = 60, image = "eq_wind_boots", unlocked = false, unlockPriceGold = 1300),
            EquipmentEntity(name = "Thunder Boots", type = "Boots", rarity = "Epic", attackBonus = 30, defenseBonus = 35, hpBonus = 100, image = "eq_thunder_boots", unlocked = false, unlockPriceGold = 4200),

            // AMULET
            EquipmentEntity(name = "Guardian Amulet", type = "Amulet", rarity = "Rare", attackBonus = 5, defenseBonus = 20, hpBonus = 120, image = "eq_guardian_amulet", unlocked = false, unlockPriceGold = 2500),
            EquipmentEntity(name = "Phoenix Amulet", type = "Amulet", rarity = "Epic", attackBonus = 35, defenseBonus = 35, hpBonus = 220, image = "eq_phoenix_amulet", unlocked = false, unlockPriceGold = 7000),
            EquipmentEntity(name = "Divine Amulet", type = "Amulet", rarity = "Legendary", attackBonus = 70, defenseBonus = 70, hpBonus = 420, image = "eq_divine_amulet", unlocked = false, unlockPriceGold = 18000)
        )
    }

    fun inventory(): List<InventoryEntity> {
        return listOf(

            InventoryEntity(
                itemName = "Small Potion",
                itemType = "Consumable",
                image = "item_small_potion",
                quantity = 5,
                description = "Memulihkan sedikit HP hero saat battle.",
                rarity = "Common",
                badge = "NEW",
                source = "Starter",
                sellGold = 150,
                glowColor = "#6CFF9B"
            ),

            InventoryEntity(
                itemName = "Large Potion",
                itemType = "Consumable",
                image = "item_large_potion",
                quantity = 2,
                description = "Memulihkan HP hero dalam jumlah besar.",
                rarity = "Rare",
                source = "Starter",
                sellGold = 400,
                glowColor = "#4FC3F7"
            ),

            InventoryEntity(
                itemName = "Mega Potion",
                itemType = "Consumable",
                image = "item_mega_potion",
                quantity = 1,
                description = "Potion premium untuk memulihkan HP sangat besar.",
                rarity = "Epic",
                badge = "HOT",
                source = "Starter",
                sellGold = 1000,
                glowColor = "#B56CFF"
            ),

            InventoryEntity(
                itemName = "Summon Ticket",
                itemType = "Ticket",
                image = "item_summon_ticket",
                quantity = 3,
                description = "Ticket untuk melakukan summon kartu spirit.",
                rarity = "Rare",
                source = "Starter",
                sellGold = 0,
                glowColor = "#FFD66B"
            ),

            InventoryEntity(
                itemName = "Epic Ticket",
                itemType = "Ticket",
                image = "item_epic_ticket",
                quantity = 1,
                description = "Ticket dengan peluang tinggi mendapatkan kartu Epic.",
                rarity = "Epic",
                badge = "HOT",
                source = "Starter",
                sellGold = 0,
                glowColor = "#B56CFF"
            ),

            InventoryEntity(
                itemName = "Legendary Ticket",
                itemType = "Ticket",
                image = "item_legend_ticket",
                quantity = 0,
                description = "Ticket langka untuk summon kartu Epic atau Legendary.",
                rarity = "Legendary",
                badge = "LIMITED",
                source = "Shop",
                sellGold = 0,
                glowColor = "#FFD700"
            ),

            InventoryEntity(
                itemName = "Fire Crystal",
                itemType = "Material",
                image = "item_fire_crystal",
                quantity = 10,
                description = "Material elemen api untuk upgrade hero dan kartu Fire.",
                rarity = "Rare",
                source = "Battle",
                sellGold = 250,
                glowColor = "#FF7043"
            ),

            InventoryEntity(
                itemName = "Water Crystal",
                itemType = "Material",
                image = "item_water_crystal",
                quantity = 8,
                description = "Material elemen air untuk upgrade hero dan kartu Water.",
                rarity = "Rare",
                source = "Battle",
                sellGold = 250,
                glowColor = "#4FC3F7"
            ),

            InventoryEntity(
                itemName = "Wind Crystal",
                itemType = "Material",
                image = "item_wind_crystal",
                quantity = 7,
                description = "Material elemen angin untuk upgrade hero dan kartu Wind.",
                rarity = "Rare",
                source = "Battle",
                sellGold = 250,
                glowColor = "#66BB6A"
            ),

            InventoryEntity(
                itemName = "Dark Crystal",
                itemType = "Material",
                image = "item_dark_crystal",
                quantity = 3,
                description = "Material gelap untuk upgrade hero dan kartu Dark.",
                rarity = "Epic",
                source = "Battle",
                sellGold = 400,
                glowColor = "#9C27B0"
            ),

            InventoryEntity(
                itemName = "Light Crystal",
                itemType = "Material",
                image = "item_light_crystal",
                quantity = 2,
                description = "Material cahaya untuk upgrade hero dan kartu Light.",
                rarity = "Epic",
                source = "Battle",
                sellGold = 400,
                glowColor = "#FFD54F"
            ),

            InventoryEntity(
                itemName = "Iron Ore",
                itemType = "Material",
                image = "item_iron_ore",
                quantity = 15,
                description = "Material dasar untuk upgrade equipment.",
                rarity = "Common",
                source = "Battle",
                sellGold = 300,
                glowColor = "#C9C9C9"
            ),

            InventoryEntity(
                itemName = "Mythril Ore",
                itemType = "Material",
                image = "item_mythril_ore",
                quantity = 4,
                description = "Ore langka untuk upgrade equipment tingkat tinggi.",
                rarity = "Epic",
                source = "Battle",
                sellGold = 1250,
                glowColor = "#B56CFF"
            ),

            InventoryEntity(
                itemName = "EXP Book",
                itemType = "Upgrade",
                image = "item_exp_book",
                quantity = 5,
                description = "Buku EXP untuk menaikkan level hero.",
                rarity = "Rare",
                source = "Quest",
                sellGold = 600,
                glowColor = "#4FC3F7"
            ),

            InventoryEntity(
                itemName = "Hero Scroll",
                itemType = "Upgrade",
                image = "item_hero_scroll",
                quantity = 2,
                description = "Scroll kuno untuk membuka potensi hero.",
                rarity = "Epic",
                source = "Quest",
                sellGold = 1100,
                glowColor = "#B56CFF"
            ),

            InventoryEntity(
                itemName = "Hero Shard",
                itemType = "Upgrade",
                image = "item_hero_shard",
                quantity = 25,
                description = "Shard untuk unlock atau upgrade hero spesial.",
                rarity = "Epic",
                badge = "HOT",
                source = "Event",
                sellGold = 0,
                glowColor = "#B56CFF"
            ),

            InventoryEntity(
                itemName = "Awaken Stone",
                itemType = "Upgrade",
                image = "item_awaken_stone",
                quantity = 1,
                description = "Batu awakening untuk meningkatkan kekuatan hero.",
                rarity = "Legendary",
                badge = "RARE",
                source = "Event",
                sellGold = 0,
                glowColor = "#FFD700"
            ),

            InventoryEntity(
                itemName = "Bronze Chest",
                itemType = "Chest",
                image = "item_bronze_chest",
                quantity = 2,
                description = "Chest sederhana berisi gold, material, atau potion.",
                rarity = "Common",
                source = "Battle",
                sellGold = 750,
                glowColor = "#CD7F32"
            ),

            InventoryEntity(
                itemName = "Silver Chest",
                itemType = "Chest",
                image = "item_silver_chest",
                quantity = 1,
                description = "Chest berisi reward Rare atau material upgrade.",
                rarity = "Rare",
                source = "Battle",
                sellGold = 2000,
                glowColor = "#C0C0C0"
            ),

            InventoryEntity(
                itemName = "Gold Chest",
                itemType = "Chest",
                image = "item_gold_chest",
                quantity = 0,
                description = "Chest premium dengan peluang reward Epic.",
                rarity = "Epic",
                badge = "BEST",
                source = "Shop",
                sellGold = 5000,
                glowColor = "#FFD700"
            ),

            InventoryEntity(
                itemName = "Legendary Chest",
                itemType = "Chest",
                image = "item_legendary_chest",
                quantity = 0,
                description = "Chest spesial dengan peluang item Legendary.",
                rarity = "Legendary",
                badge = "LIMITED",
                source = "Shop",
                sellGold = 0,
                glowColor = "#FFD700"
            ),

            InventoryEntity(
                itemName = "Diamond Pack S",
                itemType = "Diamond",
                image = "item_diamond_pack_small",
                quantity = 0,
                description = "Paket kecil diamond untuk summon dan shop premium.",
                rarity = "Rare",
                source = "Shop",
                sellGold = 0,
                glowColor = "#7DE7FF"
            ),

            InventoryEntity(
                itemName = "Diamond Pack M",
                itemType = "Diamond",
                image = "item_diamond_pack_medium",
                quantity = 0,
                description = "Paket medium diamond dengan value lebih baik.",
                rarity = "Epic",
                badge = "VALUE",
                source = "Shop",
                sellGold = 0,
                glowColor = "#4FC3F7"
            ),

            InventoryEntity(
                itemName = "Diamond Pack L",
                itemType = "Diamond",
                image = "item_diamond_pack_large",
                quantity = 0,
                description = "Paket besar diamond terbaik untuk player aktif.",
                rarity = "Legendary",
                badge = "BEST",
                source = "Shop",
                sellGold = 0,
                glowColor = "#FFD700"
            )
        )
    }

    fun missions(): List<MissionEntity> {
        return listOf(

            // DAILY
            MissionEntity(
                title = "First Battle",
                description = "Menangkan 1 battle.",
                type = "Daily",
                target = 1,
                progress = 0,
                rewardGold = 500,
                rewardDiamond = 10
            ),

            MissionEntity(
                title = "Summon Spirit",
                description = "Lakukan summon 1 kali.",
                type = "Daily",
                target = 1,
                progress = 0,
                rewardGold = 300,
                rewardDiamond = 5
            ),

            MissionEntity(
                title = "Buy Item",
                description = "Beli 1 item dari Shop.",
                type = "Daily",
                target = 1,
                progress = 0,
                rewardGold = 350,
                rewardDiamond = 5
            ),

            MissionEntity(
                title = "Use Potion",
                description = "Gunakan 1 potion dari inventory.",
                type = "Daily",
                target = 1,
                progress = 0,
                rewardGold = 250,
                rewardDiamond = 3
            ),

            MissionEntity(
                title = "Guild Donation",
                description = "Donasi gold ke guild 1 kali.",
                type = "Daily",
                target = 1,
                progress = 0,
                rewardGold = 400,
                rewardDiamond = 5
            ),

            // MAIN
            MissionEntity(
                title = "Card Collector",
                description = "Miliki 5 kartu.",
                type = "Main",
                target = 5,
                progress = 5,
                rewardGold = 1000,
                rewardDiamond = 20,
                completed = true
            ),

            MissionEntity(
                title = "Hero Training",
                description = "Naikkan level hero 1 kali.",
                type = "Main",
                target = 1,
                progress = 0,
                rewardGold = 1200,
                rewardDiamond = 25
            ),

            MissionEntity(
                title = "Equipment Buyer",
                description = "Buka 1 equipment terkunci.",
                type = "Main",
                target = 1,
                progress = 0,
                rewardGold = 1500,
                rewardDiamond = 20
            ),

            MissionEntity(
                title = "Story Beginner",
                description = "Selesaikan 3 stage story.",
                type = "Main",
                target = 3,
                progress = 0,
                rewardGold = 2000,
                rewardDiamond = 35
            ),

            MissionEntity(
                title = "Deck Builder",
                description = "Tambahkan 5 kartu ke deck.",
                type = "Main",
                target = 5,
                progress = 0,
                rewardGold = 1500,
                rewardDiamond = 20
            ),

            // WEEKLY
            MissionEntity(
                title = "Battle Warrior",
                description = "Menangkan 10 battle.",
                type = "Weekly",
                target = 10,
                progress = 0,
                rewardGold = 5000,
                rewardDiamond = 80
            ),

            MissionEntity(
                title = "Summon Master",
                description = "Lakukan summon 10 kali.",
                type = "Weekly",
                target = 10,
                progress = 0,
                rewardGold = 4500,
                rewardDiamond = 70
            ),

            MissionEntity(
                title = "Shop Lover",
                description = "Beli 5 item dari Shop.",
                type = "Weekly",
                target = 5,
                progress = 0,
                rewardGold = 3500,
                rewardDiamond = 50
            ),

            MissionEntity(
                title = "Guild Supporter",
                description = "Donasi gold ke guild 5 kali.",
                type = "Weekly",
                target = 5,
                progress = 0,
                rewardGold = 4000,
                rewardDiamond = 60
            ),

            // EVENT
            MissionEntity(
                title = "Legend Hunter",
                description = "Dapatkan 1 kartu Legendary.",
                type = "Event",
                target = 1,
                progress = 0,
                rewardGold = 10000,
                rewardDiamond = 150
            ),

            MissionEntity(
                title = "Crystal Hunter",
                description = "Kumpulkan 20 crystal material.",
                type = "Event",
                target = 20,
                progress = 0,
                rewardGold = 6000,
                rewardDiamond = 90
            )
        )
    }

    fun achievements(): List<AchievementEntity> {
        return listOf(

            AchievementEntity(
                title = "New Adventurer",
                description = "Mulai petualangan pertama.",
                category = "Beginner",
                rarity = "Common",
                icon = "achievement_new_adventurer",
                badge = "START",
                target = 1,
                progress = 1,
                rewardGold = 500,
                rewardDiamond = 20,
                unlocked = true
            ),

            AchievementEntity(
                title = "First Victory",
                description = "Menangkan battle pertama.",
                category = "Battle",
                rarity = "Common",
                icon = "achievement_first_victory",
                badge = "BATTLE",
                target = 1,
                rewardGold = 500,
                rewardDiamond = 20
            ),

            AchievementEntity(
                title = "Battle Rookie",
                description = "Menangkan 10 battle.",
                category = "Battle",
                rarity = "Rare",
                icon = "achievement_battle_rookie",
                badge = "BATTLE",
                target = 10,
                rewardGold = 2000,
                rewardDiamond = 30
            ),

            AchievementEntity(
                title = "Battle Veteran",
                description = "Menangkan 50 battle.",
                category = "Battle",
                rarity = "Epic",
                icon = "achievement_battle_veteran",
                badge = "BATTLE",
                target = 50,
                rewardGold = 8000,
                rewardDiamond = 80
            ),

            AchievementEntity(
                title = "Battle Legend",
                description = "Menangkan 250 battle.",
                category = "Battle",
                rarity = "Legendary",
                icon = "achievement_battle_legend",
                badge = "LEGEND",
                target = 250,
                rewardGold = 25000,
                rewardDiamond = 200
            ),

            AchievementEntity(
                title = "Card Master Beginner",
                description = "Kumpulkan 10 kartu.",
                category = "Collection",
                rarity = "Rare",
                icon = "achievement_card_master",
                badge = "CARD",
                target = 10,
                progress = 5,
                rewardGold = 3000,
                rewardDiamond = 50
            ),

            AchievementEntity(
                title = "Card Collector",
                description = "Kumpulkan 25 kartu.",
                category = "Collection",
                rarity = "Rare",
                icon = "achievement_card_collector",
                badge = "CARD",
                target = 25,
                rewardGold = 5000,
                rewardDiamond = 80
            ),

            AchievementEntity(
                title = "Card Hunter",
                description = "Kumpulkan 50 kartu.",
                category = "Collection",
                rarity = "Epic",
                icon = "achievement_card_collector",
                badge = "CARD",
                target = 50,
                rewardGold = 12000,
                rewardDiamond = 120
            ),

            AchievementEntity(
                title = "Card Emperor",
                description = "Kumpulkan 100 kartu.",
                category = "Collection",
                rarity = "Mythic",
                icon = "achievement_card_emperor",
                badge = "MYTHIC",
                target = 100,
                rewardGold = 50000,
                rewardDiamond = 300,
                rewardItemName = "Legendary Ticket",
                rewardItemAmount = 1
            ),

            AchievementEntity(
                title = "Summon Beginner",
                description = "Lakukan summon 10 kali.",
                category = "Summon",
                rarity = "Common",
                icon = "achievement_summon_beginner",
                badge = "SUMMON",
                target = 10,
                rewardGold = 2000,
                rewardDiamond = 30
            ),

            AchievementEntity(
                title = "Summon Expert",
                description = "Lakukan summon 50 kali.",
                category = "Summon",
                rarity = "Epic",
                icon = "achievement_summon_expert",
                badge = "SUMMON",
                target = 50,
                rewardGold = 10000,
                rewardDiamond = 80
            ),

            AchievementEntity(
                title = "Summon King",
                description = "Lakukan summon 200 kali.",
                category = "Summon",
                rarity = "Legendary",
                icon = "achievement_summon_king",
                badge = "KING",
                target = 200,
                rewardGold = 35000,
                rewardDiamond = 250
            ),

            AchievementEntity(
                title = "Legend Hunter",
                description = "Dapatkan 1 kartu Legendary.",
                category = "Summon",
                rarity = "Legendary",
                icon = "achievement_legend_hunter",
                badge = "LEGEND",
                target = 1,
                rewardGold = 10000,
                rewardDiamond = 100
            ),

            AchievementEntity(
                title = "Legend Collector",
                description = "Dapatkan 10 kartu Legendary.",
                category = "Summon",
                rarity = "Mythic",
                icon = "achievement_legend_collector",
                badge = "MYTHIC",
                target = 10,
                rewardGold = 100000,
                rewardDiamond = 500,
                rewardItemName = "Legendary Chest",
                rewardItemAmount = 1
            ),

            AchievementEntity(
                title = "Hero Trainer",
                description = "Level up hero 1 kali.",
                category = "Hero",
                rarity = "Common",
                icon = "achievement_hero_trainer",
                badge = "HERO",
                target = 1,
                rewardGold = 1000,
                rewardDiamond = 25
            ),

            AchievementEntity(
                title = "Hero Instructor",
                description = "Level up hero 10 kali.",
                category = "Hero",
                rarity = "Epic",
                icon = "achievement_hero_instructor",
                badge = "HERO",
                target = 10,
                rewardGold = 8000,
                rewardDiamond = 100
            ),

            AchievementEntity(
                title = "Hero Grandmaster",
                description = "Level up hero 50 kali.",
                category = "Hero",
                rarity = "Legendary",
                icon = "achievement_hero_grandmaster",
                badge = "MASTER",
                target = 50,
                rewardGold = 45000,
                rewardDiamond = 300
            ),

            AchievementEntity(
                title = "Equipment Collector",
                description = "Buka 1 equipment.",
                category = "Equipment",
                rarity = "Common",
                icon = "achievement_equipment_collector",
                badge = "EQUIP",
                target = 1,
                rewardGold = 1000,
                rewardDiamond = 20
            ),

            AchievementEntity(
                title = "Equipment Enthusiast",
                description = "Buka 10 equipment.",
                category = "Equipment",
                rarity = "Epic",
                icon = "achievement_equipment_enthusiast",
                badge = "EQUIP",
                target = 10,
                rewardGold = 12000,
                rewardDiamond = 100
            ),

            AchievementEntity(
                title = "Equipment Emperor",
                description = "Buka semua equipment.",
                category = "Equipment",
                rarity = "Legendary",
                icon = "achievement_equipment_emperor",
                badge = "EMPEROR",
                target = 30,
                rewardGold = 50000,
                rewardDiamond = 300
            ),

            AchievementEntity(
                title = "Story Explorer",
                description = "Selesaikan 5 stage story.",
                category = "Story",
                rarity = "Rare",
                icon = "achievement_story_explorer",
                badge = "STORY",
                target = 5,
                rewardGold = 3000,
                rewardDiamond = 30
            ),

            AchievementEntity(
                title = "Story Adventurer",
                description = "Selesaikan 20 stage story.",
                category = "Story",
                rarity = "Epic",
                icon = "achievement_story_adventurer",
                badge = "STORY",
                target = 20,
                rewardGold = 12000,
                rewardDiamond = 100
            ),

            AchievementEntity(
                title = "Story Champion",
                description = "Selesaikan seluruh Chapter 1.",
                category = "Story",
                rarity = "Epic",
                icon = "achievement_story_champion",
                badge = "CHAPTER",
                target = 10,
                rewardGold = 15000,
                rewardDiamond = 150
            ),

            AchievementEntity(
                title = "Story Hero",
                description = "Selesaikan seluruh Chapter 2.",
                category = "Story",
                rarity = "Legendary",
                icon = "achievement_story_hero",
                badge = "CHAPTER",
                target = 20,
                rewardGold = 30000,
                rewardDiamond = 250
            ),

            AchievementEntity(
                title = "Dragon Conqueror",
                description = "Kalahkan Dragon Lord.",
                category = "Story",
                rarity = "Mythic",
                icon = "achievement_dragon_conqueror",
                badge = "BOSS",
                target = 1,
                rewardGold = 80000,
                rewardDiamond = 500,
                rewardItemName = "Legendary Ticket",
                rewardItemAmount = 1
            ),

            AchievementEntity(
                title = "Shop Buyer",
                description = "Beli item pertama.",
                category = "Shop",
                rarity = "Common",
                icon = "achievement_shop_buyer",
                badge = "SHOP",
                target = 1,
                rewardGold = 1000,
                rewardDiamond = 20
            ),

            AchievementEntity(
                title = "Big Spender",
                description = "Beli 50 item.",
                category = "Shop",
                rarity = "Epic",
                icon = "achievement_big_spender",
                badge = "SHOP",
                target = 50,
                rewardGold = 25000,
                rewardDiamond = 120
            ),

            AchievementEntity(
                title = "Gold Collector",
                description = "Kumpulkan 10.000 Gold.",
                category = "Economy",
                rarity = "Rare",
                icon = "achievement_gold_collector",
                badge = "GOLD",
                target = 10000,
                rewardGold = 5000,
                rewardDiamond = 50
            ),

            AchievementEntity(
                title = "Gold Tycoon",
                description = "Kumpulkan 100.000 Gold.",
                category = "Economy",
                rarity = "Legendary",
                icon = "achievement_gold_tycoon",
                badge = "GOLD",
                target = 100000,
                rewardGold = 20000,
                rewardDiamond = 250
            ),

            AchievementEntity(
                title = "Gold Emperor",
                description = "Kumpulkan 1.000.000 Gold.",
                category = "Economy",
                rarity = "Mythic",
                icon = "achievement_gold_emperor",
                badge = "EMPEROR",
                target = 1000000,
                rewardGold = 100000,
                rewardDiamond = 1000
            ),

            AchievementEntity(
                title = "Guild Member",
                description = "Gabung ke guild.",
                category = "Guild",
                rarity = "Common",
                icon = "achievement_guild_member",
                badge = "GUILD",
                target = 1,
                rewardGold = 1000,
                rewardDiamond = 25
            ),

            AchievementEntity(
                title = "Guild Supporter",
                description = "Donasi 10 kali ke guild.",
                category = "Guild",
                rarity = "Epic",
                icon = "achievement_guild_supporter",
                badge = "GUILD",
                target = 10,
                rewardGold = 10000,
                rewardDiamond = 100
            ),

            AchievementEntity(
                title = "Guild Hero",
                description = "Donasi 100 kali ke guild.",
                category = "Guild",
                rarity = "Legendary",
                icon = "achievement_guild_hero",
                badge = "GUILD",
                target = 100,
                rewardGold = 50000,
                rewardDiamond = 500
            ),

            AchievementEntity(
                title = "Arena Challenger",
                description = "Menang 5 Arena Match.",
                category = "Arena",
                rarity = "Rare",
                icon = "achievement_arena_challenger",
                badge = "ARENA",
                target = 5,
                rewardGold = 5000,
                rewardDiamond = 50
            ),

            AchievementEntity(
                title = "Arena Champion",
                description = "Menang 50 Arena Match.",
                category = "Arena",
                rarity = "Legendary",
                icon = "achievement_arena_champion",
                badge = "ARENA",
                target = 50,
                rewardGold = 30000,
                rewardDiamond = 250
            ),

            AchievementEntity(
                title = "Arena King",
                description = "Menang 250 Arena Match.",
                category = "Arena",
                rarity = "Mythic",
                icon = "achievement_arena_king",
                badge = "KING",
                target = 250,
                rewardGold = 100000,
                rewardDiamond = 1000
            )
        )
    }

    fun defaultDeck(): DeckEntity {
        return DeckEntity(
            deckName = "Starter Deck",
            selected = true
        )
    }

    fun storyStages(): List<StoryStageEntity> {
        return listOf(

            // ==========================
            // CHAPTER 1 — AWAKENING FOREST
            // ==========================

            StoryStageEntity(
                chapter = 1,
                stage = 1,
                title = "Awakening Forest",
                storyText = "Di hutan kuno yang tertutup kabut ungu, sebuah kartu roh mulai terbangun.",
                storyEndingText = "Cahaya kecil dari kartu roh mulai menuntun jalanmu.",
                backgroundImage = "story_chapter_1",
                stageImage = "story_stage_1",
                enemyName = "Forest Slime",
                enemyHp = 450,
                enemyAttack = 35,
                enemyDefense = 12,
                difficulty = "NORMAL",
                recommendedPower = 300,
                energyCost = 5,
                rewardGold = 300,
                rewardExp = 40,
                rewardDiamond = 5,
                unlocked = true
            ),

            StoryStageEntity(
                chapter = 1,
                stage = 2,
                title = "Dark Wolf Path",
                storyText = "Jejak serigala gelap membawa Card Master menuju jalur terlarang.",
                storyEndingText = "Auman serigala menghilang di balik kabut malam.",
                backgroundImage = "story_chapter_1",
                stageImage = "story_stage_1",
                enemyName = "Dark Wolf",
                enemyHp = 580,
                enemyAttack = 48,
                enemyDefense = 18,
                difficulty = "NORMAL",
                recommendedPower = 420,
                energyCost = 5,
                rewardGold = 450,
                rewardExp = 60,
                rewardDiamond = 5
            ),

            StoryStageEntity(
                chapter = 1,
                stage = 3,
                title = "Poison Swamp",
                storyText = "Kabut racun mulai menyelimuti seluruh rawa.",
                storyEndingText = "Rawa beracun mulai tenang setelah monster penjaganya kalah.",
                backgroundImage = "story_chapter_1",
                stageImage = "story_stage_1",
                enemyName = "Poison Toad",
                enemyHp = 650,
                enemyAttack = 55,
                enemyDefense = 25,
                difficulty = "NORMAL",
                recommendedPower = 550,
                energyCost = 5,
                rewardGold = 550,
                rewardExp = 75,
                rewardDiamond = 8
            ),

            StoryStageEntity(
                chapter = 1,
                stage = 4,
                title = "Ancient Shrine",
                storyText = "Kuil kuno mulai menunjukkan kekuatan tersembunyi.",
                storyEndingText = "Segel pertama di kuil kuno mulai terbuka.",
                backgroundImage = "story_chapter_1",
                stageImage = "story_stage_1",
                enemyName = "Shrine Guardian",
                enemyHp = 720,
                enemyAttack = 62,
                enemyDefense = 32,
                difficulty = "NORMAL",
                recommendedPower = 700,
                energyCost = 6,
                rewardGold = 650,
                rewardExp = 90,
                rewardDiamond = 8
            ),

            StoryStageEntity(
                chapter = 1,
                stage = 5,
                title = "Shadow Nest",
                storyText = "Monster bayangan mulai berkumpul di sarangnya.",
                storyEndingText = "Sarang bayangan hancur, tetapi energi gelap masih terasa.",
                backgroundImage = "story_chapter_1",
                stageImage = "story_stage_1",
                enemyName = "Shadow Spider",
                enemyHp = 800,
                enemyAttack = 68,
                enemyDefense = 38,
                isElite = true,
                difficulty = "NORMAL",
                recommendedPower = 850,
                energyCost = 6,
                rewardGold = 750,
                rewardExp = 100,
                rewardDiamond = 10,
                cardDropRate = 5
            ),

            StoryStageEntity(
                chapter = 1,
                stage = 6,
                title = "Whispering Tree",
                storyText = "Pohon roh tua mulai berbicara kepada sang hero.",
                storyEndingText = "Pohon tua memberimu petunjuk menuju danau terlupakan.",
                backgroundImage = "story_chapter_1",
                stageImage = "story_stage_1",
                enemyName = "Tree Spirit",
                enemyHp = 900,
                enemyAttack = 75,
                enemyDefense = 45,
                difficulty = "NORMAL",
                recommendedPower = 1000,
                energyCost = 6,
                rewardGold = 850,
                rewardExp = 120,
                rewardDiamond = 10
            ),

            StoryStageEntity(
                chapter = 1,
                stage = 7,
                title = "Forgotten Lake",
                storyText = "Danau terlupakan menyimpan monster air legendaris.",
                storyEndingText = "Permukaan danau kembali tenang setelah pertarungan sengit.",
                backgroundImage = "story_chapter_1",
                stageImage = "story_stage_1",
                enemyName = "Water Serpent",
                enemyHp = 1000,
                enemyAttack = 82,
                enemyDefense = 52,
                difficulty = "NORMAL",
                recommendedPower = 1200,
                energyCost = 7,
                rewardGold = 950,
                rewardExp = 140,
                rewardDiamond = 12
            ),

            StoryStageEntity(
                chapter = 1,
                stage = 8,
                title = "Night Forest",
                storyText = "Kegelapan malam menguasai seluruh hutan.",
                storyEndingText = "Cahaya kartu roh mulai menembus kegelapan hutan.",
                backgroundImage = "story_chapter_1",
                stageImage = "story_stage_1",
                enemyName = "Night Beast",
                enemyHp = 1100,
                enemyAttack = 90,
                enemyDefense = 60,
                difficulty = "HARD",
                recommendedPower = 1400,
                energyCost = 7,
                rewardGold = 1050,
                rewardExp = 160,
                rewardDiamond = 15
            ),

            StoryStageEntity(
                chapter = 1,
                stage = 9,
                title = "Corrupted Spirit",
                storyText = "Spirit penjaga hutan telah berubah menjadi jahat.",
                storyEndingText = "Spirit yang rusak perlahan kembali menjadi cahaya.",
                backgroundImage = "story_chapter_1",
                stageImage = "story_stage_1",
                enemyName = "Corrupted Dryad",
                enemyHp = 1250,
                enemyAttack = 105,
                enemyDefense = 70,
                isElite = true,
                difficulty = "HARD",
                recommendedPower = 1650,
                energyCost = 8,
                rewardGold = 1200,
                rewardExp = 180,
                rewardDiamond = 15,
                cardDropRate = 8
            ),

            StoryStageEntity(
                chapter = 1,
                stage = 10,
                title = "Forest King",
                storyText = "Raja hutan bangkit untuk menghentikan perjalananmu.",
                storyEndingText = "Forest King tumbang, dan jalan menuju Crystal Ruins akhirnya terbuka.",
                backgroundImage = "story_chapter_1",
                stageImage = "story_stage_1",
                bossImage = "boss_stage_1",
                enemyName = "Forest King",
                enemyHp = 1600,
                enemyAttack = 130,
                enemyDefense = 90,
                enemyCriticalRate = 10,
                isBoss = true,
                difficulty = "HARD",
                recommendedPower = 2000,
                energyCost = 10,
                rewardGold = 1500,
                rewardExp = 250,
                rewardDiamond = 30,
                cardDropRate = 15
            ),

            // ==========================
            // CHAPTER 2 — CRYSTAL RUINS
            // ==========================

            StoryStageEntity(
                chapter = 2,
                stage = 1,
                title = "Crystal Ruins",
                storyText = "Reruntuhan kristal mulai retak akibat energi gelap.",
                storyEndingText = "Kristal pertama kembali bersinar lembut.",
                backgroundImage = "story_chapter_2",
                stageImage = "story_stage_2",
                enemyName = "Crystal Golem",
                enemyHp = 1800,
                enemyAttack = 145,
                enemyDefense = 100,
                difficulty = "HARD",
                recommendedPower = 2300,
                energyCost = 8,
                rewardGold = 1700,
                rewardExp = 280,
                rewardDiamond = 15
            ),

            StoryStageEntity(
                chapter = 2,
                stage = 2,
                title = "Broken Crystal",
                storyText = "Fragmen kristal hidup menyerang tanpa henti.",
                storyEndingText = "Fragmen kristal pecah menjadi debu cahaya.",
                backgroundImage = "story_chapter_2",
                stageImage = "story_stage_2",
                enemyName = "Crystal Fragment",
                enemyHp = 1900,
                enemyAttack = 155,
                enemyDefense = 110,
                difficulty = "HARD",
                recommendedPower = 2500,
                energyCost = 8,
                rewardGold = 1800,
                rewardExp = 300,
                rewardDiamond = 18
            ),

            StoryStageEntity(
                chapter = 2,
                stage = 3,
                title = "Ancient Tunnel",
                storyText = "Lorong bawah tanah penuh jebakan kuno.",
                storyEndingText = "Lorong tua terbuka menuju ruang kristal berikutnya.",
                backgroundImage = "story_chapter_2",
                stageImage = "story_stage_2",
                enemyName = "Tunnel Worm",
                enemyHp = 2100,
                enemyAttack = 170,
                enemyDefense = 120,
                difficulty = "HARD",
                recommendedPower = 2800,
                energyCost = 8,
                rewardGold = 2000,
                rewardExp = 330,
                rewardDiamond = 18
            ),

            StoryStageEntity(
                chapter = 2,
                stage = 4,
                title = "Crystal Guardian",
                storyText = "Penjaga kristal muncul dari inti reruntuhan.",
                storyEndingText = "Penjaga kristal runtuh dan meninggalkan pecahan energi.",
                backgroundImage = "story_chapter_2",
                stageImage = "story_stage_2",
                enemyName = "Crystal Knight",
                enemyHp = 2300,
                enemyAttack = 180,
                enemyDefense = 130,
                isElite = true,
                difficulty = "HARD",
                recommendedPower = 3100,
                energyCost = 9,
                rewardGold = 2200,
                rewardExp = 360,
                rewardDiamond = 20,
                equipmentDropRate = 5
            ),

            StoryStageEntity(
                chapter = 2,
                stage = 5,
                title = "Mirror Chamber",
                storyText = "Pantulan dirimu sendiri menjadi musuh mematikan.",
                storyEndingText = "Cermin ilusi pecah dan membuka jalan rahasia.",
                backgroundImage = "story_chapter_2",
                stageImage = "story_stage_2",
                enemyName = "Mirror Spirit",
                enemyHp = 2500,
                enemyAttack = 190,
                enemyDefense = 140,
                difficulty = "HARD",
                recommendedPower = 3400,
                energyCost = 9,
                rewardGold = 2400,
                rewardExp = 400,
                rewardDiamond = 20
            ),

            StoryStageEntity(
                chapter = 2,
                stage = 6,
                title = "Crystal Core",
                storyText = "Energi kristal kuno mulai kehilangan kendali.",
                storyEndingText = "Inti kristal berhasil distabilkan.",
                backgroundImage = "story_chapter_2",
                stageImage = "story_stage_2",
                enemyName = "Crystal Core",
                enemyHp = 2800,
                enemyAttack = 210,
                enemyDefense = 160,
                difficulty = "NIGHTMARE",
                recommendedPower = 3800,
                energyCost = 9,
                rewardGold = 2600,
                rewardExp = 450,
                rewardDiamond = 22
            ),

            StoryStageEntity(
                chapter = 2,
                stage = 7,
                title = "Dark Resonance",
                storyText = "Gelombang energi hitam menyebar ke seluruh reruntuhan.",
                storyEndingText = "Resonansi gelap melemah setelah sumbernya dihancurkan.",
                backgroundImage = "story_chapter_2",
                stageImage = "story_stage_2",
                enemyName = "Dark Resonator",
                enemyHp = 3100,
                enemyAttack = 225,
                enemyDefense = 175,
                difficulty = "NIGHTMARE",
                recommendedPower = 4200,
                energyCost = 10,
                rewardGold = 2900,
                rewardExp = 500,
                rewardDiamond = 25
            ),

            StoryStageEntity(
                chapter = 2,
                stage = 8,
                title = "Forgotten Hall",
                storyText = "Aula kuno menyimpan rahasia kerajaan lama.",
                storyEndingText = "Aula tua kembali sunyi setelah prajurit kuno dikalahkan.",
                backgroundImage = "story_chapter_2",
                stageImage = "story_stage_2",
                enemyName = "Ancient Soldier",
                enemyHp = 3400,
                enemyAttack = 245,
                enemyDefense = 190,
                isElite = true,
                difficulty = "NIGHTMARE",
                recommendedPower = 4600,
                energyCost = 10,
                rewardGold = 3200,
                rewardExp = 550,
                rewardDiamond = 25,
                equipmentDropRate = 8
            ),

            StoryStageEntity(
                chapter = 2,
                stage = 9,
                title = "Crystal Emperor",
                storyText = "Kaisar kristal mulai terbangun dari tidurnya.",
                storyEndingText = "Mahkota kristal retak, tetapi kekuatan kaisar belum sepenuhnya hilang.",
                backgroundImage = "story_chapter_2",
                stageImage = "story_stage_2",
                enemyName = "Crystal Emperor",
                enemyHp = 3800,
                enemyAttack = 270,
                enemyDefense = 210,
                enemyCriticalRate = 12,
                difficulty = "NIGHTMARE",
                recommendedPower = 5200,
                energyCost = 11,
                rewardGold = 3600,
                rewardExp = 620,
                rewardDiamond = 30
            ),

            StoryStageEntity(
                chapter = 2,
                stage = 10,
                title = "Ruins Overlord",
                storyText = "Penguasa reruntuhan muncul sebagai boss chapter.",
                storyEndingText = "Ruins Overlord runtuh dan membuka portal menuju Dragon Kingdom.",
                backgroundImage = "story_chapter_2",
                stageImage = "story_stage_2",
                bossImage = "boss_stage_2",
                enemyName = "Ruins Overlord",
                enemyHp = 4500,
                enemyAttack = 320,
                enemyDefense = 260,
                enemyCriticalRate = 15,
                isBoss = true,
                difficulty = "NIGHTMARE",
                recommendedPower = 6000,
                energyCost = 12,
                rewardGold = 4500,
                rewardExp = 800,
                rewardDiamond = 60,
                cardDropRate = 18,
                equipmentDropRate = 10
            ),

            // ==========================
            // CHAPTER 3 — DRAGON KINGDOM
            // ==========================

            StoryStageEntity(
                chapter = 3,
                stage = 1,
                title = "Dragon Kingdom",
                storyText = "Kerajaan naga legendaris akhirnya ditemukan.",
                storyEndingText = "Gerbang kerajaan naga terbuka perlahan.",
                backgroundImage = "story_chapter_3",
                stageImage = "story_stage_3",
                enemyName = "Dragon Scout",
                enemyHp = 5000,
                enemyAttack = 350,
                enemyDefense = 280,
                difficulty = "NIGHTMARE",
                recommendedPower = 6500,
                energyCost = 10,
                rewardGold = 5000,
                rewardExp = 850,
                rewardDiamond = 30
            ),

            StoryStageEntity(
                chapter = 3,
                stage = 2,
                title = "Flame Valley",
                storyText = "Lembah api dipenuhi naga muda penjaga wilayah.",
                storyEndingText = "Api lembah mulai mereda setelah Fire Drake kalah.",
                backgroundImage = "story_chapter_3",
                stageImage = "story_stage_3",
                enemyName = "Fire Drake",
                enemyHp = 5400,
                enemyAttack = 380,
                enemyDefense = 300,
                difficulty = "NIGHTMARE",
                recommendedPower = 7000,
                energyCost = 11,
                rewardGold = 5500,
                rewardExp = 900,
                rewardDiamond = 35
            ),

            StoryStageEntity(
                chapter = 3,
                stage = 3,
                title = "Dragon Nest",
                storyText = "Sarang naga dipenuhi telur naga kuno.",
                storyEndingText = "Sarang naga berhasil diamankan.",
                backgroundImage = "story_chapter_3",
                stageImage = "story_stage_3",
                enemyName = "Dragon Hatchling",
                enemyHp = 6000,
                enemyAttack = 410,
                enemyDefense = 330,
                isElite = true,
                difficulty = "NIGHTMARE",
                recommendedPower = 7600,
                energyCost = 11,
                rewardGold = 6200,
                rewardExp = 980,
                rewardDiamond = 35,
                cardDropRate = 10
            ),

            StoryStageEntity(
                chapter = 3,
                stage = 4,
                title = "Sky Temple",
                storyText = "Kuil langit menjadi gerbang menuju penguasa naga.",
                storyEndingText = "Langit bergetar saat pintu kuil terbuka.",
                backgroundImage = "story_chapter_3",
                stageImage = "story_stage_3",
                enemyName = "Sky Guardian",
                enemyHp = 6700,
                enemyAttack = 450,
                enemyDefense = 360,
                difficulty = "HELL",
                recommendedPower = 8500,
                energyCost = 12,
                rewardGold = 7000,
                rewardExp = 1100,
                rewardDiamond = 40
            ),

            StoryStageEntity(
                chapter = 3,
                stage = 5,
                title = "Dragon Lord",
                storyText = "Pemimpin para naga mulai menunjukkan kekuatannya.",
                storyEndingText = "Dragon Lord mengakui kekuatanmu, tetapi ancaman yang lebih besar mulai muncul.",
                backgroundImage = "story_chapter_3",
                stageImage = "story_stage_3",
                bossImage = "boss_stage_3",
                enemyName = "Dragon Lord",
                enemyHp = 8000,
                enemyAttack = 520,
                enemyDefense = 420,
                enemyCriticalRate = 20,
                isBoss = true,
                difficulty = "HELL",
                recommendedPower = 10000,
                energyCost = 15,
                rewardGold = 10000,
                rewardExp = 1500,
                rewardDiamond = 100,
                cardDropRate = 25,
                equipmentDropRate = 15,
                heroDropRate = 5
            )
        )
    }

    fun shopItems(): List<ShopItemEntity> {
        return listOf(

            ShopItemEntity(itemName = "Diamond Pack 50", itemType = "Diamond Pack", itemImage = "item_diamond_pack_small",
                description = "Paket 50 Diamond yang bisa dibeli memakai Gold.",
                rarity = "Rare", priceGold = 50000, quantity = 50,
                featured = true, badge = "GOLD", glowColor = "#7DE7FF", sortOrder = 0
            ),

            ShopItemEntity(itemName = "Diamond Pack 120", itemType = "Diamond Pack", itemImage = "item_diamond_pack_medium",
                description = "Paket 120 Diamond untuk summon dan ticket premium.",
                rarity = "Epic", priceGold = 120000, quantity = 120,
                featured = true, badge = "HOT", glowColor = "#B56CFF", sortOrder = 0
            ),

            ShopItemEntity(itemName = "Diamond Pack 250", itemType = "Diamond Pack", itemImage = "item_diamond_pack_large",
                description = "Paket 250 Diamond terbaik untuk player aktif.",
                rarity = "Legendary", priceGold = 250000, quantity = 250,
                featured = true, badge = "BEST", glowColor = "#FFD700", sortOrder = 0
            ),

            ShopItemEntity(itemName = "Small Potion", itemType = "Consumable", itemImage = "item_small_potion",
                description = "Memulihkan sedikit HP hero saat battle.",
                rarity = "Common", priceGold = 300, badge = "NEW",
                glowColor = "#6CFF9B", sortOrder = 1
            ),

            ShopItemEntity(itemName = "Large Potion", itemType = "Consumable", itemImage = "item_large_potion",
                description = "Memulihkan HP hero dalam jumlah besar.",
                rarity = "Rare", priceGold = 800,
                glowColor = "#4FC3F7", sortOrder = 2
            ),

            ShopItemEntity(itemName = "Mega Potion", itemType = "Consumable", itemImage = "item_mega_potion",
                description = "Potion premium untuk memulihkan HP sangat besar.",
                rarity = "Epic", priceGold = 2000, badge = "HOT",
                glowColor = "#B56CFF", sortOrder = 3
            ),

            ShopItemEntity(itemName = "Summon Ticket", itemType = "Ticket", itemImage = "item_summon_ticket",
                description = "Ticket untuk melakukan summon kartu spirit.",
                rarity = "Rare", priceDiamond = 15, isDiamondShop = true,
                glowColor = "#FFD66B", sortOrder = 10
            ),

            ShopItemEntity(itemName = "Epic Ticket", itemType = "Ticket", itemImage = "item_epic_ticket",
                description = "Ticket dengan peluang tinggi mendapatkan kartu Epic.",
                rarity = "Epic", priceDiamond = 45, isDiamondShop = true,
                badge = "HOT", glowColor = "#B56CFF", sortOrder = 11
            ),

            ShopItemEntity(itemName = "Legendary Ticket", itemType = "Ticket", itemImage = "item_legend_ticket",
                description = "Ticket langka untuk summon kartu Epic atau Legendary.",
                rarity = "Legendary", priceDiamond = 100, isDiamondShop = true,
                featured = true, badge = "LIMITED",
                glowColor = "#FFD700", sortOrder = 12
            ),

            ShopItemEntity(itemName = "Fire Crystal", itemType = "Material", itemImage = "item_fire_crystal",
                description = "Material elemen api untuk upgrade hero dan kartu Fire.",
                rarity = "Rare", priceGold = 500, quantity = 3,
                glowColor = "#FF7043", sortOrder = 20
            ),

            ShopItemEntity(itemName = "Water Crystal", itemType = "Material", itemImage = "item_water_crystal",
                description = "Material elemen air untuk upgrade hero dan kartu Water.",
                rarity = "Rare", priceGold = 500, quantity = 3,
                glowColor = "#4FC3F7", sortOrder = 21
            ),

            ShopItemEntity(itemName = "Wind Crystal", itemType = "Material", itemImage = "item_wind_crystal",
                description = "Material elemen angin untuk upgrade hero dan kartu Wind.",
                rarity = "Rare", priceGold = 500, quantity = 3,
                glowColor = "#66BB6A", sortOrder = 22
            ),

            ShopItemEntity(itemName = "Dark Crystal", itemType = "Material", itemImage = "item_dark_crystal",
                description = "Material gelap untuk upgrade hero dan kartu Dark.",
                rarity = "Epic", priceGold = 800, quantity = 2,
                glowColor = "#9C27B0", sortOrder = 23
            ),

            ShopItemEntity(itemName = "Light Crystal", itemType = "Material", itemImage = "item_light_crystal",
                description = "Material cahaya untuk upgrade hero dan kartu Light.",
                rarity = "Epic", priceGold = 800, quantity = 2,
                glowColor = "#FFD54F", sortOrder = 24
            ),

            ShopItemEntity(itemName = "Iron Ore", itemType = "Material", itemImage = "item_iron_ore",
                description = "Material dasar untuk upgrade equipment.",
                rarity = "Common", priceGold = 600, quantity = 5,
                glowColor = "#C9C9C9", sortOrder = 30
            ),

            ShopItemEntity(itemName = "Mythril Ore", itemType = "Material", itemImage = "item_mythril_ore",
                description = "Ore langka untuk upgrade equipment tingkat tinggi.",
                rarity = "Epic", priceGold = 2500, quantity = 2,
                glowColor = "#B56CFF", sortOrder = 31
            ),

            ShopItemEntity(itemName = "EXP Book", itemType = "Upgrade", itemImage = "item_exp_book",
                description = "Buku EXP untuk menaikkan level hero.",
                rarity = "Rare", priceGold = 1200,
                glowColor = "#4FC3F7", sortOrder = 40
            ),

            ShopItemEntity(itemName = "Hero Scroll", itemType = "Upgrade", itemImage = "item_hero_scroll",
                description = "Scroll kuno untuk membuka potensi hero.",
                rarity = "Epic", priceGold = 2200,
                glowColor = "#B56CFF", sortOrder = 41
            ),

            ShopItemEntity(itemName = "Hero Shard", itemType = "Upgrade", itemImage = "item_hero_shard",
                description = "Shard untuk unlock atau upgrade hero spesial.",
                rarity = "Epic", priceDiamond = 20, quantity = 5,
                isDiamondShop = true, badge = "HOT",
                glowColor = "#B56CFF", sortOrder = 42
            ),

            ShopItemEntity(itemName = "Awaken Stone", itemType = "Upgrade", itemImage = "item_awaken_stone",
                description = "Batu awakening untuk meningkatkan kekuatan hero.",
                rarity = "Legendary", priceDiamond = 35,
                isDiamondShop = true, featured = true, badge = "RARE",
                glowColor = "#FFD700", sortOrder = 43
            ),

            ShopItemEntity(itemName = "Bronze Chest", itemType = "Chest", itemImage = "item_bronze_chest",
                description = "Chest sederhana berisi gold, material, atau potion.",
                rarity = "Common", priceGold = 1500,
                glowColor = "#CD7F32", sortOrder = 50
            ),

            ShopItemEntity(itemName = "Silver Chest", itemType = "Chest", itemImage = "item_silver_chest",
                description = "Chest berisi reward Rare atau material upgrade.",
                rarity = "Rare", priceGold = 4000,
                glowColor = "#C0C0C0", sortOrder = 51
            ),

            ShopItemEntity(itemName = "Gold Chest", itemType = "Chest", itemImage = "item_gold_chest",
                description = "Chest premium dengan peluang reward Epic.",
                rarity = "Epic", priceGold = 10000,
                featured = true, badge = "BEST",
                glowColor = "#FFD700", sortOrder = 52
            ),

            ShopItemEntity(itemName = "Legendary Chest", itemType = "Chest", itemImage = "item_legendary_chest",
                description = "Chest spesial dengan peluang item Legendary.",
                rarity = "Legendary", priceDiamond = 150,
                isDiamondShop = true, featured = true, badge = "LIMITED",
                discountPercent = 10, stock = 5,
                glowColor = "#FFD700", sortOrder = 53
            ),

            ShopItemEntity(itemName = "Diamond Pack S", itemType = "Diamond", itemImage = "item_diamond_pack_small",
                description = "Paket kecil diamond untuk summon dan shop premium.",
                rarity = "Rare", priceDiamond = 50,
                isDiamondShop = true,
                glowColor = "#7DE7FF", sortOrder = 60
            ),

            ShopItemEntity(itemName = "Diamond Pack M", itemType = "Diamond", itemImage = "item_diamond_pack_medium",
                description = "Paket medium diamond dengan value lebih baik.",
                rarity = "Epic", priceDiamond = 120,
                isDiamondShop = true, badge = "VALUE",
                glowColor = "#4FC3F7", sortOrder = 61
            ),

            ShopItemEntity(itemName = "Diamond Pack L", itemType = "Diamond", itemImage = "item_diamond_pack_large",
                description = "Paket besar diamond terbaik untuk player aktif.",
                rarity = "Legendary", priceDiamond = 250,
                isDiamondShop = true, featured = true, badge = "BEST",
                glowColor = "#FFD700", sortOrder = 62
            )
        )
    }
}