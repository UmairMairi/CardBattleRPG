package com.pegasus.cardbattlerpg.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.pegasus.cardbattlerpg.R

object GameAudioManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentBgm: Int = 0
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<Int, Int>()
    private var initialized = false

    var bgmEnabled: Boolean = true
    var sfxEnabled: Boolean = true
    var bgmVolume: Float = 0.42f
    var sfxVolume: Float = 0.82f

    fun init(context: Context) {
        if (initialized) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attrs)
            .build()

        listOf(
            R.raw.sfx_click,
            R.raw.sfx_attack,
            R.raw.sfx_hit,
            R.raw.sfx_enemy_attack,
            R.raw.sfx_victory,
            R.raw.sfx_defeat,
            R.raw.sfx_potion
        ).forEach { resId ->
            soundIds[resId] = soundPool?.load(context.applicationContext, resId, 1) ?: 0
        }
        initialized = true
    }

    fun playBgm(context: Context, bgmRes: Int) {
        if (!bgmEnabled || bgmRes == 0) return
        if (currentBgm == bgmRes && mediaPlayer?.isPlaying == true) return

        stopBgm()
        currentBgm = bgmRes
        mediaPlayer = MediaPlayer.create(context.applicationContext, bgmRes)?.apply {
            isLooping = true
            setVolume(bgmVolume, bgmVolume)
            start()
        }
    }

    fun stopBgm() {
        mediaPlayer?.runCatching { stop() }
        mediaPlayer?.release()
        mediaPlayer = null
        currentBgm = 0
    }

    fun pauseBgm() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }

    fun resumeBgm() {
        mediaPlayer?.takeIf { bgmEnabled && !it.isPlaying }?.start()
    }

    fun playClick() = playSfx(R.raw.sfx_click)
    fun playAttack() = playSfx(R.raw.sfx_attack)
    fun playHit() = playSfx(R.raw.sfx_hit)
    fun playEnemyAttack() = playSfx(R.raw.sfx_enemy_attack)
    fun playVictory() = playSfx(R.raw.sfx_victory)
    fun playDefeat() = playSfx(R.raw.sfx_defeat)
    fun playPotion() = playSfx(R.raw.sfx_potion)

    fun playSfx(resId: Int) {
        if (!sfxEnabled) return
        val id = soundIds[resId] ?: return
        soundPool?.play(id, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    fun release() {
        stopBgm()
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        initialized = false
    }
}

fun bgmForRoute(route: String?): Int {
    return when {
        route == null -> R.raw.bgm_default
        route.startsWith("splash") -> R.raw.bgm_splash
        route.startsWith("login") -> R.raw.bgm_login
        route.startsWith("menu") -> R.raw.bgm_menu
        route.startsWith("story_battle") || route.startsWith("battle") -> R.raw.bgm_battle
        route.startsWith("story") -> R.raw.bgm_story
        route.startsWith("summon") -> R.raw.bgm_summon
        route.startsWith("shop") -> R.raw.bgm_shop
        route.startsWith("inventory") || route.startsWith("equipment") || route.startsWith("hero") -> R.raw.bgm_inventory
        route.startsWith("profile") -> R.raw.bgm_profile
        route.startsWith("guild") -> R.raw.bgm_guild
        else -> R.raw.bgm_default
    }
}
