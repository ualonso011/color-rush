package com.gentleai.colorrush.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.util.Log
import com.gentleai.colorrush.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [AudioManager] using [MediaPlayer] for looping
 * background music and [SoundPool] for low-latency sound effects.
 *
 * Both playback systems are created lazily and gracefully degrade when audio
 * resources have not been bundled yet (e.g. during early development phases).
 *
 * Lifecycle:
 * - Created as a singleton via Hilt.
 * - Call [release] from a LifecycleObserver or ViewModel.onCleared.
 *
 * @param context Application context required to load audio resources.
 */
@Singleton
class AudioManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AudioManager {

    companion object {
        private const val TAG = "AudioManager"
        private const val BGM_VOLUME = 0.5f
        private const val SFX_VOLUME = 0.8f
        private const val MAX_STREAMS = 4
    }

    /** System audio manager for focus requests. */
    private val systemAudioManager: android.media.AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

    /** Background music player — null until first successful load. */
    private var mediaPlayer: MediaPlayer? = null

    /** Sound pool for short SFX — null until first successful load. */
    private var soundPool: SoundPool? = null

    /** Maps [SfxType] to the SoundPool sample ID (0 = not loaded). */
    private val sfxIds = mutableMapOf<SfxType, Int>()

    /** Whether audio output is globally enabled. */
    @Volatile
    private var enabled = true

    /** Tracks whether we currently hold audio focus for BGM. */
    private var hasAudioFocus = false

    // ── Initialization ──────────────────────────────────────────────────────

    init {
        try {
            initializeSoundPool()
            initializeMediaPlayer()
        } catch (e: Exception) {
            Log.w(TAG, "Audio initialization failed — audio will be unavailable", e)
        }
        Log.d(TAG, "Init complete — mediaPlayer=${mediaPlayer != null}, soundPool=${soundPool != null}, sfxLoaded=${sfxIds.size}")
    }

    private fun initializeSoundPool() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(attrs)
            .build()

        loadSfx(SfxType.TAP_GREEN, R.raw.sfx_tap_green)
        loadSfx(SfxType.TAP_RED, R.raw.sfx_tap_red)
        loadSfx(SfxType.TAP_YELLOW, R.raw.sfx_tap_yellow)
        loadSfx(SfxType.GAME_OVER, R.raw.sfx_game_over)
    }

    private fun loadSfx(type: SfxType, resId: Int) {
        try {
            val sampleId = soundPool?.load(context, resId, 1) ?: return
            if (sampleId != 0) {
                sfxIds[type] = sampleId
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load SFX: $type", e)
        }
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = try {
            val mp = MediaPlayer.create(context, R.raw.bgm_arcade)
            if (mp == null) {
                Log.w(TAG, "MediaPlayer.create returned null — BGM resource may be missing or corrupt")
                null
            } else {
                mp.isLooping = true
                mp.setVolume(BGM_VOLUME, BGM_VOLUME)
                mp
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create background music player", e)
            null
        }
    }

    // ── Audio focus ─────────────────────────────────────────────────────────

    /** Requests audio focus for music/game playback. */
    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(android.media.AudioManager.AUDIOFOCUS_GAIN)
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            systemAudioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            systemAudioManager.requestAudioFocus(
                null,
                android.media.AudioManager.STREAM_MUSIC,
                android.media.AudioManager.AUDIOFOCUS_GAIN,
            )
        }
        hasAudioFocus = result == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (!hasAudioFocus) {
            Log.w(TAG, "Audio focus request denied")
        }
        return hasAudioFocus
    }

    /** Abandons audio focus when pausing/stopping. */
    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(android.media.AudioManager.AUDIOFOCUS_GAIN)
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            systemAudioManager.abandonAudioFocusRequest(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            systemAudioManager.abandonAudioFocus(null)
        }
        hasAudioFocus = false
    }

    // ── BGM controls ────────────────────────────────────────────────────────

    override fun playBgm() {
        if (!enabled) return
        val mp = mediaPlayer ?: return
        if (!mp.isPlaying) {
            if (!requestAudioFocus()) return
            try {
                mp.start()
                Log.d(TAG, "BGM started")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start BGM", e)
            }
        }
    }

    override fun stopBgm() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    pause()
                    seekTo(0)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to stop BGM", e)
        }
        abandonAudioFocus()
    }

    override fun pauseBgm() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) pause()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to pause BGM", e)
        }
        abandonAudioFocus()
    }

    override fun resumeBgm() {
        if (!enabled) return
        try {
            mediaPlayer?.apply {
                if (!isPlaying) {
                    if (!requestAudioFocus()) return
                    start()
                    Log.d(TAG, "BGM resumed")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resume BGM", e)
        }
    }

    // ── SFX ─────────────────────────────────────────────────────────────────

    override fun playSfx(type: SfxType) {
        if (!enabled) return
        val pool = soundPool ?: return
        val sampleId = sfxIds[type] ?: return
        if (sampleId != 0) {
            try {
                pool.play(
                    sampleId,
                    SFX_VOLUME, SFX_VOLUME, // left/right volume
                    1,              // priority (unused by modern SoundPool)
                    0,              // loop (0 = no loop)
                    1.0f,           // rate (1.0 = normal speed)
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to play SFX: $type", e)
            }
        }
    }

    // ── Global control ──────────────────────────────────────────────────────

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) {
            stopBgm()
            abandonAudioFocus()
        }
    }

    override fun release() {
        abandonAudioFocus()
        try {
            mediaPlayer?.release()
            soundPool?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing audio resources", e)
        }
        mediaPlayer = null
        soundPool = null
        sfxIds.clear()
    }
}
