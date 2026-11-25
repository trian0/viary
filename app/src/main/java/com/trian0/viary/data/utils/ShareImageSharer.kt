package com.trian0.viary.data.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.File
import java.io.FileOutputStream
import java.text.Normalizer

private const val SHARE_DIR = "share"
private const val MAX_SLUG_LENGTH = 40

private val diacritics = Regex("\\p{M}+")
private val nonAlphanumeric = Regex("[^a-z0-9]+")

fun shareImageFileName(viaryName: String, timestamp: Long): String {
    val slug = Normalizer.normalize(viaryName.lowercase(), Normalizer.Form.NFD)
        .replace(diacritics, "")
        .replace(nonAlphanumeric, "-")
        .trim('-')
        .take(MAX_SLUG_LENGTH)
        .trim('-')
        .ifEmpty { "viary" }
    return "viary-$slug-$timestamp.png"
}

fun clearShareDir(dir: File) {
    dir.listFiles()?.forEach { it.delete() }
}

/** Grava o PNG no cache e devolve a Uri do FileProvider. Escreve em disco, chame fora da main thread. */
fun writeShareImage(context: Context, bitmap: Bitmap, viaryName: String): Uri? {
    return try {
        val dir = File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
        clearShareDir(dir)

        val file = File(dir, shareImageFileName(viaryName, System.currentTimeMillis()))
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        Log.e("ShareImage", "Erro ao gravar imagem de compartilhamento", e)
        null
    }
}

fun launchShareChooser(context: Context, uri: Uri, chooserTitle: String): Boolean {
    return try {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, chooserTitle))
        true
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        Log.e("ShareImage", "Erro ao abrir o seletor de apps", e)
        false
    }
}
