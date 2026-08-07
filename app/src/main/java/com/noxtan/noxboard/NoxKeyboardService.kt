package com.noxtan.noxboard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.inputmethodservice.InputMethodService
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.emoji2.emojipicker.EmojiPickerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.noxtan.noxboard.ui.screens.CredentialVaultPanel
import com.noxtan.noxboard.ui.screens.VaultAccount
import com.noxtan.noxboard.ui.theme.NoxBoardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.noxtan.noxboard.data.NoxDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.draw.clip


class NoxKeyboardService : InputMethodService(), NoxKeyboardView.OnKeyboardActionListener,
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val _suggestionsFlow = MutableStateFlow<List<String>>(emptyList())
    private val _typedWordFlow = MutableStateFlow<String>("")
    private val _suggestionBounds = MutableStateFlow<RectF?>(null)
    private lateinit var suggestionComposeView: ComposeView
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var db: NoxDatabase
    private val trieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val myTrieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val enTrieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val myNextWordEngine = com.noxtan.noxboard.engines.NextWordEngine()
    private val enNextWordEngine = com.noxtan.noxboard.engines.NextWordEngine()
    private val myUserTrieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val enUserTrieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val myUserNextWordEngine = com.noxtan.noxboard.engines.NextWordEngine()
    private val enUserNextWordEngine = com.noxtan.noxboard.engines.NextWordEngine()
    private val tempWordCounts = mutableMapOf<String, Int>()
    private val tempPairCounts = mutableMapOf<Pair<String, String>, Int>()
    private val badWordsSet = mutableSetOf<String>()

    private var currentSuggestionsMap = mapOf<String, String>()

    private fun buildFullSuggestion(prefix: String, prediction: String, isEnglish: Boolean): String {
        val overlap = getOverlapLength(prefix, prediction)
        val basePrefix = prefix.dropLast(overlap)
        return if (basePrefix.isNotEmpty()) {
            if (isEnglish) "$basePrefix $prediction" else "$basePrefix$prediction"
        } else {
            prediction
        }
    }

    private var lastSavedWordCount = 0
    private var lastSavedWordStr = ""
    private var lastSavedWordTime = 0L
    private val temporarilyDeletedWords = mutableSetOf<String>()
    private var myMyanmarEngine: com.noxtan.noxboard.engines.MyanmarInputEngine? = null
    private var isWaitingForVaultUnlock = false

    private var serviceLifecycleOwner: ServiceLifecycleOwner? = null

    private lateinit var frameLayout: FrameLayout
    private lateinit var keyboardView: NoxKeyboardView
    private var composeView: ComposeView? = null
    private var emojiPickerView: EmojiPickerView? = null
    private var currentActionId = EditorInfo.IME_ACTION_NONE
    private var isMultiLine = false

    private lateinit var sharedPrefs: SharedPreferences

    private val DOUBLE_TAP_TIMEOUT = 350L

    private var lastTapKey = ""
    private var lastTapTime = 0L
    private var tapCount = 0

    private var soundPool: SoundPool? = null
    private var mechClickySoundId: Int = -1
    private var mechThockySoundId: Int = -1
    private var mechLinearSoundId: Int = -1
    private var mechTactileSoundId: Int = -1
    private var mechSilentSoundId: Int = -1
    private var bubbleSoundId: Int = -1
    private var iosSoundId: Int = -1
    private var defaultSoundId: Int = -1
    private var typewriterSoundId: Int = -1
    private var woodenSoundId: Int = -1
    private var softThudSoundId: Int = -1
    private var sciFiSoundId: Int = -1
    private val maukchaConsonants = setOf("ခ", "ဂ", "င", "ဒ", "ပ", "ဝ")
    private val myanmarConsonants = setOf(
        "က", "ခ", "ဂ", "ဃ", "င",
        "စ", "ဆ", "ဇ", "ဈ", "ည",
        "ဋ", "ဌ", "ဍ", "ဎ", "ဏ",
        "တ", "ထ", "ဒ", "ဓ", "န",
        "ပ", "ဖ", "ဗ", "ဘ", "မ",
        "ယ", "ရ", "လ", "ဝ", "သ",
        "ဟ", "ဠ", "အ"
    )

    private val doubleTapMap = mapOf(
        "ဆ" to "ဈ", "ဈ" to "ဈ", "က" to "ဿ", "ဿ" to "ဿ", "သ" to "ဥ", "ဥ" to "ဥ",
        "ေ" to "ဗ", "ဗ" to "ဗ", "ျ" to "ှ", "ှ" to "ှ", "ိ" to "ီ", "ီ" to "ီ",
        "်" to "ွ", "ွ" to "ွ", "့" to "ံ", "ံ" to "ံ", "ြ" to "ဲ", "ဲ" to "ဲ",
        "ု" to "ဒ", "ဒ" to "ဒ", "ူ" to "ဓ", "ဓ" to "ဓ", "ဖ" to "ဇ", "ဇ" to "ဇ",
        "ထ" to "ဌ", "ဌ" to "ဌ", "ခ" to "ဃ", "ဃ" to "ဃ", "လ" to "ဠ", "ဠ" to "ဠ",
        "ဘ" to "၊", "၊" to "၊", "ည" to "ဉ", "ဉ" to "ဉ", "ာ" to "ါ", "ါ" to "ါ",
        "ယ" to "။", "။" to "။", "ဏ" to "သြ", "သြ" to "သြ", "ရ" to "ဌ", "ဌ" to "ဌ",
        "ဂ" to "ဃ", "ဃ" to "ဃ", "ဝ" to "ဠ", "ဠ" to "ဠ", "ဟ" to "ဦ", "ဦ" to "ဦ"
    )

    private val autoCorrectMap = mutableMapOf<String, String>()

    override fun onCreate() {
        super.onCreate()

        com.noxtan.noxboard.utils.NoxLogger.init(this)

        try {
            serviceLifecycleOwner = ServiceLifecycleOwner().apply {
                onCreate()
            }
            Log.d("NoxBoard", "ServiceLifecycleOwner initialized successfully")
        } catch (e: Throwable) {
            Log.e("NoxBoard", "Failed to initialize ServiceLifecycleOwner", e)
        }

        sharedPrefs = getSharedPreferences("noxboard_prefs", MODE_PRIVATE)
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
        initSoundPool()
        loadMyanmarEngine()
        db = NoxDatabase.getDatabase(this)
        loadDictionaryFromAssets()
        loadUserDictionary()
    }

    private fun getOverlapLength(prefix: String, suggestion: String): Int {
        val minLen = minOf(prefix.length, suggestion.length)
        for (i in minLen downTo 1) {
            if (prefix.endsWith(suggestion.substring(0, i), ignoreCase = true)) {
                return i
            }
        }
        return 0
    }

    private fun updateSuggestions() {
        val editorInfo = currentInputEditorInfo
        if (editorInfo != null) {
            val inputType = editorInfo.inputType
            val cls = inputType and android.text.InputType.TYPE_MASK_CLASS
            val variation = inputType and android.text.InputType.TYPE_MASK_VARIATION

            val isPassword = (cls == android.text.InputType.TYPE_CLASS_TEXT &&
                    (variation == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                            variation == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                            variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)) ||
                    (cls == android.text.InputType.TYPE_CLASS_NUMBER &&
                            variation == android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD)

            if (isPassword) {
                _suggestionsFlow.value = emptyList()
                _typedWordFlow.value = ""
                currentSuggestionsMap = emptyMap()
                return
            }
        }

        val prefs = NoxBoardPrefs(this)
        val showSystemSuggestions = prefs.isSuggestionEnabled
        val showUserSuggestions = !prefs.isIncognitoModeEnabled

        if (!showSystemSuggestions && !showUserSuggestions) {
            _suggestionsFlow.value = emptyList()
            _typedWordFlow.value = ""
            currentSuggestionsMap = emptyMap()
            return
        }

        val ic = currentInputConnection ?: run {
            _suggestionsFlow.value = emptyList()
            currentSuggestionsMap = emptyMap()
            return
        }

        val textBefore = ic.getTextBeforeCursor(50, 0)?.toString() ?: ""
        val words = textBefore.split(Regex("\\s+"))
        val lastWord = words.lastOrNull() ?: ""

        if (lastWord.isNotBlank()) {
            val cleanPrefix = lastWord.replace("\u200C", "")
            _typedWordFlow.value = lastWord

            if (cleanPrefix.length >= 1) {
                val isEnglish = keyboardView.currentMode == com.noxtan.noxboard.KeyboardMode.ENGLISH
                val newSuggestionsMap = mutableMapOf<String, String>()
                val suggestionsList = mutableListOf<String>()

                android.util.Log.d("NoxDebug", "--- Typing Word: '$cleanPrefix' ---")
                android.util.Log.d("NoxDebug", "Show System: $showSystemSuggestions, Show User: $showUserSuggestions")

                val currentEditor = currentInputEditorInfo
                var isEmailField = false
                if (currentEditor != null) {
                    val inputType = currentEditor.inputType
                    val cls = inputType and android.text.InputType.TYPE_MASK_CLASS
                    val variation = inputType and android.text.InputType.TYPE_MASK_VARIATION
                    isEmailField = cls == android.text.InputType.TYPE_CLASS_TEXT &&
                            (variation == android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                                    variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS)
                }

                val shouldShowEmails = isEmailField || (cleanPrefix.contains("@") && cleanPrefix.indexOf("@") > 0)

                android.util.Log.d("NoxDebug", "Email Check -> isEmailField: $isEmailField, shouldShowEmails: $shouldShowEmails")

                if (shouldShowEmails) {
                    val domains = listOf("gmail.com", "outlook.com", "proton.me", "yahoo.com", "icloud.com")
                    if (!cleanPrefix.contains("@")) {
                        domains.forEach { domain ->
                            val display = "@$domain"
                            val fullEmail = "$cleanPrefix@$domain"
                            suggestionsList.add(display)
                            newSuggestionsMap[display] = fullEmail
                        }
                    } else {
                        val parts = cleanPrefix.split("@")
                        if (parts.size == 2) {
                            val name = parts[0]
                            val domainPrefix = parts[1].lowercase()
                            domains.filter { it.startsWith(domainPrefix) }.forEach { domain ->
                                val display = domain
                                val fullEmail = "$name@$domain"
                                suggestionsList.add(display)
                                newSuggestionsMap[display] = fullEmail
                            }
                        }
                    }
                }

                if (isEnglish) {
                    val prevWordRaw = if (words.size >= 2) words[words.size - 2] else ""
                    val prevWord = prevWordRaw.replace(Regex("[.,!?။၊:;\"'()\\[\\]{}]"), "").replace("\u200C", "")

                    if (prevWord.isNotBlank() && prefs.isNextWordPredictionEnabled) {
                        val userNextWords = if (showUserSuggestions) enUserNextWordEngine.getExactNextWords(prevWord).filter { it.startsWith(cleanPrefix, ignoreCase = true) } else emptyList()
                        val systemNextWords = if (showSystemSuggestions) enNextWordEngine.getExactNextWords(prevWord).filter { it.startsWith(cleanPrefix, ignoreCase = true) } else emptyList()

                        (userNextWords + systemNextWords).distinct().map { adjustCasing(it, cleanPrefix) }.forEach { pred ->
                            if (!suggestionsList.contains(pred)) { suggestionsList.add(pred); newSuggestionsMap[pred] = pred }
                        }
                    }

                    val userTrie = if (showUserSuggestions) enUserTrieEngine.searchPrefix(cleanPrefix.lowercase()) else emptyList()
                    val systemTrie = if (showSystemSuggestions) enTrieEngine.searchPrefix(cleanPrefix.lowercase()) else emptyList()

                    android.util.Log.d("NoxDebug", "User Trie: $userTrie")
                    android.util.Log.d("NoxDebug", "System Trie: $systemTrie")

                    (userTrie + systemTrie).distinct().map { adjustCasing(it, cleanPrefix) }.forEach { pred ->
                        if (!suggestionsList.contains(pred)) { suggestionsList.add(pred); newSuggestionsMap[pred] = pred }
                    }

                    if (showSystemSuggestions && cleanPrefix.length >= 2) {
                        val typoResults = enNextWordEngine.getMissingSpaceWords(cleanPrefix)
                        typoResults.forEach { rawCombined ->
                            val display = adjustCasing(rawCombined, cleanPrefix)
                            if (!suggestionsList.contains(display)) { suggestionsList.add(display); newSuggestionsMap[display] = rawCombined }
                        }
                    }
                } else {
                    val userTrie = if (showUserSuggestions) myUserTrieEngine.searchPrefix(cleanPrefix) else emptyList()
                    val systemTrie = if (showSystemSuggestions) myTrieEngine.searchPrefix(cleanPrefix) else emptyList()

                    android.util.Log.d("NoxDebug", "User Trie: $userTrie")
                    android.util.Log.d("NoxDebug", "System Trie: $systemTrie")

                    (userTrie + systemTrie).distinct().forEach { pred ->
                        if (!suggestionsList.contains(pred)) {
                            suggestionsList.add(pred)
                            newSuggestionsMap[pred] = pred
                        }
                    }
                }

                currentSuggestionsMap = newSuggestionsMap

                val garbageWords = setOf("re", "ll", "ve", "m", "d", "s", "t")
                val cleanSuggestionsList = suggestionsList.filter {
                    !(isEnglish && garbageWords.contains(it.lowercase()))
                }

                val finalSuggestions = if (prefs.isBlockOffensiveWordsEnabled) {
                    cleanSuggestionsList.filter { suggestion ->
                        val raw = currentSuggestionsMap[suggestion] ?: suggestion
                        badWordsSet.none { badWord -> raw.contains(badWord) }
                    }.take(6)
                } else {
                    cleanSuggestionsList.take(6)
                }

                android.util.Log.d("NoxDebug", "Final Suggestions: $finalSuggestions")
                _suggestionsFlow.value = finalSuggestions

            } else {
                _suggestionsFlow.value = emptyList()
                currentSuggestionsMap = emptyMap()
            }
        } else {
            _typedWordFlow.value = ""

            if (!prefs.isNextWordPredictionEnabled) {
                _suggestionsFlow.value = emptyList()
                currentSuggestionsMap = emptyMap()
                return
            }

            val prevWordRaw = if (words.size >= 2) words[words.size - 2] else ""
            val prevWord = prevWordRaw.replace(Regex("[.,!?။၊:;\"'()\\[\\]{}]"), "").replace("\u200C", "")

            android.util.Log.d("NoxDebug", "--- Space Pressed. Prev Word: '$prevWord' ---")

            if (prevWord.isNotBlank() && prevWord.length >= 1) {
                val isEnglish = keyboardView.currentMode == com.noxtan.noxboard.KeyboardMode.ENGLISH

                val showSystemSuggestions = prefs.isSuggestionEnabled
                val showUserSuggestions = !prefs.isIncognitoModeEnabled

                val suggestions = if (isEnglish) {
                    val userNext = if (showUserSuggestions) enUserNextWordEngine.getExactNextWords(prevWord) else emptyList()
                    val sysNext = if (showSystemSuggestions) enNextWordEngine.getExactNextWords(prevWord) else emptyList()
                    (userNext + sysNext).distinct()
                } else {
                    val userNext = if (showUserSuggestions) myUserNextWordEngine.getExactNextWords(prevWord) else emptyList()
                    val sysNext = if (showSystemSuggestions) myNextWordEngine.getExactNextWords(prevWord) else emptyList()
                    (userNext + sysNext).distinct()
                }

                android.util.Log.d("NoxDebug", "Next Word Suggestions: $suggestions")

                val newSuggestionsMap = mutableMapOf<String, String>()
                suggestions.forEach { newSuggestionsMap[it] = it }
                currentSuggestionsMap = newSuggestionsMap

                val garbageWords = setOf("re", "ll", "ve", "m", "d", "s", "t")
                val cleanSuggestions = suggestions.filter {
                    !(isEnglish && garbageWords.contains(it.lowercase()))
                }

                _suggestionsFlow.value = if (prefs.isBlockOffensiveWordsEnabled) {
                    cleanSuggestions.filter { suggestion ->
                        badWordsSet.none { badWord -> suggestion.contains(badWord) }
                    }.take(6)
                } else {
                    cleanSuggestions.take(6)
                }
            } else {
                _suggestionsFlow.value = emptyList()
                currentSuggestionsMap = emptyMap()
            }
        }
        if (::keyboardView.isInitialized) {
            val hasSuggestions = _suggestionsFlow.value.isNotEmpty() || _typedWordFlow.value.isNotEmpty()
            keyboardView.setSuggestionsActive(hasSuggestions)
        }
    }

    private fun adjustCasing(word: String, prefix: String): String {
        if (prefix.isEmpty()) return word
        return when {
            prefix.all { it.isUpperCase() } -> word.uppercase()
            prefix[0].isUpperCase() -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            else -> word
        }
    }

    private fun checkAutoCapitalization() {
        if (!::keyboardView.isInitialized) return
        val prefs = NoxBoardPrefs(this)

        if (keyboardView.currentMode != com.noxtan.noxboard.KeyboardMode.ENGLISH) return

        if (!prefs.isAutoCapitalizationEnabled) return

        val ic = currentInputConnection ?: return
        val editorInfo = currentInputEditorInfo ?: return

        var shouldShift = false
        if (editorInfo.inputType != android.text.InputType.TYPE_NULL) {
            val capsMode = ic.getCursorCapsMode(editorInfo.inputType)
            shouldShift = (capsMode and android.text.TextUtils.CAP_MODE_SENTENCES) != 0 ||
                    (capsMode and android.text.TextUtils.CAP_MODE_WORDS) != 0 ||
                    (capsMode and android.text.TextUtils.CAP_MODE_CHARACTERS) != 0
        }

        if (!shouldShift) {
            val textBefore = ic.getTextBeforeCursor(3, 0)?.toString() ?: ""
            shouldShift = textBefore.isEmpty() ||
                    textBefore.endsWith(". ") ||
                    textBefore.endsWith("? ") ||
                    textBefore.endsWith("! ") ||
                    textBefore.endsWith("\n")
        }

        keyboardView.setAutoShift(shouldShift)
    }

    private fun performAutoCorrect(ic: android.view.inputmethod.InputConnection) {
        val textBefore = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""

        val match = Regex("([a-zA-Z'\u1000-\u109F\u200C]+)\$").find(textBefore)

        if (match != null) {
            val lastWord = match.value
            val cleanWord = lastWord.replace("\u200C", "")

            val correctedWord = autoCorrectMap[cleanWord.lowercase()]

            if (correctedWord != null) {
                val finalCorrected = if (cleanWord.isNotEmpty() && cleanWord.first().isUpperCase()) {
                    correctedWord.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                } else {
                    correctedWord
                }

                ic.deleteSurroundingText(lastWord.length, 0)
                ic.commitText(finalCorrected, 1)
                myMyanmarEngine?.resetState()
            }
        }
    }

    private fun insertSuggestion(displayWord: String) {
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(50, 0)?.toString() ?: ""

        val words = textBefore.split(Regex("\\s+"))
        val lastWord = words.lastOrNull() ?: ""
        val cleanPrefix = lastWord.replace("\u200C", "")

        val isEnglish = keyboardView.currentMode == com.noxtan.noxboard.KeyboardMode.ENGLISH
        val prefs = NoxBoardPrefs(this)

        val rawPrediction = currentSuggestionsMap[displayWord] ?: displayWord

        val isVerbatimTap = displayWord == _typedWordFlow.value

        if (isVerbatimTap && !prefs.isIncognitoModeEnabled && isValidSpelling(rawPrediction)) {
            val cleanWord = rawPrediction.replace("\u200C", "")
            val isMyanmar = cleanWord.any { it in '\u1000'..'\u109F' }

            android.util.Log.d("NoxDebug", "insertSuggestion: Preview tapped! Force saving '$cleanWord'")

            if (isMyanmar) myUserTrieEngine.insert(cleanWord, 1) else enUserTrieEngine.insert(cleanWord, 1)

            tempWordCounts[cleanWord] = 3

            serviceScope.launch {
                try { db.suggestionDao().upsert(cleanWord, "", 1) } catch (e: Exception) {}
            }

            if (words.size >= 2) {
                val prevWord = words[words.size - 2].trim().replace(Regex("[.,!?။၊:;\"'()\\[\\]{}]"), "").replace("\u200C", "")
                if (prevWord.isNotBlank() && isValidSpelling(prevWord)) {
                    if (isMyanmar) myUserNextWordEngine.insert(prevWord, cleanWord, 1) else enUserNextWordEngine.insert(prevWord, cleanWord, 1)
                    val pair = Pair(prevWord, cleanWord)
                    tempPairCounts[pair] = 3
                    serviceScope.launch {
                        try { db.suggestionDao().upsert(prevWord, cleanWord, 1) } catch (e: Exception) {}
                    }
                }
            }
        } else {
            val triggerKey = if (isEnglish) {
                enUserNextWordEngine.findTriggerKey(cleanPrefix, rawPrediction)
                    ?: enNextWordEngine.findTriggerKey(cleanPrefix, rawPrediction)
            } else {
                myUserNextWordEngine.findTriggerKey(cleanPrefix, rawPrediction)
                    ?: myNextWordEngine.findTriggerKey(cleanPrefix, rawPrediction)
            }

            if (triggerKey != null && !prefs.isIncognitoModeEnabled) {
                if (isEnglish) enUserNextWordEngine.insert(triggerKey, rawPrediction, 1) else myUserNextWordEngine.insert(triggerKey, rawPrediction, 1)
                serviceScope.launch {
                    try { db.suggestionDao().upsert(triggerKey, rawPrediction, 1) } catch (e: Exception) {}
                }
            }
        }

        if (lastWord.isNotEmpty()) {
            ic.deleteSurroundingText(lastWord.length, 0)
        }

        val textToCommit = if (rawPrediction.contains("@")) rawPrediction else displayWord
        ic.commitText("$textToCommit ", 1)

        myMyanmarEngine?.resetState()
        saveTypingData()
        updateSuggestions()
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        serviceScope.launch {
            try {
                val soundPaths = com.noxtan.noxboard.utils.SoundGenerator.generateSounds(this@NoxKeyboardService)
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    soundPool?.let { pool ->
                        soundPaths["DEFAULT"]?.let { defaultSoundId = pool.load(it, 1) }
                        soundPaths["MECH_CLICKY"]?.let { mechClickySoundId = pool.load(it, 1) }
                        soundPaths["MECH_THOCKY"]?.let { mechThockySoundId = pool.load(it, 1) }
                        soundPaths["MECH_LINEAR"]?.let { mechLinearSoundId = pool.load(it, 1) }
                        soundPaths["MECH_TACTILE"]?.let { mechTactileSoundId = pool.load(it, 1) }
                        soundPaths["MECH_SILENT"]?.let { mechSilentSoundId = pool.load(it, 1) }
                        soundPaths["BUBBLE"]?.let { bubbleSoundId = pool.load(it, 1) }
                        soundPaths["IOS"]?.let { iosSoundId = pool.load(it, 1) }
                        soundPaths["TYPEWRITER"]?.let { typewriterSoundId = pool.load(it, 1) }
                        soundPaths["WOODEN"]?.let { woodenSoundId = pool.load(it, 1) }
                        soundPaths["SOFT_THUD"]?.let { softThudSoundId = pool.load(it, 1) }
                        soundPaths["SCI_FI"]?.let { sciFiSoundId = pool.load(it, 1) }
                    }
                }
            } catch (e: Exception) {
                Log.e("NoxBoard", "Failed to generate custom sounds", e)
            }
        }
    }

    private fun loadMyanmarEngine() {
        val prefs = NoxBoardPrefs(this)
        myMyanmarEngine = when (prefs.myanmarTypingStyle) {
            "UNICODE" -> com.noxtan.noxboard.engines.UnicodeInputEngine()
            "VISUAL_SMART" -> com.noxtan.noxboard.engines.VisualSmartInputEngine()
            else -> com.noxtan.noxboard.engines.ZawgyiInputEngine()
        }

        val masterDoubleTapMap = mapOf(
            "ဆ" to "ဈ", "က" to "ဿ", "သ" to "ဥ", "ေ" to "ဗ", "ျ" to "ှ",
            "ိ" to "ီ", "်" to "ွ", "့" to "ံ", "ြ" to "ဲ", "ု" to "ဒ",
            "ူ" to "ဓ", "ဖ" to "ဇ", "ထ" to "ဌ", "ခ" to "ဃ", "လ" to "ဠ",
            "ဘ" to "၊", "ည" to "ဉ", "ာ" to "ါ", "ယ" to "။", "ဏ" to "ဩ",
            "ရ" to "ဋ္ဌ", "ဂ" to "ဏ္ဍ", "ဝ" to "ဋ", "ဟ" to "ဍ"
        )

        val savedStates = try { org.json.JSONObject(prefs.doubleTapStates) } catch (e: Exception) { org.json.JSONObject() }
        val activeMap = mutableMapOf<String, String>()

        masterDoubleTapMap.forEach { (key, value) ->
            val isEnabled = if (savedStates.has(key)) savedStates.getBoolean(key) else true
            if (isEnabled) {
                activeMap[key] = value
            }
        }
        myMyanmarEngine?.activeDoubleTapMap = activeMap

        Log.d("NoxBoard", "Myanmar Engine Loaded: ${prefs.myanmarTypingStyle}")
    }

    private fun loadDictionaryFromAssets() {
        serviceScope.launch {
            try {
                assets.open("dict_my.txt").bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val parts = line.split(",")
                        if (parts.size >= 3) {
                            val w1 = parts[0].trim()
                            val w2 = parts[1].trim()
                            val freq = parts[2].trim().toIntOrNull() ?: 1
                            myNextWordEngine.insert(w1, w2, freq)
                            myTrieEngine.insert(w1, freq)
                            myTrieEngine.insert(w2, freq)
                        } else if (parts.isNotEmpty()) {
                            myTrieEngine.insert(parts[0].trim(), parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 1)
                        }
                    }
                }

                assets.open("dict_en.txt").bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val parts = line.split(",")
                        if (parts.size >= 3) {
                            val w1 = parts[0].trim()
                            val w2 = parts[1].trim()
                            val freq = parts[2].trim().toIntOrNull() ?: 1
                            enNextWordEngine.insert(w1, w2, freq)
                            enTrieEngine.insert(w1, freq)
                            enTrieEngine.insert(w2, freq)
                        } else if (parts.isNotEmpty()) {
                            enTrieEngine.insert(parts[0].trim(), parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 1)
                        }
                    }
                }

                val autoCorrectFiles = listOf("auto_correct_en.txt", "auto_correct_my.txt")
                for (fileName in autoCorrectFiles) {
                    try {
                        assets.open(fileName).bufferedReader().useLines { lines ->
                            lines.forEach { line ->
                                val parts = line.split(",")
                                if (parts.size >= 2) {
                                    autoCorrectMap[parts[0].trim().lowercase()] = parts[1].trim()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("NoxBoard", "Error loading $fileName", e)
                    }
                }

                Log.d("NoxBoard", "All Dictionaries, Bad Words & Auto-Corrects loaded!")
            } catch (e: Exception) {
                com.noxtan.noxboard.utils.NoxLogger.logError("Dictionary", "Failed to load dictionary from assets", e)
            }
        }
    }

    private fun isValidSpelling(word: String): Boolean {
        val cleanWord = word.replace("\u200C", "")
        if (cleanWord.isEmpty()) return false

        val isMyanmar = cleanWord.any { it in '\u1000'..'\u109F' }

        if (isMyanmar) {
            val hasBaseChar = cleanWord.any { it in '\u1000'..'\u102A' || it == '\u103F' || it in '\u104C'..'\u104F' }
            if (!hasBaseChar) return false

            val firstChar = cleanWord.first()
            if (firstChar in '\u102B'..'\u103E') return false

            for (i in 0 until cleanWord.length - 1) {
                val c1 = cleanWord[i]
                val c2 = cleanWord[i+1]
                if (c1 == c2 && c1 in '\u102B'..'\u103E') return false
            }
        }
        return true
    }

    private fun loadUserDictionary() {
        serviceScope.launch {
            try {
                db.suggestionDao().cleanupOldWords()
                val userWords = db.suggestionDao().getAllSuggestions()
                userWords.forEach { entity ->
                    val isMyanmar = entity.word.any { it in '\u1000'..'\u109F' }
                    if (entity.nextWord.isBlank()) {
                        if (isMyanmar) myUserTrieEngine.insert(entity.word, entity.frequency) else enUserTrieEngine.insert(entity.word, entity.frequency)
                    } else {
                        if (isMyanmar) myUserNextWordEngine.insert(entity.word, entity.nextWord, entity.frequency) else enUserNextWordEngine.insert(entity.word, entity.nextWord, entity.frequency)
                    }
                }
                android.util.Log.d("NoxBoard", "User dictionary loaded into RAM!")
            } catch (e: Exception) {
                android.util.Log.e("NoxBoard", "Failed to load user dictionary", e)
                com.noxtan.noxboard.utils.NoxLogger.logError("Dictionary", "Failed to load user dictionary", e)
            }
        }
    }

    fun playKeypressSound() {
        val prefs = NoxBoardPrefs(this)
        if (!prefs.isSoundEnabled) return

        val volume = prefs.soundVolume
        when (prefs.selectedSoundPack) {
            "MECH_CLICKY", "MECHANICAL" -> {
                if (mechClickySoundId != -1) soundPool?.play(mechClickySoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
            "MECH_THOCKY" -> {
                if (mechThockySoundId != -1) soundPool?.play(mechThockySoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
            "MECH_LINEAR" -> {
                if (mechLinearSoundId != -1) soundPool?.play(mechLinearSoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
            "MECH_TACTILE" -> {
                if (mechTactileSoundId != -1) soundPool?.play(mechTactileSoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
            "MECH_SILENT" -> {
                if (mechSilentSoundId != -1) soundPool?.play(mechSilentSoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
            "BUBBLE" -> {
                if (bubbleSoundId != -1) soundPool?.play(bubbleSoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
            "IOS" -> {
                if (iosSoundId != -1) soundPool?.play(iosSoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
            "TYPEWRITER" -> {
                if (typewriterSoundId != -1) soundPool?.play(typewriterSoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
            "WOODEN" -> {
                if (woodenSoundId != -1) soundPool?.play(woodenSoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
            "SOFT_THUD" -> {
                if (softThudSoundId != -1) soundPool?.play(softThudSoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
            "SCI_FI" -> {
                if (sciFiSoundId != -1) soundPool?.play(sciFiSoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
            else -> {
                if (defaultSoundId != -1) soundPool?.play(defaultSoundId, volume, volume, 1, 0, 1f) else playSystemSound()
            }
        }
    }

    private fun playSystemSound() {
        val am = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        am.playSoundEffect(android.media.AudioManager.FX_KEYPRESS_STANDARD)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            serviceLifecycleOwner?.onDestroy()
            serviceLifecycleOwner = null
        } catch (e: Throwable) {
            Log.e("NoxBoard", "Error during onDestroy lifecycle release", e)
        }

        if (::sharedPrefs.isInitialized) {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
        }
        soundPool?.release()
        soundPool = null
        serviceJob.cancel()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "clear_dict_trigger") {
            myUserTrieEngine.clear()
            enUserTrieEngine.clear()
            myUserNextWordEngine.clear()
            enUserNextWordEngine.clear()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                updateSuggestions()
            }
            return
        }

        if (key == "font_size" || key == "keyboard_height" ||
            key == "keyboard_width" || key == "keyboard_alignment" ||
            key == "key_corner_radius" || key == "key_padding" ||
            key == "bottom_padding" || key == "is_sliding" ||
            key == "slide_width" || key == "slide_height" ||
            key == "slide_bottom_padding" || key == "slide_alignment" ||
            key == "sound_volume" || key == "selected_sound_pack" ||
            key == "custom_wallpaper_uri" || key == "custom_wallpaper_scale" ||
            key == "custom_wallpaper_offset_x" || key == "custom_wallpaper_offset_y" ||
            key == "custom_bg_color" ||
            key == "custom_key_color" || key == "custom_key_border_color" ||
            key == "custom_text_color" || key == "custom_special_key_color" || key == "custom_special_text_color" ||
            key == "custom_active_key_color" || key == "custom_popup_bg_color" || key == "custom_popup_text_color" ||
            key == "number_row_enabled" ||
            key == "myanmar_typing_style" ||
            key == "space_cursor_control" || key == "space_cursor_sensitivity" || key == "space_drag_delay" ||
            key == "global_row_gap" || key == "individual_row_gaps" ||
            key?.startsWith("icon_style_") == true) {

            loadMyanmarEngine()

            if (::keyboardView.isInitialized) {
                keyboardView.post {
                    keyboardView.applySettings(NoxBoardPrefs(this))
                    applyNavigationBarTheme()
                }
            }
        }
    }

    private fun saveTypingData() {
        val prefs = NoxBoardPrefs(this)
        if (prefs.isIncognitoModeEnabled) return

        val editorInfo = currentInputEditorInfo
        if (editorInfo != null) {
            val inputType = editorInfo.inputType
            val cls = inputType and android.text.InputType.TYPE_MASK_CLASS
            val variation = inputType and android.text.InputType.TYPE_MASK_VARIATION

            val isPassword = (cls == android.text.InputType.TYPE_CLASS_TEXT &&
                    (variation == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                            variation == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                            variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)) ||
                    (cls == android.text.InputType.TYPE_CLASS_NUMBER &&
                            variation == android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD)

            val isNoLearning = (editorInfo.imeOptions and android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0

            if (isPassword || isNoLearning) {
                android.util.Log.d("NoxDebug", "saveTypingData: Password/Incognito field detected. Aborting.")
                return
            }
        }

        val ic = currentInputConnection ?: return
        val textBeforeRaw = ic.getTextBeforeCursor(100, 0)?.toString() ?: return
        val textBefore = textBeforeRaw.trimEnd()
        if (textBefore.isEmpty()) return

        val words = textBefore.split(Regex("\\s+"))

        if (words.isNotEmpty()) {
            val lastWordRaw = words.last().trim()
            val lastWord = lastWordRaw.replace(Regex("[.,!?။၊:;\"'()\\[\\]{}]"), "")

            if (lastWord.isBlank()) return

            if (words.size == lastSavedWordCount && lastWord == lastSavedWordStr) return
            lastSavedWordCount = words.size
            lastSavedWordStr = lastWord
            lastSavedWordTime = System.currentTimeMillis()

            if (lastWord.length > 25) return
            if (lastWord.any { it.isDigit() }) return
            if (android.util.Patterns.WEB_URL.matcher(lastWord).matches()) return
            if (!isValidSpelling(lastWord)) return

            val cleanWord = lastWord.replace("\u200C", "")
            if (temporarilyDeletedWords.contains(cleanWord)) return
            if (prefs.isBlockOffensiveWordsEnabled && badWordsSet.any { cleanWord.contains(it) }) return

            val isMyanmar = cleanWord.any { it in '\u1000'..'\u109F' }
            val finalCleanWord = cleanWord

            val wordCount = (tempWordCounts[finalCleanWord] ?: 0) + 1
            tempWordCounts[finalCleanWord] = wordCount

            android.util.Log.d("NoxDebug", "saveTypingData: '$finalCleanWord' typed $wordCount times")

            var finalPrevWord = ""
            var pairCount = 0
            if (words.size >= 2) {
                val prevWordRaw = words[words.size - 2].trim()
                val prevWord = prevWordRaw.replace(Regex("[.,!?။၊:;\"'()\\[\\]{}]"), "").replace("\u200C", "")
                if (prevWord.isNotBlank() && isValidSpelling(prevWord) && !temporarilyDeletedWords.contains(prevWord)) {
                    finalPrevWord = prevWord
                    val pair = Pair(finalPrevWord, finalCleanWord)
                    pairCount = (tempPairCounts[pair] ?: 0) + 1
                    tempPairCounts[pair] = pairCount
                }
            }

            if (wordCount % 3 == 0) {
                android.util.Log.d("NoxDebug", "saveTypingData: Inserting '$finalCleanWord' permanently")
                if (isMyanmar) myUserTrieEngine.insert(finalCleanWord, 1) else enUserTrieEngine.insert(finalCleanWord, 1)

                serviceScope.launch {
                    try { db.suggestionDao().upsert(finalCleanWord, "", 1) } catch (e: Exception) {}
                }
            }

            if (pairCount % 3 == 0 && finalPrevWord.isNotBlank()) {
                android.util.Log.d("NoxDebug", "saveTypingData: Inserting pair '$finalPrevWord -> $finalCleanWord' permanently")
                if (isMyanmar) {
                    myUserNextWordEngine.insert(finalPrevWord, finalCleanWord, 1)
                } else {
                    enUserNextWordEngine.insert(finalPrevWord, finalCleanWord, 1)
                }

                serviceScope.launch {
                    try { db.suggestionDao().upsert(finalPrevWord, finalCleanWord, 1) } catch (e: Exception) {}
                }
            }
        }
    }

    private fun closeEmojiPicker() {
        if (::keyboardView.isInitialized && keyboardView.isEmojiMode) {
            keyboardView.isEmojiMode = false
            keyboardView.invalidate()
            emojiPickerView?.visibility = View.GONE
        }
    }

    private var activeAppPackageName: String = "com.google.android.apps.messaging"

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        activeAppPackageName = info?.packageName ?: "com.google.android.apps.messaging"

        try {
            window?.window?.decorView?.let { decorView ->
                decorView.setViewTreeLifecycleOwner(serviceLifecycleOwner)
                decorView.setViewTreeViewModelStoreOwner(serviceLifecycleOwner)
                decorView.setViewTreeSavedStateRegistryOwner(serviceLifecycleOwner)
            }
        } catch (e: Throwable) {
            Log.e("NoxBoard", "Failed to set ViewTree owners on window decorView", e)
        }

        try {
            serviceLifecycleOwner?.onStart()
        } catch (e: Throwable) {
            Log.e("NoxBoard", "Error during onStart lifecycle", e)
        }

        if (isWaitingForVaultUnlock) {
            isWaitingForVaultUnlock = false
        } else {
            closeEmojiPicker()
            hideVaultPanel()
        }

        var isNumberField = false

        if (info != null) {
            currentActionId = info.imeOptions and EditorInfo.IME_MASK_ACTION
            if (::keyboardView.isInitialized) {
                keyboardView.setEnterKeyAction(currentActionId)
            }

            val textClass = info.inputType and EditorInfo.TYPE_MASK_CLASS
            val isTextField = (textClass == EditorInfo.TYPE_CLASS_TEXT)
            val isMultiLineFlag = (info.inputType and EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) != 0
            isMultiLine = isTextField && isMultiLineFlag

            val isNumberField = textClass == android.text.InputType.TYPE_CLASS_NUMBER ||
                    textClass == android.text.InputType.TYPE_CLASS_PHONE ||
                    textClass == android.text.InputType.TYPE_CLASS_DATETIME

            if (::keyboardView.isInitialized) {
                keyboardView.isNumpadMode = false

                if (isNumberField) {
                    keyboardView.currentMode = com.noxtan.noxboard.KeyboardMode.NUMBER
                } else if (keyboardView.currentMode == com.noxtan.noxboard.KeyboardMode.NUMBER) {
                    keyboardView.currentMode = com.noxtan.noxboard.KeyboardMode.MYANMAR
                }
            }
        } else {
            currentActionId = EditorInfo.IME_ACTION_NONE
            isMultiLine = false
        }

        if (::keyboardView.isInitialized) {
            val prefs = NoxBoardPrefs(this)
            keyboardView.applySettings(prefs)
            applyNavigationBarTheme()
        }
        loadMyanmarEngine()
        checkAutoCapitalization()
        updateSuggestions()
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        updateSuggestions()
        checkAutoCapitalization()

        val isSelecting = newSelStart != newSelEnd

        if (::keyboardView.isInitialized && keyboardView.hasSelection != isSelecting) {
            keyboardView.hasSelection = isSelecting
            keyboardView.invalidate()
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        try {
            serviceLifecycleOwner?.onStop()
        } catch (e: Throwable) {
            Log.e("NoxBoard", "Error during onStop lifecycle", e)
        }

        _typedWordFlow.value = ""
        _suggestionsFlow.value = emptyList()
        currentSuggestionsMap = emptyMap()
        myMyanmarEngine?.resetState()
        lastSavedWordStr = ""
    }

    private fun applyNavigationBarTheme() {
        val window = window?.window ?: return
        val theme = keyboardView.theme

        window.navigationBarColor = theme.backgroundColor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val decorView = window.decorView
            var flags = decorView.systemUiVisibility

            val isLightBg = isColorLight(theme.backgroundColor)
            flags = if (isLightBg) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            decorView.systemUiVisibility = flags
        }
    }

    private fun isColorLight(color: Int): Boolean {
        val darkness = 1 - (0.299 * android.graphics.Color.red(color) +
                0.587 * android.graphics.Color.green(color) +
                0.114 * android.graphics.Color.blue(color)) / 255
        return darkness < 0.5
    }

    @androidx.compose.foundation.ExperimentalFoundationApi
    override fun onCreateInputView(): View {
        frameLayout = FrameLayout(this)

        keyboardView = NoxKeyboardView(this)
        keyboardView.listener = this

        val prefs = NoxBoardPrefs(this)
        keyboardView.applySettings(prefs)

        applyNavigationBarTheme()

        val owner = serviceLifecycleOwner ?: ServiceLifecycleOwner().apply { onCreate() }.also { serviceLifecycleOwner = it }

        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(owner)
            decorView.setViewTreeViewModelStoreOwner(owner)
            decorView.setViewTreeSavedStateRegistryOwner(owner)
        }

        frameLayout.setViewTreeLifecycleOwner(owner)
        frameLayout.setViewTreeViewModelStoreOwner(owner)
        frameLayout.setViewTreeSavedStateRegistryOwner(owner)

        try {
            composeView = ComposeView(this).apply {
                id = android.view.View.generateViewId()
                setViewTreeLifecycleOwner(owner)
                setViewTreeViewModelStoreOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
                setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            }
        } catch (e: Throwable) { }

        suggestionComposeView = ComposeView(this).apply {
            id = android.view.View.generateViewId()
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindow)

            setContent {
                NoxBoardTheme {
                    val suggestions by _suggestionsFlow.collectAsState()
                    val typedWord by _typedWordFlow.collectAsState()
                    var wordToRemove by remember { mutableStateOf<String?>(null) }
                    val bgColor = Color.Transparent
                    val textColor = Color(keyboardView.getResolvedSuggestionTextColor())
                    val corner = keyboardView.theme.keyCornerRadiusDp.dp

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgColor, androidx.compose.foundation.shape.RoundedCornerShape(corner))
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(corner))
                    ) {
                        if (wordToRemove != null) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remove '$wordToRemove'?", color = textColor, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                Row {
                                    TextButton(onClick = { wordToRemove = null }) {
                                        Text("No", color = Color.Gray)
                                    }
                                    TextButton(onClick = {
                                        val displayWord = wordToRemove ?: return@TextButton
                                        wordToRemove = null

                                        val rawWord = currentSuggestionsMap[displayWord] ?: displayWord
                                        val isMyanmar = rawWord.any { it in '\u1000'..'\u109F' }
                                        if (isMyanmar) myTrieEngine.delete(rawWord) else enTrieEngine.delete(rawWord)

                                        temporarilyDeletedWords.add(rawWord)

                                        serviceScope.launch {
                                            db.suggestionDao().deleteWord(rawWord)

                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                updateSuggestions()
                                            }
                                        }
                                    }) {
                                        Text("Yes", color = Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else if (typedWord.isNotEmpty() || suggestions.isNotEmpty()) {
                            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                            val screenWidth = configuration.screenWidthDp.dp

                            androidx.compose.foundation.lazy.LazyRow(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(corner)),
                                verticalAlignment = Alignment.CenterVertically,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                            ) {
                                if (typedWord.isNotEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .widthIn(min = screenWidth / 4, max = screenWidth * 0.8f)
                                                .clipToBounds()
                                                .clickable { insertSuggestion(typedWord) }
                                                .padding(horizontal = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = typedWord,
                                                color = textColor,
                                                fontSize = 16.sp,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.wrapContentWidth(Alignment.End, unbounded = true)
                                            )
                                        }
                                    }
                                }

                                val filteredSuggestions = suggestions.filter { it != typedWord }
                                itemsIndexed(filteredSuggestions) { index, word ->

                                    val isBestPrediction = index == 0
                                    val wordColor = if (isBestPrediction) textColor else textColor.copy(alpha = 0.7f)
                                    val wordWeight = if (isBestPrediction) FontWeight.Bold else FontWeight.Normal

                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .widthIn(min = screenWidth / 4, max = screenWidth * 0.85f)
                                            .combinedClickable(
                                                onClick = { insertSuggestion(word) },
                                                onLongClick = { wordToRemove = word }
                                            )
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = word,
                                            color = wordColor,
                                            fontSize = 16.sp,
                                            fontWeight = wordWeight,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        } else {
                        }
                    }
                }
            }
        }

        frameLayout.addView(keyboardView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { gravity = android.view.Gravity.BOTTOM })

        frameLayout.addView(suggestionComposeView, FrameLayout.LayoutParams(0, 0).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.LEFT
        })

        keyboardView.onSuggestionBoundsChanged = { rect ->
            if (rect != null) {
                val w = rect.width().toInt()
                val h = rect.height().toInt()

                val params = suggestionComposeView.layoutParams as FrameLayout.LayoutParams

                if (params.width != w || params.height != h) {
                    params.width = w
                    params.height = h
                    suggestionComposeView.layoutParams = params
                }

                suggestionComposeView.translationX = rect.left
                suggestionComposeView.translationY = rect.top

                if (suggestionComposeView.visibility != android.view.View.VISIBLE) {
                    suggestionComposeView.visibility = android.view.View.VISIBLE
                }
            } else {
                if (suggestionComposeView.visibility != android.view.View.GONE) {
                    suggestionComposeView.visibility = android.view.View.GONE
                }
            }
        }

        return frameLayout
    }

    private fun showVaultPanel() {
        val view = composeView ?: return
        try {
            frameLayout.removeView(view)

            val density = resources.displayMetrics.density
            val totalHeight = (keyboardView.getEffectiveKeyboardHeightDp() * density).toInt() +
                    (keyboardView.getEffectiveBottomPaddingDp() * density).toInt()

            frameLayout.addView(view, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                totalHeight
            ).apply { gravity = Gravity.BOTTOM })

            keyboardView.visibility = View.GONE
            view.visibility = View.VISIBLE

            view.setContent {
                NoxBoardTheme {
                    var accounts by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<VaultAccount>>(emptyList()) }
                    var isLocked by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                    fun refreshData() {
                        when (val result = com.noxtan.noxboard.provider.VaultDataFetcher.fetchAccounts(this@NoxKeyboardService)) {
                            is com.noxtan.noxboard.provider.VaultDataFetcher.VaultResult.Success -> {
                                accounts = result.accounts
                                isLocked = false
                            }
                            is com.noxtan.noxboard.provider.VaultDataFetcher.VaultResult.Locked -> {
                                accounts = emptyList()
                                isLocked = true
                            }
                            is com.noxtan.noxboard.provider.VaultDataFetcher.VaultResult.Error -> {
                                accounts = emptyList()
                                isLocked = false
                            }
                        }
                    }

                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        refreshData()
                    }

                    val windowInfo = androidx.compose.ui.platform.LocalWindowInfo.current
                    androidx.compose.runtime.LaunchedEffect(windowInfo.isWindowFocused) {
                        if (windowInfo.isWindowFocused) {
                            refreshData()
                        }
                    }

                    CredentialVaultPanel(
                        packageName = activeAppPackageName,
                        bottomPaddingDp = keyboardView.getEffectiveBottomPaddingDp(),
                        accounts = accounts,
                        isLocked = isLocked,
                        onUnlockClick = {
                            isWaitingForVaultUnlock = true

                            val intent = Intent("com.noxtan.kee.ACTION_UNLOCK_VAULT").apply {
                                setPackage("com.noxtan.kee")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(this@NoxKeyboardService, "Kee Vault is not installed", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        onFillText = { text ->
                            when (text) {
                                "⌫" -> {
                                    val ic = currentInputConnection
                                    if (ic != null) {
                                        val selectedText = ic.getSelectedText(0)
                                        if (!selectedText.isNullOrEmpty()) {
                                            ic.commitText("", 1)
                                        } else {
                                            ic.deleteSurroundingText(1, 0)
                                        }
                                    }
                                }
                                "\n" -> {
                                    val ic = currentInputConnection
                                    if (ic != null) {
                                        if (isMultiLine) {
                                            ic.commitText("\n", 1)
                                        } else {
                                            ic.performEditorAction(currentActionId)
                                        }
                                    }
                                }
                                else -> {
                                    currentInputConnection?.commitText(text, 1)
                                }
                            }
                        },
                        onClose = {
                            hideVaultPanel()
                        }
                    )
                }
            }
            Log.d("NoxBoard", "VaultPanel content set and displayed successfully")
        } catch (e: Throwable) {
            Log.e("NoxBoard", "Crash inside showVaultPanel while calling setContent", e)
            hideVaultPanel()
        }
    }

    private fun hideVaultPanel() {
        composeView?.visibility = View.GONE
        keyboardView.visibility = View.VISIBLE
    }

    private fun showEmojiPicker() {
        keyboardView.isEmojiMode = true
        keyboardView.invalidate()

        emojiPickerView?.let {
            frameLayout.removeView(it)
        }

        var darkThemeId = resources.getIdentifier("Theme_Material3_Dark_NoActionBar", "style", packageName)
        if (darkThemeId == 0) {
            darkThemeId = resources.getIdentifier("Theme_AppCompat_NoActionBar", "style", packageName)
        }
        if (darkThemeId == 0) {
            darkThemeId = android.R.style.Theme_DeviceDefault_NoActionBar
        }

        val emojiContext = if (isColorLight(keyboardView.theme.backgroundColor)) {
            this
        } else {
            android.view.ContextThemeWrapper(this, darkThemeId)
        }

        val newPicker = EmojiPickerView(emojiContext).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)

            val screenWidthDp = resources.configuration.screenWidthDp
            emojiGridColumns = screenWidthDp / 36

            setOnEmojiPickedListener { emoji ->
                currentInputConnection?.commitText(emoji.emoji, 1)
            }
        }
        emojiPickerView = newPicker

        val density = resources.displayMetrics.density

        val pickerBottomMargin = keyboardView.getEmojiPickerBottomMargin()
        val pickerHeight = keyboardView.getEmojiPickerHeight()

        frameLayout.addView(newPicker, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            pickerHeight
        ).apply {
            gravity = Gravity.BOTTOM
            bottomMargin = pickerBottomMargin
        })

        newPicker.visibility = View.VISIBLE
    }

    override fun onKey(code: Int, text: String): Boolean {
        val ic = currentInputConnection ?: return false

        if (code == -99 && text == "CLOSE_EMOJI") {
            closeEmojiPicker()
            return true
        }

        var success = true

        when (code) {
            -3 -> { }
            -97 -> {
                val prefs = NoxBoardPrefs(this)
                if (prefs.isDoubleSpacePeriodEnabled) {
                    val textBefore = ic.getTextBeforeCursor(2, 0)?.toString() ?: ""

                    if (textBefore.length == 2 && textBefore[1] == ' ' && textBefore[0] != ' ' && textBefore[0] != '.' && textBefore[0] != '။' && textBefore[0] != '\n') {
                        ic.deleteSurroundingText(1, 0)
                        if (keyboardView.currentMode == com.noxtan.noxboard.KeyboardMode.ENGLISH) {
                            ic.commitText(". ", 1)
                        } else {
                            ic.commitText("။ ", 1)
                        }
                    } else {
                        ic.commitText(" ", 1)
                    }
                } else {
                    ic.commitText(" ", 1)
                }
                myMyanmarEngine?.resetState()
            }
            -98 -> {
                if (lastSavedWordStr.isNotEmpty() && System.currentTimeMillis() - lastSavedWordTime < 500) {
                    val isMyanmar = lastSavedWordStr.any { it in '\u1000'..'\u109F' }
                    if (isMyanmar) myTrieEngine.delete(lastSavedWordStr) else enTrieEngine.delete(lastSavedWordStr)

                    val wordToDelete = lastSavedWordStr
                    serviceScope.launch {
                        try {
                            db.suggestionDao().deleteWord(wordToDelete)
                        } catch (e: Exception) {}
                    }
                    lastSavedWordStr = ""
                }
            }
            -16 -> {
                requestHideSelf(0)
            }
            -11 -> {
                if (keyboardView.isEmojiMode) {
                    closeEmojiPicker()
                } else {
                    showEmojiPicker()
                }
            }
            -12 -> {
                showVaultPanel()
            }
            -13 -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val selectedText = ic.getSelectedText(0)

                if (!selectedText.isNullOrEmpty()) {
                    val clip = android.content.ClipData.newPlainText("Nox Clipboard", selectedText)
                    clipboard.setPrimaryClip(clip)
                    android.widget.Toast.makeText(this, "Copied", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    val clipData = clipboard.primaryClip
                    if (clipData != null && clipData.itemCount > 0) {
                        val pasteText = clipData.getItemAt(0).coerceToText(this@NoxKeyboardService)

                        if (!pasteText.isNullOrEmpty()) {
                            ic.commitText(pasteText, 1)
                        }
                    }
                }
            }
            -14 -> {
                closeEmojiPicker()
                hideVaultPanel()
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
            -2 -> {
                val selectedText = ic.getSelectedText(0)
                if (!selectedText.isNullOrEmpty()) {
                    ic.commitText("", 1)
                } else {
                    val textBefore = ic.getTextBeforeCursor(1, 0)
                    if (!textBefore.isNullOrEmpty()) {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                    } else {
                        success = false
                    }
                }
                myMyanmarEngine?.resetState()
            }
            -21 -> { success = moveCursor(-1, 0) }
            -22 -> { success = moveCursor(1, 0) }
            -23 -> { success = moveCursor(0, -1) }
            -24 -> { success = moveCursor(0, 1) }
            -31 -> { success = moveCursorWithSelection(-1, 0) }
            -32 -> { success = moveCursorWithSelection(1, 0) }
            -33 -> { success = moveCursorWithSelection(0, -1) }
            -34 -> { success = moveCursorWithSelection(0, 1) }
            -35 -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
                success = true
            }
            10 -> {
                saveTypingData()
                val action = currentActionId
                if (isMultiLine) {
                    ic.commitText("\n", 1)
                } else if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ic.performEditorAction(action)
                } else {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }
                myMyanmarEngine?.resetState()
            }
            32 -> {
                val prefs = NoxBoardPrefs(this)
                if (prefs.isAutoCorrectionEnabled) {
                    performAutoCorrect(ic)
                }

                saveTypingData()
                ic.commitText(" ", 1)
                myMyanmarEngine?.resetState()
            }
            else -> {
                val prefs = NoxBoardPrefs(this)
                val punctuations = setOf(".", ",", "!", "?", "။", "၊", ":", ";", "”", "’", ")", "]", "}")
                val isPunctuation = punctuations.contains(text)

                if (isPunctuation && prefs.isAutoCorrectionEnabled) {
                    performAutoCorrect(ic)
                }

                if (isPunctuation) {
                    val textBefore = ic.getTextBeforeCursor(1, 0)
                    if (textBefore == " ") {
                        ic.deleteSurroundingText(1, 0)
                    }
                }

                if (keyboardView.currentMode == KeyboardMode.MYANMAR) {
                    myMyanmarEngine?.handleKeyPress(text, ic)
                } else {
                    ic.commitText(text, 1)
                }

                if (isPunctuation && prefs.isAutoSpaceEnabled) {
                    val noSpaceAfter = setOf("”", "’", "(", "[", "{")
                    if (!noSpaceAfter.contains(text)) {

                        var shouldSpace = true
                        if (text == ".") {
                            val textBefore = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
                            val currentWord = textBefore.split(Regex("\\s+")).lastOrNull() ?: ""

                            val editorInfo = currentInputEditorInfo
                            if (editorInfo != null) {
                                val inputType = editorInfo.inputType
                                val cls = inputType and android.text.InputType.TYPE_MASK_CLASS
                                val variation = inputType and android.text.InputType.TYPE_MASK_VARIATION
                                if (cls == android.text.InputType.TYPE_CLASS_TEXT &&
                                    (variation == android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                                            variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ||
                                            variation == android.text.InputType.TYPE_TEXT_VARIATION_URI)) {
                                    shouldSpace = false
                                }
                            }

                            if (currentWord.contains("@") || currentWord.startsWith("www") || currentWord.startsWith("http")) {
                                shouldSpace = false
                            }
                        }

                        if (shouldSpace) {
                            ic.commitText(" ", 1)
                            myMyanmarEngine?.resetState()
                        }
                    }
                }
            }
        }
        updateSuggestions()
        checkAutoCapitalization()
        return success
    }

    private fun moveCursor(horizontal: Int, vertical: Int): Boolean {
        val ic = currentInputConnection ?: return false
        var moved = false

        if (horizontal != 0) {
            if (horizontal < 0) {
                if (!ic.getTextBeforeCursor(1, 0).isNullOrEmpty()) moved = true
            } else {
                if (!ic.getTextAfterCursor(1, 0).isNullOrEmpty()) moved = true
            }
            if (moved) {
                val keyCode = if (horizontal < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            }
        } else if (vertical != 0) {
            if (vertical < 0) {
                if (!ic.getTextBeforeCursor(1, 0).isNullOrEmpty()) moved = true
            } else {
                if (!ic.getTextAfterCursor(1, 0).isNullOrEmpty()) moved = true
            }
            if (moved) {
                val keyCode = if (vertical < 0) KeyEvent.KEYCODE_DPAD_UP else KeyEvent.KEYCODE_DPAD_DOWN
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            }
        }
        return moved
    }

    private fun moveCursorWithSelection(horizontal: Int, vertical: Int): Boolean {
        val ic = currentInputConnection ?: return false
        val hasSelection = !ic.getSelectedText(0).isNullOrEmpty()
        var moved = false

        val keyCode = when {
            horizontal < 0 -> {
                if (hasSelection || !ic.getTextBeforeCursor(1, 0).isNullOrEmpty()) moved = true
                KeyEvent.KEYCODE_DPAD_LEFT
            }
            horizontal > 0 -> {
                if (hasSelection || !ic.getTextAfterCursor(1, 0).isNullOrEmpty()) moved = true
                KeyEvent.KEYCODE_DPAD_RIGHT
            }
            vertical < 0 -> {
                if (hasSelection || !ic.getTextBeforeCursor(1, 0).isNullOrEmpty()) moved = true
                KeyEvent.KEYCODE_DPAD_UP
            }
            vertical > 0 -> {
                if (hasSelection || !ic.getTextAfterCursor(1, 0).isNullOrEmpty()) moved = true
                KeyEvent.KEYCODE_DPAD_DOWN
            }
            else -> return false
        }

        if (moved) {
            val eventTime = android.os.SystemClock.uptimeMillis()
            ic.sendKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0))
            ic.sendKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0, KeyEvent.META_SHIFT_ON))
            ic.sendKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0, KeyEvent.META_SHIFT_ON))
            ic.sendKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0))
        }
        return moved
    }
}

private class ServiceLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        try {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        } catch (e: Throwable) {
            Log.e("NoxBoard", "Error during ServiceLifecycleOwner onCreate", e)
        }
    }

    fun onStart() {
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } catch (e: Throwable) {
            Log.e("NoxBoard", "Error during ServiceLifecycleOwner onStart", e)
        }
    }

    fun onStop() {
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        } catch (e: Throwable) {
            Log.e("NoxBoard", "Error during ServiceLifecycleOwner onStop", e)
        }
    }

    fun onDestroy() {
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
        } catch (e: Throwable) {
            Log.e("NoxBoard", "Error during ServiceLifecycleOwner onDestroy", e)
        }
    }
}