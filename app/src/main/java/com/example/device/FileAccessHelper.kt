package com.example.device

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class SelectedFileInfo(
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val textPreview: String?,
    val isImage: Boolean
)

class FileAccessHelper(private val context: Context) {

    suspend fun inspectUri(uri: Uri): Result<SelectedFileInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri) ?: "application/octet-stream"

            var displayName = uri.lastPathSegment ?: "selected_file"
            var sizeBytes = -1L

            resolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx != -1) displayName = cursor.getString(nameIdx) ?: displayName
                    if (sizeIdx != -1) sizeBytes = cursor.getLong(sizeIdx)
                }
            }

            val isImage = mimeType.startsWith("image/")
            val textPreview = if (!isImage && isReadableText(mimeType, displayName)) {
                resolver.openInputStream(uri)?.use { stream ->
                    val buffer = ByteArray(64 * 1024)
                    val read = stream.read(buffer)
                    if (read > 0) String(buffer, 0, read, Charsets.UTF_8) else ""
                }
            } else {
                null
            }

            SelectedFileInfo(
                uri = uri,
                displayName = displayName,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                textPreview = textPreview,
                isImage = isImage
            )
        }
    }

    suspend fun loadScaledBitmap(uri: Uri, maxDimension: Int = 1280): Result<Bitmap> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val rawBytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Gagal membaca fail imej.")

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bounds)

            var sampleSize = 1
            while (bounds.outWidth / sampleSize > maxDimension || bounds.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts)
                ?: error("Format imej tidak disokong.")

            val rotated = rotateIfNeeded(decoded, rawBytes)
            ensureUnderPayloadLimit(rotated)
        }
    }

    suspend fun writeTextToUri(uri: Uri, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
            } ?: error("Gagal membuka aliran tulis fail.")
        }
    }

    private fun rotateIfNeeded(bitmap: Bitmap, bytes: ByteArray): Bitmap {
        val orientation = runCatching {
            ExifInterface(bytes.inputStream()).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun ensureUnderPayloadLimit(bitmap: Bitmap): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        if (stream.size() < 4 * 1024 * 1024) return bitmap
        val scaled = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * 0.6f).toInt().coerceAtLeast(1),
            (bitmap.height * 0.6f).toInt().coerceAtLeast(1),
            true
        )
        return scaled
    }

    private fun isReadableText(mimeType: String, fileName: String): Boolean {
        if (mimeType.startsWith("text/")) return true
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return ext in setOf(
            "txt", "md", "json", "xml", "yaml", "yml", "sh", "py", "js", "ts",
            "kt", "java", "c", "cpp", "h", "html", "css", "csv", "log", "conf",
            "ini", "toml", "properties", "gradle", "kts", "sql"
        ) || mimeType.contains("json") || mimeType.contains("xml") || mimeType.contains("javascript")
    }
}
