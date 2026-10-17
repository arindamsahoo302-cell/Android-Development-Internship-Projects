package com.example.aichatboot20

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "ai_chat_prefs"
        private const val KEY_API_KEY = "gemini_api_key"
        // Enter your Gemini API key here or configure via the in-app settings dialog
        private const val DEFAULT_API_KEY = "YOUR_GEMINI_API_KEY"
    }

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var headerStatusTextView: TextView

    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    private val prefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupListeners()
        updateStatus()

        // Show welcome message in English
        if (messageList.isEmpty()) {
            chatAdapter.addMessage(
                ChatMessage(
                    "Hello! I am your AI Assistant. How can I help you today?",
                    isUser = false
                )
            )
        }
    }

    private fun initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        progressBar = findViewById(R.id.progressBar)
        headerStatusTextView = findViewById(R.id.headerStatusTextView)
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatRecyclerView.adapter = chatAdapter
    }

    private fun setupListeners() {
        sendButton.setOnClickListener {
            val userText = messageEditText.text.toString().trim()
            if (userText.isNotEmpty()) {
                sendMessage(userText)
            }
        }

        // Hidden secret shortcut: long press status to open API key settings if ever needed
        headerStatusTextView.setOnLongClickListener {
            showApiKeyDialog()
            true
        }
    }

    private fun getActiveApiKey(): String {
        val savedKey = prefs.getString(KEY_API_KEY, null)
        return if (!savedKey.isNullOrBlank()) savedKey else DEFAULT_API_KEY
    }

    private fun saveApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key.trim()).apply()
        updateStatus()
        Toast.makeText(this, "API Key saved successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun updateStatus() {
        val key = getActiveApiKey()
        if (key.isBlank() || key == "YOUR_GEMINI_API_KEY") {
            headerStatusTextView.text = "API Connecting..."
            headerStatusTextView.setTextColor(android.graphics.Color.parseColor("#E65100"))
        } else {
            headerStatusTextView.text = "API Connected"
            headerStatusTextView.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        }
    }

    private fun showApiKeyDialog() {
        val currentKey = getActiveApiKey()
        val input = EditText(this).apply {
            hint = "AQ.xxxxxx or AIzaxxxxxx"
            setText(currentKey)
            setSelection(text.length)
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Gemini API Key Settings")
            .setMessage("Enter or update your Google AI Studio API Key below:")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newKey = input.text.toString().trim()
                if (newKey.isNotEmpty()) {
                    saveApiKey(newKey)
                } else {
                    Toast.makeText(this, "API Key cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Get Key") { _, _ ->
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                startActivity(browserIntent)
            }
            .show()
    }

    private fun sendMessage(userText: String) {
        val apiKey = getActiveApiKey()
        if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
            Toast.makeText(this, "Please enter your Gemini API Key", Toast.LENGTH_SHORT).show()
            showApiKeyDialog()
            return
        }

        // Add user message to UI
        chatAdapter.addMessage(ChatMessage(userText, isUser = true))
        messageEditText.text.clear()
        chatRecyclerView.smoothScrollToPosition(messageList.size - 1)

        sendButton.isEnabled = false
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val response = fetchAiResponse(userText, apiKey)
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                sendButton.isEnabled = true
                chatAdapter.addMessage(ChatMessage(response, isUser = false))
                chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
            }
        }
    }

    private fun fetchAiResponse(prompt: String, apiKey: String): String {
        val cleanPrompt = prompt.trim().lowercase()
        if (cleanPrompt.contains("who create u") ||
            cleanPrompt.contains("who created u") ||
            cleanPrompt.contains("who created you") ||
            cleanPrompt.contains("who create you") ||
            cleanPrompt.contains("who made you") ||
            cleanPrompt.contains("who is your creator") ||
            cleanPrompt.contains("who developed you") ||
            cleanPrompt.contains("who is your developer") ||
            cleanPrompt.contains("who built you") ||
            cleanPrompt.contains("creator ke") ||
            cleanPrompt.contains("ke baniyeche") ||
            cleanPrompt.contains("ke toiri koreche")
        ) {
            return "I was created and developed by Arindam Sahoo."
        }

        // Standard official Gemini models in v1beta API (in order of preference)
        val models = listOf(
            "gemini-3.8-flash",
            "gemini-3.5-flash",
            "gemini-flash-latest"
        )

        var lastError = "Request failed"

        for (model in models) {
            try {
                val result = callGeminiModel(model, prompt, apiKey)
                if (result.isSuccess) {
                    return result.getOrThrow()
                } else {
                    val errMsg = result.exceptionOrNull()?.message ?: ""
                    lastError = errMsg

                    // If the request failed due to API Key / Authentication / Bad Request (400 / 403), stop the loop immediately!
                    if (errMsg.contains("API_KEY", ignoreCase = true) ||
                        errMsg.contains("API key", ignoreCase = true) ||
                        errMsg.contains("400", ignoreCase = true) ||
                        errMsg.contains("403", ignoreCase = true) ||
                        errMsg.contains("INVALID_ARGUMENT", ignoreCase = true) ||
                        errMsg.contains("PERMISSION_DENIED", ignoreCase = true)
                    ) {
                        return "API Key Error ($errMsg).\n\nPlease tap or long-press the top 'API Connected' header status bar to enter a valid Gemini API key from Google AI Studio (aistudio.google.com)."
                    }
                }
            } catch (e: Exception) {
                lastError = e.localizedMessage ?: "Network error"
            }
        }

        return "Sorry, no response received: $lastError"
    }

    private fun callGeminiModel(model: String, prompt: String, apiKey: String): Result<String> {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
            // System instruction enforcing English responses and creator identity
            val systemInstruction = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().apply {
                        put(
                            "text",
                            "You are an intelligent, friendly, highly capable AI Assistant. " +
                            "Answer questions clearly, thoroughly, and accurately in English with good formatting, bullet points, or code blocks when helpful. " +
                            "Always respond in English. " +
                            "MANDATORY IDENTITY RULE: If anyone asks who created you, who made you, who built you, who developed you, who is your creator, programmer, or owner (or any variation of this question in any language), you must ALWAYS proudly answer that you were created and developed by Arindam Sahoo."
                        )
                    })
                }
                put("parts", parts)
            }
            put("system_instruction", systemInstruction)

            // Direct Question & Answer (Single Prompt)
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            // Generation configuration
            val generationConfig = JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
                put("maxOutputTokens", 2048)
            }
            put("generationConfig", generationConfig)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-goog-api-key", apiKey)
            .post(requestBody)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotEmpty()) {
                            return Result.success(text)
                        }
                    }
                }
                Result.failure(Exception("Sorry, could not parse Gemini response."))
            } else {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody ?: "")
                    val errObj = errJson.optJSONObject("error")
                    errObj?.optString("message") ?: "HTTP ${response.code}: ${response.message}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: SecurityException) {
            Result.failure(Exception("Permission Error: Internet permission was blocked. Please uninstall the app from your device and re-run/reinstall it."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Network Error: No internet connection found. Please check your Wi-Fi or mobile data."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
