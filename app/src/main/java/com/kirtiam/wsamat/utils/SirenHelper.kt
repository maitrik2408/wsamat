package com.kirtiam.wsamat.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.sin

class SirenHelper(private val context: Context) {

    private var audioTrack: AudioTrack? = null
    @Volatile
    private var isPlaying = false

    fun startSiren() {
        if (isPlaying) return
        isPlaying = true

        thread {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize)
                .build()

            audioTrack?.play()

            val numSamples = sampleRate / 2
            val sampleLow = ShortArray(numSamples)
            val sampleHigh = ShortArray(numSamples)

            val freqLow = 600.0
            val freqHigh = 1200.0

            for (i in 0 until numSamples) {
                val angleLow = 2.0 * Math.PI * i / (sampleRate / freqLow)
                sampleLow[i] = (sin(angleLow) * Short.MAX_VALUE * 0.9).toInt().toShort()

                val angleHigh = 2.0 * Math.PI * i / (sampleRate / freqHigh)
                sampleHigh[i] = (sin(angleHigh) * Short.MAX_VALUE * 0.9).toInt().toShort()
            }

            while (isPlaying) {
                audioTrack?.write(sampleLow, 0, sampleLow.size)
                if (!isPlaying) break
                audioTrack?.write(sampleHigh, 0, sampleHigh.size)
            }
        }
    }

    fun startHighDecibelWhistle() {
        if (isPlaying) return
        isPlaying = true

        thread {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize)
                .build()

            audioTrack?.play()

            val numSamples = sampleRate / 4
            val whistleBuffer = ShortArray(numSamples)
            val baseFreq = 3800.0

            while (isPlaying) {
                for (i in 0 until numSamples) {
                    val vibrato = sin(2.0 * Math.PI * i / (sampleRate / 15.0)) * 200.0
                    val freq = baseFreq + vibrato
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    whistleBuffer[i] = (sin(angle) * Short.MAX_VALUE * 0.95).toInt().toShort()
                }
                audioTrack?.write(whistleBuffer, 0, whistleBuffer.size)
            }
        }
    }

    fun stopSiren() {
        isPlaying = false
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isSirenPlaying(): Boolean = isPlaying
}
