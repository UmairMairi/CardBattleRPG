package com.pegasus.cardbattlerpg

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.pegasus.cardbattlerpg.utils.GameAudioManager
import com.pegasus.cardbattlerpg.utils.bgmForRoute
import com.pegasus.cardbattlerpg.database.DatabaseProvider
import com.pegasus.cardbattlerpg.repository.GameRepository
import com.pegasus.cardbattlerpg.ui.AppScreen
import com.pegasus.cardbattlerpg.ui.login.LoginScreen
import com.pegasus.cardbattlerpg.ui.menu.MainMenuScreen
import com.pegasus.cardbattlerpg.ui.splash.SplashScreen
import com.pegasus.cardbattlerpg.ui.theme.CardBattleRPGTheme
import com.pegasus.cardbattlerpg.ui.collection.CollectionScreen
import com.pegasus.cardbattlerpg.ui.deck.DeckBuilderScreen
import com.pegasus.cardbattlerpg.ui.hero.HeroScreen
import com.pegasus.cardbattlerpg.ui.equipment.EquipmentScreen
import com.pegasus.cardbattlerpg.ui.battle.BattleScreen
import com.pegasus.cardbattlerpg.ui.story.StoryScreen
import com.pegasus.cardbattlerpg.ui.story.StoryBattleScreen
import com.pegasus.cardbattlerpg.ui.summon.SummonScreen
import com.pegasus.cardbattlerpg.ui.inventory.InventoryScreen
import com.pegasus.cardbattlerpg.ui.mission.MissionScreen
import com.pegasus.cardbattlerpg.ui.achievement.AchievementScreen
import com.pegasus.cardbattlerpg.ui.save.SaveGameScreen
import com.pegasus.cardbattlerpg.repository.RankingRepository
import com.pegasus.cardbattlerpg.ui.ranking.RankingScreen
import com.pegasus.cardbattlerpg.ui.arena.ArenaScreen
import com.pegasus.cardbattlerpg.ui.shop.ShopScreen
import com.pegasus.cardbattlerpg.ui.profile.ProfileScreen
import com.pegasus.cardbattlerpg.ui.guild.GuildScreen

class MainActivity : ComponentActivity() {

    override fun onPause() {
        super.onPause()
        GameAudioManager.pauseBgm()
    }

    override fun onResume() {
        super.onResume()
        GameAudioManager.resumeBgm()
    }

    override fun onDestroy() {
        GameAudioManager.release()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android 15+ (enforced with no opt-out on API 36) draws the app edge-to-edge.
        // Transparent bars with light icons suit the dark game UI; content is inset below.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        val database = DatabaseProvider.getDatabase(this)
        val repository = GameRepository(
            playerDao = database.playerDao(),
            cardDao = database.cardDao(),
            heroDao = database.heroDao(),
            equipmentDao = database.equipmentDao(),
            inventoryDao = database.inventoryDao(),
            missionDao = database.missionDao(),
            achievementDao = database.achievementDao(),
            deckDao = database.deckDao(),
            storyDao = database.storyDao(),
            shopDao = database.shopDao(),
            guildDao = database.guildDao()
        )
        setContent {
            CardBattleRPGTheme {
                val context = LocalContext.current
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                DisposableEffect(Unit) {
                    GameAudioManager.init(context)
                    onDispose { }
                }

                LaunchedEffect(currentRoute) {
                    GameAudioManager.playBgm(context, bgmForRoute(currentRoute))
                }

                // Keep every screen clear of the status bar, navigation bar, display cutout and keyboard.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF08000F))
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = AppScreen.Splash.route
                    ) {
                        composable(AppScreen.Splash.route) {
                            SplashScreen(
                                repository = repository,
                                onGoLogin = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Login.route) {
                                        popUpTo(AppScreen.Splash.route) {
                                            inclusive = true
                                        }
                                    }
                                },
                                onGoMenu = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Menu.route) {
                                        popUpTo(AppScreen.Splash.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }

                        composable(AppScreen.Login.route) {
                            LoginScreen(
                                repository = repository,
                                onLoginSuccess = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Menu.route) {
                                        popUpTo(AppScreen.Login.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }

                        composable(AppScreen.Menu.route) {
                            MainMenuScreen(
                                repository = repository,
                                onOpenCollection = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Collection.route)
                                },
                                onOpenDeck = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Deck.route)
                                },
                                onOpenHero = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Hero.route)
                                },
                                onOpenEquipment = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Equipment.route)
                                },
                                onOpenBattle = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Battle.route)
                                },
                                onOpenStory = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Story.route)
                                },
                                onOpenSummon = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Summon.route)
                                },
                                onOpenInventory = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Inventory.route)
                                },
                                onOpenMission = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Mission.route)
                                },
                                onOpenAchievement = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Achievement.route)
                                },
                                onOpenSaveGame = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.SaveGame.route)
                                },
                                onOpenRanking = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Ranking.route)
                                },
                                onOpenArena = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Arena.route)
                                },
                                onOpenShop = {
                                    GameAudioManager.playClick()
                                    navController.navigate(
                                        AppScreen.Shop.route
                                    )
                                },
                                onOpenProfile = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Profile.route)
                                },
                                onOpenGuild = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Guild.route)
                                }
                            )
                        }

                        composable(AppScreen.Collection.route) {
                            CollectionScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.Deck.route) {
                            DeckBuilderScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.Hero.route) {
                            HeroScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.Equipment.route) {
                            EquipmentScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.Battle.route) {
                            BattleScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.Story.route) {
                            StoryScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                },
                                onOpenStage = { stageId ->
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.StoryBattle.createRoute(stageId))
                                }
                            )
                        }

                        composable(AppScreen.StoryBattle.route) { backStackEntry ->
                            val stageId = backStackEntry.arguments
                                ?.getString("stageId")
                                ?.toIntOrNull() ?: 1

                            StoryBattleScreen(
                                repository = repository,
                                stageId = stageId,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.Summon.route) {
                            SummonScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.Inventory.route) {
                            InventoryScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.Mission.route) {
                            MissionScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.Achievement.route) {
                            AchievementScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.SaveGame.route) {
                            SaveGameScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                },
                                onResetFinished = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Login.route) {
                                        popUpTo(AppScreen.Menu.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }

                        composable(AppScreen.Ranking.route) {
                            RankingScreen(
                                gameRepository = repository,
                                rankingRepository = RankingRepository(),
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.Arena.route) {
                            ArenaScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            AppScreen.Shop.route
                        ) {
                            ShopScreen(
                                repository =
                                    repository,

                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreen.Profile.route) {
                            ProfileScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                },
                                onLogout = {
                                    GameAudioManager.playClick()
                                    navController.navigate(AppScreen.Login.route) {
                                        popUpTo(AppScreen.Menu.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(AppScreen.Guild.route) {
                            GuildScreen(
                                repository = repository,
                                onBack = {
                                    GameAudioManager.playClick()
                                    navController.popBackStack()
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}