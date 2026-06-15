package com.trian0.viary.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.concurrent.TimeUnit

fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val fileName = "viary_image_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return null

        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, output)
        }

        file.absolutePath
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        Log.e("FileUtil", "Erro ao salvar imagem", e)
        null
    }
}

fun Date.elapsedTime(): String {
    val now = Date()
    val diffMillis = now.time - this.time

    val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis) % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}