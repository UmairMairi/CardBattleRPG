package com.pegasus.cardbattlerpg.ui

sealed class AppScreen(val route: String) {
    object Splash : AppScreen("splash")
    object Login : AppScreen("login")
    object Menu : AppScreen("menu")
    object Collection : AppScreen("collection")
    object Deck : AppScreen("deck")
    object Hero : AppScreen("hero")
    object Equipment : AppScreen("equipment")
    object Battle : AppScreen("battle")

    object Story : AppScreen("story")
    object StoryBattle : AppScreen("story_battle/{stageId}") {
        fun createRoute(stageId: Int): String {
            return "story_battle/$stageId"
        }
    }
    object Summon : AppScreen("summon")
    object Inventory : AppScreen("inventory")
    object Mission : AppScreen("mission")
    object Achievement : AppScreen("achievement")
    object SaveGame : AppScreen("save_game")
    object Ranking : AppScreen("ranking")
    object Arena : AppScreen("arena")
    object Shop : AppScreen("shop")

    object Profile : AppScreen("profile")

    object Guild : AppScreen("guild")

}