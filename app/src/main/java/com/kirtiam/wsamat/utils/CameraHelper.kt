package com.kirtiam.wsamat.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class CameraHelper(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun captureSilentPhoto(onPhotoCaptured: (String?) -> Unit) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            Log.w("CameraHelper", "CAMERA permission not granted.")
            onPhotoCaptured(null)
            return
        }

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        if (cameraManager == null) {
            onPhotoCaptured(null)
            return
        }

        try {
            val cameraIds = cameraManager.cameraIdList
            if (cameraIds.isEmpty()) {
                onPhotoCaptured(null)
                return
            }

            var selectedCameraId = cameraIds[0]
            for (id in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                if (facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT) {
                    selectedCameraId = id
                    break
                }
            }

            val outputDir = File(context.getExternalFilesDir(null), "emergency_photos").apply {
                if (!exists()) mkdirs()
            }
            val photoFile = File(outputDir, "SOS_Photo_${System.currentTimeMillis()}.jpg")

            val imageReader = ImageReader.newInstance(1280, 720, ImageFormat.JPEG, 2)
            val handler = Handler(Looper.getMainLooper())

            var isCaptured = false

            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                if (image != null && !isCaptured) {
                    isCaptured = true
                    try {
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)

                        FileOutputStream(photoFile).use { out ->
                            out.write(bytes)
                        }
                        Log.d("CameraHelper", "Emergency photo saved: ${photoFile.absolutePath}")
                        onPhotoCaptured(photoFile.absolutePath)
                    } catch (e: Exception) {
                        Log.e("CameraHelper", "Failed to save photo bytes", e)
                        onPhotoCaptured(null)
                    } finally {
                        image.close()
                    }
                } else {
                    image?.close()
                }
            }, handler)

            cameraManager.openCamera(selectedCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    try {
                        val captureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                        captureBuilder.addTarget(imageReader.surface)

                        @Suppress("DEPRECATION")
                        camera.createCaptureSession(
                            listOf(imageReader.surface),
                            object : android.hardware.camera2.CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: android.hardware.camera2.CameraCaptureSession) {
                                    try {
                                        session.capture(captureBuilder.build(), null, handler)
                                        handler.postDelayed({
                                            try { camera.close() } catch (e: Exception) {}
                                        }, 1500)
                                    } catch (e: Exception) {
                                        Log.e("CameraHelper", "Error issuing capture request", e)
                                        camera.close()
                                        if (!isCaptured) onPhotoCaptured(null)
                                    }
                                }

                                override fun onConfigureFailed(session: android.hardware.camera2.CameraCaptureSession) {
                                    camera.close()
                                    if (!isCaptured) onPhotoCaptured(null)
                                }
                            },
                            handler
                        )
                    } catch (e: Exception) {
                        Log.e("CameraHelper", "Error building capture request", e)
                        camera.close()
                        if (!isCaptured) onPhotoCaptured(null)
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    if (!isCaptured) onPhotoCaptured(null)
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    if (!isCaptured) onPhotoCaptured(null)
                }
            }, handler)

            handler.postDelayed({
                if (!isCaptured) {
                    onPhotoCaptured(null)
                }
            }, 5000)

        } catch (e: Exception) {
            Log.e("CameraHelper", "Error in captureSilentPhoto", e)
            onPhotoCaptured(null)
        }
    }
}
