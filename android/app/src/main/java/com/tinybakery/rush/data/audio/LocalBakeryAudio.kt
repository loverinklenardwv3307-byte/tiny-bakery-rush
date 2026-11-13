package com.tinybakery.rush.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.tinybakery.rush.domain.repository.BakeryAudio
import com.tinybakery.rush.domain.repository.BakeryTone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

class LocalBakeryAudio(context: Context) : BakeryAudio {
    private val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    @Volatile private var focused = false
    private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
    private val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(attributes)
        .setOnAudioFocusChangeListener { change -> focused = change == AudioManager.AUDIOFOCUS_GAIN }
        .build()

    override fun activate(): Boolean {
        focused = manager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return focused
    }

    override fun deactivate() {
        focused = false
        manager.abandonAudioFocusRequest(request)
    }

    override suspend fun music() {
        val notes = listOf(261.63, 329.63, 392.0, 329.63, 349.23, 440.0, 392.0, 329.63)
        while (currentCoroutineContext().isActive) {
            for (note in notes) {
                if (focused) play(note, 450, 0.018)
                delay(50L)
            }
        }
    }

    override suspend fun effect(tone: BakeryTone) {
        if (!focused) return
        when (tone) {
            BakeryTone.SHAPE -> play(392.0, 65, 0.055)
            BakeryTone.COLLECT -> play(523.25, 90, 0.055)
            BakeryTone.SERVE -> { play(523.25, 60, 0.055); play(659.25, 60, 0.055) }
        }
    }

    private suspend fun play(frequency: Double, milliseconds: Int, amplitude: Double) = withContext(Dispatchers.IO) {
        if (!focused) return@withContext
        val count = 22050 * milliseconds / 1000
        val samples = ShortArray(count) { index ->
            val envelope = minOf(1.0, index / 220.0, (count - index) / 220.0)
            (sin(2.0 * PI * frequency * index / 22050.0) * Short.MAX_VALUE * amplitude * envelope).toInt().toShort()
        }
        var track: AudioTrack? = null
        try {
            val output = AudioTrack.Builder().setAudioAttributes(attributes)
                .setAudioFormat(AudioFormat.Builder().setSampleRate(22050).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
                .setTransferMode(AudioTrack.MODE_STREAM).setBufferSizeInBytes(maxOf(4096, samples.size * 2)).build()
            track = output
            output.play()
            var position = 0
            while (position < samples.size && focused) {
                currentCoroutineContext().ensureActive()
                val written = output.write(samples, position, minOf(441, samples.size - position), AudioTrack.WRITE_BLOCKING)
                if (written <= 0) break
                position += written
            }
            delay(milliseconds.toLong())
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            focused = false
        } finally {
            track?.let { output -> runCatching { output.stop() }; output.release() }
        }
    }
}
