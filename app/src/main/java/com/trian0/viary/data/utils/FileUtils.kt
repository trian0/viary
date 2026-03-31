package com.trian0.viary.data.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.concurrent.TimeUnit

fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val fileName = "viary_image_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
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