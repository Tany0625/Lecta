package com.tany.lecta

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

object ProfileStore {
    private const val PREFS = "lecta_prefs"
    private const val NAME_KEY = "profile_name"
    private const val IMAGE_FILE = "profile.jpg"
    const val DEFAULT_NAME = "User"

    fun loadName(context: Context): String {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(NAME_KEY, null)
        return if (saved.isNullOrBlank()) DEFAULT_NAME else saved
    }

    fun saveName(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(NAME_KEY, name)
            .apply()
    }

    fun loadImage(context: Context): ImageBitmap? {
        val file = File(context.filesDir, IMAGE_FILE)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
    }

    fun deleteImage(context: Context) {
        File(context.filesDir, IMAGE_FILE).delete()
    }

    fun saveImage(context: Context, uri: Uri): ImageBitmap? {
        return try {
            val resolver = context.contentResolver

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

            var sample = 1
            while (bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) {
                sample *= 2
            }

            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            val orientation = resolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            val degrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            val bitmap = if (degrees != 0f) {
                Bitmap.createBitmap(
                    decoded, 0, 0, decoded.width, decoded.height,
                    Matrix().apply { postRotate(degrees) }, true
                )
            } else {
                decoded
            }

            File(context.filesDir, IMAGE_FILE).outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

object NoticeStore {
    private const val PREFS = "lecta_prefs"
    private const val KEY = "notices"

    fun load(context: Context): List<LectaNotice> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map {
                val o = array.getJSONObject(it)
                LectaNotice(
                    id = o.getInt("id"),
                    text = o.getString("text"),
                    date = LocalDate.parse(o.getString("date"))
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, notices: List<LectaNotice>) {
        val array = JSONArray()
        notices.forEach {
            array.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("text", it.text)
                    put("date", it.date.toString())
                }
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }
}

object TaskStore {
    private const val PREFS = "lecta_prefs"
    private const val KEY = "tasks"
    const val EXPIRY_MS = 24L * 60 * 60 * 1000

    fun isExpired(task: LectaTask, now: Long = System.currentTimeMillis()): Boolean {
        val completed = task.completedAt ?: return false
        return task.done && now - completed >= EXPIRY_MS
    }

    fun load(context: Context): List<LectaTask> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length())
                .map { toTask(array.getJSONObject(it)) }
                .filter { !isExpired(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, tasks: List<LectaTask>) {
        val array = JSONArray()
        tasks.forEach { array.put(toJson(it)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }

    private fun toJson(task: LectaTask): JSONObject = JSONObject().apply {
        put("id", task.id)
        put("title", task.title)
        put("source", task.source)
        put("priority", task.priority.name)
        put("startDate", task.startDate.toString())
        put("endDate", task.endDate.toString())
        put("confidence", task.confidence)
        put("extracted", task.extracted)
        put("done", task.done)
        put("manual", task.manual)
        put("createdAt", task.createdAt)
        put("completedAt", task.completedAt ?: JSONObject.NULL)
    }

    private fun toTask(o: JSONObject): LectaTask = LectaTask(
        id = o.getInt("id"),
        title = o.getString("title"),
        source = o.getString("source"),
        priority = Priority.valueOf(o.getString("priority")),
        startDate = LocalDate.parse(o.getString("startDate")),
        endDate = LocalDate.parse(o.getString("endDate")),
        confidence = o.getInt("confidence"),
        extracted = o.getString("extracted"),
        done = o.getBoolean("done"),
        manual = o.getBoolean("manual"),
        createdAt = o.getLong("createdAt"),
        completedAt = if (o.isNull("completedAt")) null else o.getLong("completedAt")
    )
}
