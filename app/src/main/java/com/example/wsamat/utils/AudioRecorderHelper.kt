package com.example.wsamat.utils

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioFile: File? = null
    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null

    fun startRecording(durationSeconds: Int = 15, onFinished: (String?) -> Unit = {}): String? {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.w("AudioRecorderHelper", "RECORD_AUDIO permission not granted")
            onFinished(null)
            return null
        }

        try {
            stopRecording()

            val outputDir = File(context.getExternalFilesDir(null), "emergency_audio").apply {
                if (!exists()) mkdirs()
            }
            val audioFile = File(outputDir, "SOS_Audio_${System.currentTimeMillis()}.mp4")
            currentAudioFile = audioFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true
            Log.d("AudioRecorderHelper", "Emergency audio recording started: ${audioFile.absolutePath}")

            stopRunnable = Runnable {
                val path = stopRecording()
                onFinished(path)
            }
            handler.postDelayed(stopRunnable!!, durationSeconds * 1000L)

            return audioFile.absolutePath
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error starting audio recorder", e)
            stopRecording()
            onFinished(null)
            return null
        }
    }

    fun stopRecording(): String? {
        stopRunnable?.let { handler.removeCallbacks(it) }
        stopRunnable = null

        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {
                Log.e("AudioRecorderHelper", "Error stopping MediaRecorder", e)
            } finally {
                mediaRecorder = null
                isRecording = false
            }
            val path = currentAudioFile?.absolutePath
            Log.d("AudioRecorderHelper", "Audio recording stopped. Path: $path")
            return path
        }
        return currentAudioFile?.absolutePath
    }
}
