package com.tany.lecta

import android.content.Context
import android.net.Uri
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

// Change this one line depending on how you run the app (see "Step 6").
const val BASE_URL = "https://lecta-backend-ajw5.onrender.com"

private const val MAX_FILE_BYTES = 10 * 1024 * 1024

class ApiException(message: String) : Exception(message)

data class AnalyzedItem(
    val kind: String,          // "task" or "notice"
    val task: String?,
    val summary: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val priority: Priority,
    val confidence: Int
)

object NoticeApi {

    suspend fun analyzeFile(context: Context, uri: Uri, fileName: String): List<AnalyzedItem> =
        withContext(Dispatchers.IO) {
            val type = context.contentResolver.getType(uri) ?: ""
            if (type != "application/pdf") {
                throw ApiException("Only PDF files are supported for now.")
            }

            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw ApiException("Could not open the selected file.")
            if (bytes.size > MAX_FILE_BYTES) {
                throw ApiException("File is too large (max 10 MB).")
            }

            try {
                parseItems(postPdf(bytes, fileName))
            } catch (e: ApiException) {
                throw e
            } catch (e: IOException) {
                throw ApiException("Could not reach the server. Check the connection and the server address.")
            } catch (e: JSONException) {
                throw ApiException("The server sent an unexpected reply.")
            }
        }
    // Wakes the free server in the background. The answer is ignored.
    suspend fun warmUp() = withContext(Dispatchers.IO) {
        try {
            val connection = URL("$BASE_URL/health").openConnection() as HttpURLConnection
            connection.connectTimeout = 60_000
            connection.readTimeout = 60_000
            connection.responseCode
            connection.disconnect()
        } catch (e: Exception) {
            // ignore: this is only a wake-up call
        }
    }
    // Sends the PDF as a multipart/form-data upload, the same thing the /docs page did for you.
    private fun postPdf(bytes: ByteArray, fileName: String): String {
        val boundary = "----Lecta" + System.currentTimeMillis()
        val safeName = fileName.replace("\"", "").replace("\r", "").replace("\n", "")

        val connection = URL("$BASE_URL/analyze-file").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 60_000   // the free server can take a minute to wake up
            connection.readTimeout = 120_000     // the AI can take a while
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            connection.outputStream.use { out ->
                out.write("--$boundary\r\n".toByteArray())
                out.write("Content-Disposition: form-data; name=\"file\"; filename=\"$safeName\"\r\n".toByteArray())
                out.write("Content-Type: application/pdf\r\n\r\n".toByteArray())
                out.write(bytes)
                out.write("\r\n--$boundary--\r\n".toByteArray())
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (code !in 200..299) {
                // our backend sends {"detail": "message"} for its own errors
                val detail = try {
                    JSONObject(body).optString("detail", "")
                } catch (e: JSONException) {
                    ""
                }
                throw ApiException(detail.ifBlank { "Server error ($code). Please try again." })
            }
            return body
        } finally {
            connection.disconnect()
        }
    }

    private fun parseItems(body: String): List<AnalyzedItem> {
        val array = JSONObject(body).getJSONArray("items")
        return (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            AnalyzedItem(
                kind = o.optString("kind", ""),
                task = o.textOrNull("task"),
                summary = o.textOrNull("summary"),
                startDate = o.textOrNull("start_date")?.let { LocalDate.parse(it) },
                endDate = o.textOrNull("end_date")?.let { LocalDate.parse(it) },
                priority = Priority.values().firstOrNull {
                    it.name.equals(o.optString("priority", ""), ignoreCase = true)
                } ?: Priority.Medium,
                confidence = o.optInt("confidence", 0)
            )
        }
    }

    // The backend leaves out keys that don't apply (a notice has no "task"), so check before reading.
    private fun JSONObject.textOrNull(key: String): String? =
        if (has(key) && !isNull(key)) getString(key) else null
}