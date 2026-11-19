package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user" or "model"
    val text: String,
    val actionBadge: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "termux_scripts")
data class TermuxScriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val language: String,
    val code: String,
    val description: String,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class AiProviderType(
    val displayLabel: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val requiresKey: Boolean
) {
    FIREBASE_AI_LOGIC(
        displayLabel = "NovaCore Built-in (Firebase Gemini)",
        defaultBaseUrl = "",
        defaultModel = "gemini-3.8-flash",
        requiresKey = false
    ),
    GEMINI_CUSTOM_KEY(
        displayLabel = "Google Gemini (API Key)",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta",
        defaultModel = "gemini-2.5-flash",
        requiresKey = true
    ),
    OPENAI(
        displayLabel = "OpenAI (ChatGPT / GPT)",
        defaultBaseUrl = "https://api.openai.com/v1",
        defaultModel = "gpt-4o-mini",
        requiresKey = true
    ),
    ANTHROPIC(
        displayLabel = "Anthropic Claude",
        defaultBaseUrl = "https://api.anthropic.com/v1",
        defaultModel = "claude-3-7-sonnet-latest",
        requiresKey = true
    ),
    GROQ(
        displayLabel = "Groq (Llama / Mixtral / DeepSeek)",
        defaultBaseUrl = "https://api.groq.com/openai/v1",
        defaultModel = "llama-3.3-70b-versatile",
        requiresKey = true
    ),
    OPENROUTER(
        displayLabel = "OpenRouter (Free & Paid Models)",
        defaultBaseUrl = "https://openrouter.ai/api/v1",
        defaultModel = "meta-llama/llama-3.3-70b-instruct:free",
        requiresKey = true
    ),
    DEEPSEEK(
        displayLabel = "DeepSeek API",
        defaultBaseUrl = "https://api.deepseek.com/v1",
        defaultModel = "deepseek-chat",
        requiresKey = true
    ),
    CUSTOM_OPENAI(
        displayLabel = "Custom / Free OpenAI-Compatible",
        defaultBaseUrl = "https://text.pollinations.ai/openai/v1",
        defaultModel = "openai",
        requiresKey = false
    )
}

@Entity(tableName = "ai_provider_profiles")
data class AiProviderProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val providerType: String, // AiProviderType.name
    val apiKey: String,
    val baseUrl: String,
    val modelName: String,
    val isActive: Boolean = false,
    val requiresKey: Boolean = true,
    val isFreeTier: Boolean = true,
    val signupUrl: String = "",
    val badgeText: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun typeEnum(): AiProviderType =
        runCatching { AiProviderType.valueOf(providerType) }.getOrDefault(AiProviderType.FIREBASE_AI_LOGIC)
}

object FreeAiPresets {
    fun defaultProfiles(): List<AiProviderProfileEntity> = listOf(
        AiProviderProfileEntity(
            name = "NovaCore Gemini 3.8 Flash",
            providerType = AiProviderType.FIREBASE_AI_LOGIC.name,
            apiKey = "",
            baseUrl = "",
            modelName = "gemini-3.8-flash",
            isActive = true,
            requiresKey = false,
            isFreeTier = true,
            signupUrl = "",
            badgeText = "FREE • TANPA KEY"
        ),
        AiProviderProfileEntity(
            name = "NovaCore Gemini 3.5 Lite",
            providerType = AiProviderType.FIREBASE_AI_LOGIC.name,
            apiKey = "",
            baseUrl = "",
            modelName = "gemini-3.5-flash-lite",
            isActive = false,
            requiresKey = false,
            isFreeTier = true,
            signupUrl = "",
            badgeText = "FREE • ULTRA LAJU"
        ),
        AiProviderProfileEntity(
            name = "Pollinations AI (OpenAI Free)",
            providerType = AiProviderType.CUSTOM_OPENAI.name,
            apiKey = "",
            baseUrl = "https://text.pollinations.ai/openai/v1",
            modelName = "openai",
            isActive = false,
            requiresKey = false,
            isFreeTier = true,
            signupUrl = "",
            badgeText = "FREE • TANPA KEY"
        ),
        AiProviderProfileEntity(
            name = "Pollinations AI (Mistral Free)",
            providerType = AiProviderType.CUSTOM_OPENAI.name,
            apiKey = "",
            baseUrl = "https://text.pollinations.ai/openai/v1",
            modelName = "mistral",
            isActive = false,
            requiresKey = false,
            isFreeTier = true,
            signupUrl = "",
            badgeText = "FREE • TANPA KEY"
        ),
        AiProviderProfileEntity(
            name = "Groq Free (Llama 3.3 70B)",
            providerType = AiProviderType.GROQ.name,
            apiKey = "",
            baseUrl = "https://api.groq.com/openai/v1",
            modelName = "llama-3.3-70b-versatile",
            isActive = false,
            requiresKey = true,
            isFreeTier = true,
            signupUrl = "https://console.groq.com/keys",
            badgeText = "FREE TIER"
        ),
        AiProviderProfileEntity(
            name = "OpenRouter Free (Llama 70B :free)",
            providerType = AiProviderType.OPENROUTER.name,
            apiKey = "",
            baseUrl = "https://openrouter.ai/api/v1",
            modelName = "meta-llama/llama-3.3-70b-instruct:free",
            isActive = false,
            requiresKey = true,
            isFreeTier = true,
            signupUrl = "https://openrouter.ai/keys",
            badgeText = "FREE TIER"
        ),
        AiProviderProfileEntity(
            name = "Google AI Studio (Free Key)",
            providerType = AiProviderType.GEMINI_CUSTOM_KEY.name,
            apiKey = "",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            modelName = "gemini-2.5-flash",
            isActive = false,
            requiresKey = true,
            isFreeTier = true,
            signupUrl = "https://aistudio.google.com/apikey",
            badgeText = "FREE TIER"
        ),
        AiProviderProfileEntity(
            name = "Cerebras Free (Llama 3.3 70B)",
            providerType = AiProviderType.CUSTOM_OPENAI.name,
            apiKey = "",
            baseUrl = "https://api.cerebras.ai/v1",
            modelName = "llama-3.3-70b",
            isActive = false,
            requiresKey = true,
            isFreeTier = true,
            signupUrl = "https://cloud.cerebras.ai",
            badgeText = "FREE TIER"
        ),
        AiProviderProfileEntity(
            name = "Mistral AI (Free Tier)",
            providerType = AiProviderType.CUSTOM_OPENAI.name,
            apiKey = "",
            baseUrl = "https://api.mistral.ai/v1",
            modelName = "mistral-small-latest",
            isActive = false,
            requiresKey = true,
            isFreeTier = true,
            signupUrl = "https://console.mistral.ai/api-keys",
            badgeText = "FREE TIER"
        ),
        AiProviderProfileEntity(
            name = "GitHub Models Free (GPT-4o Mini)",
            providerType = AiProviderType.CUSTOM_OPENAI.name,
            apiKey = "",
            baseUrl = "https://models.inference.ai.azure.com",
            modelName = "gpt-4o-mini",
            isActive = false,
            requiresKey = true,
            isFreeTier = true,
            signupUrl = "https://github.com/settings/tokens",
            badgeText = "FREE TIER"
        ),
        AiProviderProfileEntity(
            name = "Termux Ollama (Local Offline)",
            providerType = AiProviderType.CUSTOM_OPENAI.name,
            apiKey = "",
            baseUrl = "http://127.0.0.1:11434/v1",
            modelName = "llama3.2",
            isActive = false,
            requiresKey = false,
            isFreeTier = true,
            signupUrl = "",
            badgeText = "OFFLINE • TERMUX"
        )
    )
}

@Entity(tableName = "system_errors")
data class SystemErrorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val module: String, // e.g., "AI_API", "ACCESSIBILITY", "TERMUX", "LUNO", "FILE", "GAME"
    val message: String,
    val suggestedFixKey: String = "", // e.g., "FIX_SWITCH_GEMINI", "FIX_ACCESSIBILITY", "FIX_TERMUX_SETUP", "FIX_RETRY_LUNO"
    val aiDiagnosis: String = "",
    val isResolved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "generated_app_projects")
data class GeneratedAppProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appName: String,
    val packageId: String,
    val prompt: String,
    val summary: String,
    val mainActivityKt: String,
    val manifestXml: String,
    val buildGradleKts: String,
    val stringsXml: String,
    val termuxBuildSh: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()
}

@Dao
interface TermuxScriptDao {
    @Query("SELECT * FROM termux_scripts ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<TermuxScriptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(script: TermuxScriptEntity): Long

    @Delete
    suspend fun delete(script: TermuxScriptEntity)
}

@Dao
interface AiProfileDao {
    @Query("SELECT * FROM ai_provider_profiles ORDER BY isActive DESC, id ASC")
    fun observeAll(): Flow<List<AiProviderProfileEntity>>

    @Query("SELECT * FROM ai_provider_profiles ORDER BY isActive DESC, id ASC")
    suspend fun getAllOnce(): List<AiProviderProfileEntity>

    @Query("SELECT * FROM ai_provider_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveOnce(): AiProviderProfileEntity?

    @Query("SELECT COUNT(*) FROM ai_provider_profiles")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: AiProviderProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<AiProviderProfileEntity>)

    @Query("UPDATE ai_provider_profiles SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE ai_provider_profiles SET isActive = 1 WHERE id = :id")
    suspend fun activateById(id: Long)

    @Delete
    suspend fun delete(profile: AiProviderProfileEntity)
}

@Dao
interface SystemErrorDao {
    @Query("SELECT * FROM system_errors ORDER BY isResolved ASC, timestamp DESC")
    fun observeAll(): Flow<List<SystemErrorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(error: SystemErrorEntity): Long

    @Query("UPDATE system_errors SET isResolved = 1 WHERE id = :id")
    suspend fun markResolved(id: Long)

    @Query("UPDATE system_errors SET aiDiagnosis = :diagnosis WHERE id = :id")
    suspend fun updateDiagnosis(id: Long, diagnosis: String)

    @Query("DELETE FROM system_errors WHERE isResolved = 1")
    suspend fun clearResolved()

    @Query("DELETE FROM system_errors")
    suspend fun clearAll()
}

@Dao
interface GeneratedAppDao {
    @Query("SELECT * FROM generated_app_projects ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GeneratedAppProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: GeneratedAppProjectEntity): Long

    @Delete
    suspend fun delete(project: GeneratedAppProjectEntity)
}

@Database(
    entities = [
        ChatMessageEntity::class,
        TermuxScriptEntity::class,
        AiProviderProfileEntity::class,
        SystemErrorEntity::class,
        GeneratedAppProjectEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun termuxScriptDao(): TermuxScriptDao
    abstract fun aiProfileDao(): AiProfileDao
    abstract fun systemErrorDao(): SystemErrorDao
    abstract fun generatedAppDao(): GeneratedAppDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getInstance(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "novacore_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
