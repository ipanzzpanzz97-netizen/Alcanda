package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.AiProviderProfileEntity
import com.example.data.AiProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ExternalAiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateText(
        profile: AiProviderProfileEntity,
        systemPrompt: String,
        userPrompt: String,
        expectJson: Boolean = false,
        bitmap: Bitmap? = null
    ): String = withContext(Dispatchers.IO) {
        val type = profile.typeEnum()
        if (type.requiresKey && profile.apiKey.isBlank()) {
            error("Kunci API belum dimasukkan untuk ${profile.name}. Sila masukkan API Key di skrin AI Engine.")
        }

        val baseUrl = profile.baseUrl.trim().trimEnd('/').ifBlank { type.defaultBaseUrl }
        val model = profile.modelName.trim().ifBlank { type.defaultModel }

        when (type) {
            AiProviderType.FIREBASE_AI_LOGIC -> {
                error("Firebase AI Logic dikendalikan terus oleh SDK.")
            }
            AiProviderType.GEMINI_CUSTOM_KEY -> {
                callGeminiRest(baseUrl, profile.apiKey.trim(), model, systemPrompt, userPrompt, expectJson, bitmap)
            }
            AiProviderType.ANTHROPIC -> {
                callAnthropicRest(baseUrl, profile.apiKey.trim(), model, systemPrompt, userPrompt, bitmap)
            }
            AiProviderType.OPENAI,
            AiProviderType.GROQ,
            AiProviderType.OPENROUTER,
            AiProviderType.DEEPSEEK,
            AiProviderType.CUSTOM_OPENAI -> {
                callOpenAiCompatible(baseUrl, profile.apiKey.trim(), model, systemPrompt, userPrompt, expectJson, bitmap)
            }
        }
    }

    suspend fun testConnection(profile: AiProviderProfileEntity): String {
        val reply = generateText(
            profile = profile,
            systemPrompt = "You are a connectivity check responder.",
            userPrompt = "Reply with exactly: OK - Connected",
            expectJson = false,
            bitmap = null
        )
        return reply.take(120)
    }

    private fun callOpenAiCompatible(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String,
        expectJson: Boolean,
        bitmap: Bitmap?
    ): String {
        val endpoint = if (baseUrl.endsWith("/chat/completions")) {
            baseUrl
        } else {
            "$baseUrl/chat/completions"
        }

        val messages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", systemPrompt))
            if (bitmap != null) {
                val b64 = bitmapToBase64Jpeg(bitmap)
                val contentParts = JSONArray().apply {
                    put(JSONObject().put("type", "text").put("text", userPrompt))
                    put(
                        JSONObject().put("type", "image_url").put(
                            "image_url",
                            JSONObject().put("url", "data:image/jpeg;base64,$b64")
                        )
                    )
                }
                put(JSONObject().put("role", "user").put("content", contentParts))
            } else {
                put(JSONObject().put("role", "user").put("content", userPrompt))
            }
        }

        val basePayload = JSONObject().apply {
            put("model", model)
            put("messages", messages)
            put("temperature", 0.35)
        }

        val primaryPayload = JSONObject(basePayload.toString()).apply {
            if (expectJson) {
                put("response_format", JSONObject().put("type", "json_object"))
            }
        }

        return try {
            executeOpenAiRequest(endpoint, apiKey, primaryPayload)
        } catch (e: HttpStatusException) {
            if (expectJson && (e.code == 400 || e.code == 422)) {
                // Fallback retry without response_format for providers/models that disallow strict json_object
                executeOpenAiRequest(endpoint, apiKey, basePayload)
            } else {
                throw e
            }
        }
    }

    private class HttpStatusException(val code: Int, message: String) : IllegalStateException(message)

    private fun executeOpenAiRequest(
        endpoint: String,
        apiKey: String,
        payload: JSONObject
    ): String {
        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(payload.toString().toRequestBody(jsonMediaType))
            .header("Content-Type", "application/json")

        if (apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer $apiKey")
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw HttpStatusException(
                    response.code,
                    "HTTP ${response.code}: ${extractErrorMessage(raw)}"
                )
            }
            val json = JSONObject(raw)
            val choices = json.optJSONArray("choices")
            val first = choices?.optJSONObject(0)
            val msg = first?.optJSONObject("message")
            return msg?.optString("content")?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Tiada kandungan jawapan daripada pelayan AI.")
        }
    }

    private fun callAnthropicRest(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String,
        bitmap: Bitmap?
    ): String {
        val endpoint = if (baseUrl.endsWith("/messages")) baseUrl else "$baseUrl/messages"

        val userContent = JSONArray().apply {
            if (bitmap != null) {
                val b64 = bitmapToBase64Jpeg(bitmap)
                put(
                    JSONObject()
                        .put("type", "image")
                        .put(
                            "source",
                            JSONObject()
                                .put("type", "base64")
                                .put("media_type", "image/jpeg")
                                .put("data", b64)
                        )
                )
            }
            put(JSONObject().put("type", "text").put("text", userPrompt))
        }

        val payload = JSONObject().apply {
            put("model", model)
            put("max_tokens", 2048)
            put("system", systemPrompt)
            put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", userContent)))
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(payload.toString().toRequestBody(jsonMediaType))
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Anthropic HTTP ${response.code}: ${extractErrorMessage(raw)}")
            }
            val json = JSONObject(raw)
            val contentArray = json.optJSONArray("content")
            val text = (0 until (contentArray?.length() ?: 0))
                .mapNotNull { idx -> contentArray?.optJSONObject(idx)?.optString("text") }
                .joinToString("\n")
                .trim()
            return text.ifBlank { throw IllegalStateException("Anthropic memulangkan jawapan kosong.") }
        }
    }

    private fun callGeminiRest(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String,
        expectJson: Boolean,
        bitmap: Bitmap?
    ): String {
        val cleanModel = model.removePrefix("models/")
        val endpoint = "$baseUrl/models/$cleanModel:generateContent"

        val parts = JSONArray().apply {
            if (bitmap != null) {
                val b64 = bitmapToBase64Jpeg(bitmap)
                put(
                    JSONObject().put(
                        "inline_data",
                        JSONObject()
                            .put("mime_type", "image/jpeg")
                            .put("data", b64)
                    )
                )
            }
            put(JSONObject().put("text", userPrompt))
        }

        val payload = JSONObject().apply {
            put(
                "system_instruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            )
            put(
                "contents",
                JSONArray().put(JSONObject().put("role", "user").put("parts", parts))
            )
            if (expectJson) {
                put(
                    "generationConfig",
                    JSONObject().put("responseMimeType", "application/json")
                )
            }
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(payload.toString().toRequestBody(jsonMediaType))
            .header("x-goog-api-key", apiKey)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Gemini API HTTP ${response.code}: ${extractErrorMessage(raw)}")
            }
            val json = JSONObject(raw)
            val candidates = json.optJSONArray("candidates")
            val contentObj = candidates?.optJSONObject(0)?.optJSONObject("content")
            val outParts = contentObj?.optJSONArray("parts")
            val text = outParts?.optJSONObject(0)?.optString("text").orEmpty()
            return text.ifBlank { throw IllegalStateException("Gemini API memulangkan jawapan kosong.") }
        }
    }

    private fun bitmapToBase64Jpeg(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 82, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }

    private fun extractErrorMessage(raw: String): String {
        if (raw.isBlank()) return "Ralat pelayan."
        return runCatching {
            val obj = JSONObject(raw)
            obj.optJSONObject("error")?.optString("message")
                ?: obj.optString("message")
                ?: raw.take(220)
        }.getOrDefault(raw.take(220))
    }
}
