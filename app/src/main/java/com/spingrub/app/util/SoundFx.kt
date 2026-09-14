package com.spingrub.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.spingrub.app.R

/**
 * Lightweight sound-effect player backed by [SoundPool]. Loads two short
 * bundled clips (a spin "tick" and a landing "ding") once and plays them
 * on demand. All playback is a no-op until the clips finish loading and
 * only fires when the caller passes enabled = true (the user's Sound setting).
 */
object SoundFx {
    private var pool: SoundPool? = null
    private var tickId: Int = 0
    private var dingId: Int = 0
    private var tickReady = false
    private var dingReady = false
    private var lastTickAt = 0L

    fun init(context: Context) {
        if (pool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val sp = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()
        sp.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                if (sampleId == tickId) tickReady = true
                if (sampleId == dingId) dingReady = true
            }
        }
        val app = context.applicationContext
        tickId = sp.load(app, R.raw.tick, 1)
        dingId = sp.load(app, R.raw.ding, 1)
        pool = sp
    }

    /** Short click while spinning. Throttled so rapid ticks don't machine-gun. */
    fun tick(enabled: Boolean) {
        if (!enabled) return
        val sp = pool ?: return
        if (!tickReady) return
        val now = System.currentTimeMillis()
        if (now - lastTickAt < 45) return
        lastTickAt = now
        sp.play(tickId, 0.35f, 0.35f, 0, 0, 1f)
    }

    /** Celebratory chime when a wheel settles. */
    fun ding(enabled: Boolean) {
        if (!enabled) return
        val sp = pool ?: return
        if (!dingReady) return
        sp.play(dingId, 0.8f, 0.8f, 1, 0, 1f)
    }

    fun release() {
        pool?.release()
        pool = null
        tickReady = false
        dingReady = false
    }
}
