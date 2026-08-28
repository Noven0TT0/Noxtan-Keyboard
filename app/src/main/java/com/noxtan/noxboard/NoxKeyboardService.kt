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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import com.noxtan.pass.aidl.VaultAccountInfo
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.res.painterResource

class NoxKeyboardService : InputMethodService(), NoxKeyboardView.OnKeyboardActionListener,
    SharedPreferences.OnSharedPreferenceChangeListener {
    var activePanelState = androidx.compose.runtime.mutableStateOf("KEYBOARD")
    var hasSelectionState = androidx.compose.runtime.mutableStateOf(false)
    var themeUpdateTrigger = androidx.compose.runtime.mutableIntStateOf(0)
    private var currentMathResult: com.noxtan.noxboard.engines.MathResult? = null
    private val _suggestionsFlow = MutableStateFlow<List<String>>(emptyList())
    private val _typedWordFlow = MutableStateFlow<String>("")
    private val _hasActiveSuggestionsFlow = kotlinx.coroutines.flow.MutableStateFlow<Boolean>(false)
    private lateinit var suggestionComposeView: ComposeView
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var db: NoxDatabase
    private val trieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val myTrieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val enTrieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val thTrieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val myNextWordEngine = com.noxtan.noxboard.engines.NextWordEngine()
    private val enNextWordEngine = com.noxtan.noxboard.engines.NextWordEngine()
    private val thNextWordEngine = com.noxtan.noxboard.engines.NextWordEngine()
    private val myUserTrieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val enUserTrieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val thUserTrieEngine = com.noxtan.noxboard.engines.TrieEngine()
    private val myUserNextWordEngine = com.noxtan.noxboard.engines.NextWordEngine()
    private val enUserNextWordEngine = com.noxtan.noxboard.engines.NextWordEngine()
    private val thUserNextWordEngine = com.noxtan.noxboard.engines.NextWordEngine()
    private val tempWordCounts = mutableMapOf<String, Int>()
    private val tempPairCounts = mutableMapOf<Pair<String, String>, Int>()
    private val badWordsSet = mutableSetOf<String>()
    private val clipboardHistory = androidx.compose.runtime.mutableStateListOf<String>()
    private var clipboardListener: android.content.ClipboardManager.OnPrimaryClipChangedListener? = null

    private val emojiSuggestionMap = mutableMapOf<String, String>()

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
    private lateinit var keyboardWrapper: android.widget.LinearLayout
    private lateinit var dragHandle: FrameLayout
    private lateinit var snapIndicatorView: View // <--- ဒါလေး ရှိနေဖို့လိုပါတယ်
    private lateinit var keyboardView: NoxKeyboardView
    private var composeView: ComposeView? = null
    private var composePaddingState = androidx.compose.runtime.mutableFloatStateOf(0f)
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
        com.noxtan.noxboard.engines.NativeDictionaryEngine.initEngine(assets)
        loadDictionaryFromAssetsOnlyForAutoCorrect()
        loadUserDictionary()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboardListener = android.content.ClipboardManager.OnPrimaryClipChangedListener {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()
                if (!text.isNullOrEmpty()) {
                    if (clipboardHistory.contains(text)) clipboardHistory.remove(text)
                    clipboardHistory.add(0, text)
                    if (clipboardHistory.size > 20) clipboardHistory.removeAt(clipboardHistory.size - 1)
                }
            }
        }
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)
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
                if (::keyboardView.isInitialized) keyboardView.setSuggestionsActive(false)
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
            if (::keyboardView.isInitialized) keyboardView.setSuggestionsActive(false)
            return
        }

        val ic = currentInputConnection ?: run {
            _suggestionsFlow.value = emptyList()
            currentSuggestionsMap = emptyMap()
            if (::keyboardView.isInitialized) keyboardView.setSuggestionsActive(false)
            return
        }

        val textBefore = ic.getTextBeforeCursor(50, 0)?.toString() ?: ""

        val mathRes = com.noxtan.noxboard.engines.MathEngine.evaluate(textBefore)
        currentMathResult = mathRes
        if (mathRes != null) {
            _typedWordFlow.value = ""
            val mathSuggestions = listOf(mathRes.resultOnly, mathRes.fullEquation)
            val newSuggestionsMap = mutableMapOf<String, String>()
            newSuggestionsMap[mathRes.resultOnly] = mathRes.resultOnly
            newSuggestionsMap[mathRes.fullEquation] = mathRes.fullEquation
            currentSuggestionsMap = newSuggestionsMap
            _suggestionsFlow.value = mathSuggestions
            _hasActiveSuggestionsFlow.value = true
            if (::keyboardView.isInitialized) keyboardView.setSuggestionsActive(true)
            return
        }

        val words = textBefore.split(Regex("\\s+"))
        val lastWord = words.lastOrNull() ?: ""

        if (lastWord.isNotBlank()) {
            val cleanPrefix = lastWord.replace("\u200C", "")

            val containsEmoji = cleanPrefix.any { Character.isSurrogate(it) || it.code in 0x1F000..0x1FAFF || it.code in 0x2600..0x27BF || it.code in 0x2B50..0x2BFF }
            if (containsEmoji) {
                _suggestionsFlow.value = emptyList()
                _typedWordFlow.value = ""
                currentSuggestionsMap = emptyMap()
                if (::keyboardView.isInitialized) keyboardView.setSuggestionsActive(textBefore.isNotEmpty())
                return
            }

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

                val isThai = keyboardView.currentMode == com.noxtan.noxboard.KeyboardMode.THAI
                if (isEnglish) {
                    val prevWordRaw = if (words.size >= 2) words[words.size - 2] else ""
                    val prevWord = prevWordRaw.replace(Regex("[.,!?။၊:;\"'()\\[\\]{}]"), "").replace("\u200C", "")

                    if (prevWord.isNotBlank() && prefs.isNextWordPredictionEnabled) {
                        val userNextWords = if (showUserSuggestions) enUserNextWordEngine.getExactNextWords(prevWord).filter { it.startsWith(cleanPrefix, ignoreCase = true) } else emptyList()

                        val systemNextWords = if (showSystemSuggestions) {
                            com.noxtan.noxboard.engines.NativeDictionaryEngine.getSuggestions("$prevWord $cleanPrefix".lowercase(), "ENGLISH", 10)
                                .map { it.split(" ").last() }
                        } else emptyList()

                        (userNextWords + systemNextWords).distinct().map { adjustCasing(it, cleanPrefix) }.forEach { pred ->
                            if (!suggestionsList.contains(pred)) { suggestionsList.add(pred); newSuggestionsMap[pred] = pred }
                        }
                    }

                    val userTrie = if (showUserSuggestions) enUserTrieEngine.searchPrefix(cleanPrefix.lowercase()) else emptyList()

                    val systemTrie = if (showSystemSuggestions) com.noxtan.noxboard.engines.NativeDictionaryEngine.getSuggestions(cleanPrefix.lowercase(), "ENGLISH", 10).map { it.split(" ").first() }.toList() else emptyList()

                    (userTrie + systemTrie).distinct().map { adjustCasing(it, cleanPrefix) }.forEach { pred ->
                        if (!suggestionsList.contains(pred)) { suggestionsList.add(pred); newSuggestionsMap[pred] = pred }
                    }

                } else if (isThai) {
                    val userTrie = if (showUserSuggestions) thUserTrieEngine.searchPrefix(cleanPrefix) else emptyList()

                    val systemTrie = if (showSystemSuggestions) com.noxtan.noxboard.engines.NativeDictionaryEngine.getSuggestions(cleanPrefix, "THAI", 10).map { it.split(" ").first() }.toList() else emptyList()

                    (userTrie + systemTrie).distinct().forEach { pred ->
                        if (!suggestionsList.contains(pred)) {
                            suggestionsList.add(pred)
                            newSuggestionsMap[pred] = pred
                        }
                    }
                } else {
                    val userTrie = if (showUserSuggestions) myUserTrieEngine.searchPrefix(cleanPrefix) else emptyList()

                    val systemTrie = if (showSystemSuggestions) com.noxtan.noxboard.engines.NativeDictionaryEngine.getSuggestions(cleanPrefix, "MYANMAR", 10).map { it.split(" ").first() }.toList() else emptyList()

                    (userTrie + systemTrie).distinct().forEach { pred ->
                        if (!suggestionsList.contains(pred)) {
                            suggestionsList.add(pred)
                            newSuggestionsMap[pred] = pred
                        }
                    }
                }

                var emoji: String? = null
                val cleanPrefixLower = cleanPrefix.lowercase()

                emoji = emojiSuggestionMap[cleanPrefixLower]

                if (emoji == null && words.size >= 2) {
                    val prevWord = words[words.size - 2].replace("\u200C", "").lowercase()
                    val twoWords = "$prevWord $cleanPrefixLower"
                    emoji = emojiSuggestionMap[twoWords]
                }

                if (emoji == null && words.size >= 3) {
                    val prevWord1 = words[words.size - 3].replace("\u200C", "").lowercase()
                    val prevWord2 = words[words.size - 2].replace("\u200C", "").lowercase()
                    val threeWords = "$prevWord1 $prevWord2 $cleanPrefixLower"
                    emoji = emojiSuggestionMap[threeWords]
                }

                if (emoji != null) {
                    if (suggestionsList.contains(emoji)) {
                        suggestionsList.remove(emoji)
                    }
                    suggestionsList.add(0, emoji)
                    newSuggestionsMap[emoji] = emoji
                }

                currentSuggestionsMap = newSuggestionsMap

                val garbageWords = setOf("re", "ll", "ve", "m", "d", "s", "t")
                val cleanSuggestionsList = suggestionsList.filter {
                    !(isEnglish && garbageWords.contains(it.lowercase()))
                }

                val finalSuggestions = if (prefs.isBlockOffensiveWordsEnabled) {
                    cleanSuggestionsList.filter { suggestion ->
                        val raw = currentSuggestionsMap[suggestion] ?: suggestion
                        val isMyanmar = raw.any { it in '\u1000'..'\u109F' }
                        badWordsSet.none { badWord ->
                            if (isMyanmar) {
                                raw.contains(badWord)
                            } else {
                                raw.equals(badWord, ignoreCase = true)
                            }
                        }
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
                if (::keyboardView.isInitialized) keyboardView.setSuggestionsActive(textBefore.isNotEmpty())
                return
            }

            val prevWordRaw = if (words.size >= 2) words[words.size - 2] else ""
            val prevWord = prevWordRaw.replace(Regex("[.,!?။၊:;\"'()\\[\\]{}]"), "").replace("\u200C", "")

            android.util.Log.d("NoxDebug", "--- Space Pressed. Prev Word: '$prevWord' ---")

            if (prevWord.isNotBlank() && prevWord.length >= 1) {
                val isEnglish = keyboardView.currentMode == com.noxtan.noxboard.KeyboardMode.ENGLISH

                val showSystemSuggestions = prefs.isSuggestionEnabled
                val showUserSuggestions = !prefs.isIncognitoModeEnabled

                val isThai = keyboardView.currentMode == com.noxtan.noxboard.KeyboardMode.THAI
                val suggestions = if (isEnglish) {
                    val userNext = if (showUserSuggestions) enUserNextWordEngine.getExactNextWords(prevWord) else emptyList()
                    val sysNext = if (showSystemSuggestions) com.noxtan.noxboard.engines.NativeDictionaryEngine.getSuggestions("$prevWord ", "ENGLISH", 10).map { it.split(" ").last() } else emptyList()
                    (userNext + sysNext).distinct()
                } else if (isThai) {
                    val userNext = if (showUserSuggestions) thUserNextWordEngine.getExactNextWords(prevWord) else emptyList()
                    val sysNext = if (showSystemSuggestions) com.noxtan.noxboard.engines.NativeDictionaryEngine.getSuggestions("$prevWord ", "THAI", 10).map { it.split(" ").last() } else emptyList()
                    (userNext + sysNext).distinct()
                } else {
                    val userNext = if (showUserSuggestions) myUserNextWordEngine.getExactNextWords(prevWord) else emptyList()
                    val sysNext = if (showSystemSuggestions) com.noxtan.noxboard.engines.NativeDictionaryEngine.getSuggestions("$prevWord ", "MYANMAR", 10).map { it.split(" ").last() } else emptyList()
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
                        val isMyanmar = suggestion.any { it in '\u1000'..'\u109F' }
                        badWordsSet.none { badWord ->
                            if (isMyanmar) {
                                suggestion.contains(badWord)
                            } else {
                                suggestion.equals(badWord, ignoreCase = true)
                            }
                        }
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
            val hasText = textBefore.isNotEmpty()

            val shouldShowSuggestions = _suggestionsFlow.value.isNotEmpty() || _typedWordFlow.value.isNotEmpty() || hasText

            _hasActiveSuggestionsFlow.value = shouldShowSuggestions
            keyboardView.setSuggestionsActive(shouldShowSuggestions)
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

    private fun performAutoCorrect(ic: android.view.inputmethod.InputConnection, isInstant: Boolean = false) {
        val textBefore = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
        if (textBefore.isEmpty()) return

        val isMyanmar = textBefore.last() in '\u1000'..'\u109F' || textBefore.last() == '\u200C'

        if (isMyanmar) {
            var matchedWrongWord: String? = null
            var correctedWord: String? = null

            for (i in minOf(textBefore.length, 15) downTo 1) {
                val suffix = textBefore.takeLast(i)
                val cleanSuffix = suffix.replace("\u200C", "")
                if (autoCorrectMap.containsKey(cleanSuffix)) {
                    matchedWrongWord = suffix
                    correctedWord = autoCorrectMap[cleanSuffix]
                    break
                }
            }

            if (matchedWrongWord != null && correctedWord != null) {
                ic.deleteSurroundingText(matchedWrongWord.length, 0)
                ic.commitText(correctedWord, 1)
                myMyanmarEngine?.resetState()
            }
        } else {
            if (!isInstant) {
                val match = Regex("([a-zA-Z']+)\$").find(textBefore)
                if (match != null) {
                    val lastWord = match.value
                    val correctedWord = autoCorrectMap[lastWord.lowercase()]
                    if (correctedWord != null) {
                        val finalCorrected = if (lastWord.isNotEmpty() && lastWord.first().isUpperCase()) {
                            correctedWord.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        } else {
                            correctedWord
                        }
                        ic.deleteSurroundingText(lastWord.length, 0)
                        ic.commitText(finalCorrected, 1)
                    }
                }
            }
        }
    }

    private fun insertSuggestion(displayWord: String) {
        val ic = currentInputConnection ?: return

        val math = currentMathResult
        if (math != null && (displayWord == math.resultOnly || displayWord == math.fullEquation)) {
            ic.deleteSurroundingText(math.expressionLength, 0)
            ic.commitText("$displayWord ", 1)
            currentMathResult = null
            saveTypingData()
            updateSuggestions()
            return
        }

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
            val isThai = cleanWord.any { it in '\u0E00'..'\u0E7F' }

            android.util.Log.d("NoxDebug", "insertSuggestion: Preview tapped! Force saving '$cleanWord'")

            if (isMyanmar) myUserTrieEngine.insert(cleanWord, 1) else if (isThai) thUserTrieEngine.insert(cleanWord, 1) else enUserTrieEngine.insert(cleanWord, 1)

            tempWordCounts[cleanWord] = 3

            serviceScope.launch {
                try { db.suggestionDao().upsert(cleanWord, "", 1) } catch (e: Exception) {}
            }

            if (words.size >= 2) {
                val prevWord = words[words.size - 2].trim().replace(Regex("[.,!?။၊:;\"'()\\[\\]{}]"), "").replace("\u200C", "")
                if (prevWord.isNotBlank() && isValidSpelling(prevWord)) {
                    if (isMyanmar) myUserNextWordEngine.insert(prevWord, cleanWord, 1) else if (isThai) thUserNextWordEngine.insert(prevWord, cleanWord, 1) else enUserNextWordEngine.insert(prevWord, cleanWord, 1)
                    val pair = Pair(prevWord, cleanWord)
                    tempPairCounts[pair] = 3
                    serviceScope.launch {
                        try { db.suggestionDao().upsert(prevWord, cleanWord, 1) } catch (e: Exception) {}
                    }
                }
            }
        } else {
            val isThaiMode = keyboardView.currentMode == com.noxtan.noxboard.KeyboardMode.THAI
            val triggerKey = if (isEnglish) {
                enUserNextWordEngine.findTriggerKey(cleanPrefix, rawPrediction)
                    ?: enNextWordEngine.findTriggerKey(cleanPrefix, rawPrediction)
            } else if (isThaiMode) {
                thUserNextWordEngine.findTriggerKey(cleanPrefix, rawPrediction)
                    ?: thNextWordEngine.findTriggerKey(cleanPrefix, rawPrediction)
            } else {
                myUserNextWordEngine.findTriggerKey(cleanPrefix, rawPrediction)
                    ?: myNextWordEngine.findTriggerKey(cleanPrefix, rawPrediction)
            }

            if (triggerKey != null && !prefs.isIncognitoModeEnabled) {
                if (isEnglish) enUserNextWordEngine.insert(triggerKey, rawPrediction, 1) else if (isThaiMode) thUserNextWordEngine.insert(triggerKey, rawPrediction, 1) else myUserNextWordEngine.insert(triggerKey, rawPrediction, 1)
                serviceScope.launch {
                    try { db.suggestionDao().upsert(triggerKey, rawPrediction, 1) } catch (e: Exception) {}
                }
            }
        }

        var deleteLength = lastWord.length

        val isEmoji = displayWord.any { Character.isSurrogate(it) || it.code in 0x1F000..0x1FAFF || it.code in 0x2600..0x27BF || it.code in 0x2B50..0x2BFF }

        if (isEmoji) {
            val cleanPrefixLower = cleanPrefix.lowercase()

            if (words.size >= 3) {
                val p1 = words[words.size - 3].replace("\u200C", "").lowercase()
                val p2 = words[words.size - 2].replace("\u200C", "").lowercase()
                if (emojiSuggestionMap["$p1 $p2 $cleanPrefixLower"] == displayWord) {
                    val match = Regex(Regex.escape(words[words.size - 3]) + "\\s+" + Regex.escape(words[words.size - 2]) + "\\s+" + Regex.escape(lastWord) + "$").find(textBefore)
                    if (match != null) deleteLength = match.value.length
                }
            }
            if (deleteLength == lastWord.length && words.size >= 2) {
                val p2 = words[words.size - 2].replace("\u200C", "").lowercase()
                if (emojiSuggestionMap["$p2 $cleanPrefixLower"] == displayWord) {
                    val match = Regex(Regex.escape(words[words.size - 2]) + "\\s+" + Regex.escape(lastWord) + "$").find(textBefore)
                    if (match != null) deleteLength = match.value.length
                }
            }
        }

        if (deleteLength > 0) {
            ic.deleteSurroundingText(deleteLength, 0)
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

        val defaultOffKeys = setOf("ဆ", "ဖ", "ထ", "ခ", "ည", "ဏ", "ဂ", "ဝ", "ဟ", "သ", "ူ", "ရ")
        val savedStates = try { org.json.JSONObject(prefs.doubleTapStates) } catch (e: Exception) { org.json.JSONObject() }
        val activeMap = mutableMapOf<String, String>()

        masterDoubleTapMap.forEach { (key, value) ->
            val isEnabled = if (savedStates.has(key)) savedStates.getBoolean(key) else !defaultOffKeys.contains(key)
            if (isEnabled) {
                activeMap[key] = value
            }
        }
        myMyanmarEngine?.activeDoubleTapMap = activeMap

        Log.d("NoxBoard", "Myanmar Engine Loaded: ${prefs.myanmarTypingStyle}")
    }

    private fun loadDictionaryFromAssetsOnlyForAutoCorrect() {
        serviceScope.launch {
            try {
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
                        android.util.Log.e("NoxBoard", "Error loading $fileName", e)
                    }
                }

                try {
                    assets.open("emoji_suggestions.txt").bufferedReader().useLines { lines ->
                        lines.forEach { line ->
                            val parts = line.split(",")
                            if (parts.size >= 2) {
                                emojiSuggestionMap[parts[0].trim().lowercase()] = parts[1].trim()
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NoxBoard", "Error loading emoji_suggestions.txt", e)
                }

                android.util.Log.d("NoxBoard", "Auto-Corrects and Emojis loaded!")
            } catch (e: Exception) {
                com.noxtan.noxboard.utils.NoxLogger.logError("Dictionary", "Failed to load extras", e)
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
                    val isThai = entity.word.any { it in '\u0E00'..'\u0E7F' }
                    if (entity.nextWord.isBlank()) {
                        if (isMyanmar) myUserTrieEngine.insert(entity.word, entity.frequency) else if (isThai) thUserTrieEngine.insert(entity.word, entity.frequency) else enUserTrieEngine.insert(entity.word, entity.frequency)
                    } else {
                        if (isMyanmar) myUserNextWordEngine.insert(entity.word, entity.nextWord, entity.frequency) else if (isThai) thUserNextWordEngine.insert(entity.word, entity.nextWord, entity.frequency) else enUserNextWordEngine.insert(entity.word, entity.nextWord, entity.frequency)
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

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::keyboardView.isInitialized) {
            val prefs = NoxBoardPrefs(this)
            if (prefs.activeTheme == "DEFAULT") {
                keyboardView.applySettings(prefs)
                applyNavigationBarTheme()
                keyboardView.invalidate()
                themeUpdateTrigger.intValue += 1
            }
        }
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
        com.noxtan.noxboard.engines.NativeDictionaryEngine.destroyEngine()
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

        if (key == "double_tap_states" || key == "font_size" || key == "keyboard_height" ||
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
            key == "suggestion_enabled" ||
            key == "myanmar_typing_style" ||
            key == "space_cursor_control" || key == "space_cursor_sensitivity" || key == "space_drag_delay" ||
            key == "global_row_gap" || key == "individual_row_gaps" ||
            key?.startsWith("icon_style_") == true) {

            loadMyanmarEngine()

            if (::keyboardView.isInitialized) {
                keyboardView.post {
                    keyboardView.applySettings(NoxBoardPrefs(this))
                    applyNavigationBarTheme()
                    themeUpdateTrigger.intValue += 1
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
            if (keyboardView.currentMode == com.noxtan.noxboard.KeyboardMode.THAI) {
                _suggestionsFlow.value = emptyList()
                _typedWordFlow.value = ""
                currentSuggestionsMap = emptyMap()
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

            val containsEmoji = lastWord.any { Character.isSurrogate(it) || it.code in 0x1F000..0x1FAFF || it.code in 0x2600..0x27BF || it.code in 0x2B50..0x2BFF }
            if (containsEmoji) return

            val cleanWord = lastWord.replace("\u200C", "")
            if (temporarilyDeletedWords.contains(cleanWord)) return

            val isMyanmar = cleanWord.any { it in '\u1000'..'\u109F' }
            val isThai = cleanWord.any { it in '\u0E00'..'\u0E7F' }

            if (prefs.isBlockOffensiveWordsEnabled && badWordsSet.any { badWord ->
                    if (isMyanmar) cleanWord.contains(badWord) else cleanWord.equals(badWord, ignoreCase = true)
                }) return
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
                if (isMyanmar) myUserTrieEngine.insert(finalCleanWord, 1) else if (isThai) thUserTrieEngine.insert(finalCleanWord, 1) else enUserTrieEngine.insert(finalCleanWord, 1)

                serviceScope.launch {
                    try { db.suggestionDao().upsert(finalCleanWord, "", 1) } catch (e: Exception) {}
                }
            }

            if (pairCount % 3 == 0 && finalPrevWord.isNotBlank()) {
                android.util.Log.d("NoxDebug", "saveTypingData: Inserting pair '$finalPrevWord -> $finalCleanWord' permanently")
                if (isMyanmar) {
                    myUserNextWordEngine.insert(finalPrevWord, finalCleanWord, 1)
                } else if (isThai) {
                    thUserNextWordEngine.insert(finalPrevWord, finalCleanWord, 1)
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
        activePanelState.value = "KEYBOARD"
        composeView?.visibility = android.view.View.GONE
        keyboardView.visibility = android.view.View.VISIBLE
    }

    private var activeAppPackageName: String = "com.google.android.apps.messaging"

    override fun onWindowShown() {
        super.onWindowShown()
        if (::keyboardView.isInitialized) {
            updateSuggestions()
            keyboardView.requestLayout()
            keyboardView.invalidate()
        }
    }

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

        if (!restarting) {
            if (isWaitingForVaultUnlock) {
                isWaitingForVaultUnlock = false
            } else {
                closeEmojiPicker()
                hideVaultPanel()
                hideToolsMenuPanel()
            }
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
                    val prefs = NoxBoardPrefs(this)
                    keyboardView.currentMode = when(prefs.defaultLanguage) {
                        "THAI" -> com.noxtan.noxboard.KeyboardMode.THAI
                        "ENGLISH" -> com.noxtan.noxboard.KeyboardMode.ENGLISH
                        else -> com.noxtan.noxboard.KeyboardMode.MYANMAR
                    }
                }
            }
        } else {
            currentActionId = EditorInfo.IME_ACTION_NONE
            isMultiLine = false
        }

        if (::keyboardView.isInitialized) {
            val isSelecting = info != null && info.initialSelStart != info.initialSelEnd && info.initialSelStart >= 0 && info.initialSelEnd >= 0
            hasSelectionState.value = isSelecting
            keyboardView.hasSelection = isSelecting

            val prefs = NoxBoardPrefs(this)
            keyboardView.applySettings(prefs)
            applyNavigationBarTheme()
            themeUpdateTrigger.intValue += 1
        }
        loadMyanmarEngine()
        checkAutoCapitalization()
        updateSuggestions()
        applyFloatingState() // <--- ဒီစာကြောင်းလေး အသစ်ထည့်ပေးပါ
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        updateSuggestions()
        checkAutoCapitalization()

        val isSelecting = newSelStart != newSelEnd && newSelStart >= 0 && newSelEnd >= 0
        hasSelectionState.value = isSelecting

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
        hasSelectionState.value = false

        if (::keyboardView.isInitialized) {
            keyboardView.hasSelection = false
            keyboardView.invalidate()
        }
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

        keyboardWrapper = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // --- Snap Indicator (အောက်ခြေက ဘောင်ပါးပါးလေး) ---
        snapIndicatorView = View(this).apply {
            val density = resources.displayMetrics.density
            // Landscape ဆိုရင် တစ်ဝက် (24dp) ပဲထားမယ်၊ Portrait ဆိုရင် 48dp
            val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            val indicatorHeight = if (isLandscape) 24f else 48f

            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (indicatorHeight * density).toInt()
            ).apply {
                gravity = android.view.Gravity.BOTTOM
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#33808080"))
                setStroke((1.5f * density).toInt(), android.graphics.Color.parseColor("#66808080"))
            }
            visibility = View.GONE
        }

        // --- Drag Handle ---
        dragHandle = FrameLayout(this).apply {
            val density = resources.displayMetrics.density
            layoutParams = android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (32 * density).toInt()
            )
            visibility = View.GONE

            val visualBar = View(context).apply {
                layoutParams = FrameLayout.LayoutParams((40 * density).toInt(), (5 * density).toInt()).apply {
                    gravity = android.view.Gravity.CENTER
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#888888"))
                    cornerRadius = 10f * density
                }
            }
            addView(visualBar)

            fun createResizeTouchZone(isLeft: Boolean): View {
                return View(context).apply {
                    layoutParams = FrameLayout.LayoutParams((48 * density).toInt(), (32 * density).toInt()).apply {
                        gravity = if (isLeft) (android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL)
                        else (android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL)
                    }
                    background = null
                }
            }

            val leftResizeHandle = createResizeTouchZone(isLeft = true)
            val rightResizeHandle = createResizeTouchZone(isLeft = false)
            addView(leftResizeHandle)
            addView(rightResizeHandle)

            var initialX = 0f
            var initialY = 0f
            var initialTouchX = 0f
            var initialTouchY = 0f

            setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        initialX = keyboardWrapper.translationX
                        initialY = keyboardWrapper.translationY
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    android.view.MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY

                        val minX = 0f
                        val maxX = (frameLayout.width - keyboardWrapper.width).toFloat()
                        val minY = 0f

                        val newX = (initialX + dx).coerceIn(minX, maxX)
                        val newY = Math.max(minY, initialY + dy)

                        keyboardWrapper.translationX = newX
                        keyboardWrapper.translationY = newY

                        val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                        val thresholdDp = if (isLandscape) 26f else 50f
                        val snapThreshold = frameLayout.height - (thresholdDp * density)
                        val currentBottomY = newY + keyboardWrapper.height

                        if (currentBottomY >= snapThreshold) {
                            snapIndicatorView.visibility = View.VISIBLE
                        } else {
                            snapIndicatorView.visibility = View.GONE
                        }
                        true
                    }
                    android.view.MotionEvent.ACTION_UP -> {
                        snapIndicatorView.visibility = View.GONE

                        val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                        val thresholdDp = if (isLandscape) 26f else 50f
                        val snapThreshold = frameLayout.height - (thresholdDp * density)
                        val currentBottomY = keyboardWrapper.translationY + keyboardWrapper.height

                        val prefs = NoxBoardPrefs(this@NoxKeyboardService)

                        if (currentBottomY >= snapThreshold) {
                            if (isLandscape) {
                                prefs.floatingLandscapeX = -1f
                                prefs.floatingLandscapeY = -1f
                            } else {
                                prefs.floatingPortraitX = -1f
                                prefs.floatingPortraitY = -1f
                            }
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                toggleFloatingMode(false)
                            }
                        } else {
                            if (isLandscape) {
                                prefs.floatingLandscapeX = keyboardWrapper.translationX
                                prefs.floatingLandscapeY = keyboardWrapper.translationY
                            } else {
                                prefs.floatingPortraitX = keyboardWrapper.translationX
                                prefs.floatingPortraitY = keyboardWrapper.translationY
                            }
                            window?.window?.decorView?.requestApplyInsets()
                        }
                        true
                    }
                    else -> false
                }
            }

            fun attachResizeListener(handle: View, isLeft: Boolean) {
                var startW = 0
                var startH = 0f
                var startRawX = 0f
                var startRawY = 0f
                var startTransX = 0f
                var startTransY = 0f

                handle.setOnTouchListener { _, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            parent.requestDisallowInterceptTouchEvent(true)
                            startW = keyboardWrapper.width
                            startH = keyboardView.theme.keyboardHeightDp
                            startRawX = event.rawX
                            startRawY = event.rawY
                            startTransX = keyboardWrapper.translationX
                            startTransY = keyboardWrapper.translationY

                            keyboardWrapper.background = android.graphics.drawable.GradientDrawable().apply {
                                setColor(keyboardView.theme.backgroundColor)
                                cornerRadius = 16f * density
                                setStroke((2f * density).toInt(), keyboardView.theme.activeKeyColor)
                            }
                            true
                        }
                        android.view.MotionEvent.ACTION_MOVE -> {
                            val dx = event.rawX - startRawX
                            val dy = event.rawY - startRawY

                            val isTablet = resources.configuration.smallestScreenWidthDp >= 600
                            val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                            val screenW = resources.displayMetrics.widthPixels.toFloat()

                            val minWidth = if (isTablet) {
                                360f * density
                            } else if (isLandscape) {
                                maxOf(270f * density, screenW * 0.4f)
                            } else {
                                maxOf(270f * density, screenW * 0.62f)
                            }
                            val maxWidth = if (isTablet) minOf(screenW * 0.90f, 580f * density) else screenW * 0.95f

                            val minHeight = if (isTablet) 200f else 180f
                            val maxHeight = if (isTablet) 400f else 340f

                            val newW = if (isLeft) (startW - dx).coerceIn(minWidth, maxWidth)
                            else (startW + dx).coerceIn(minWidth, maxWidth)

                            if (isLeft) {
                                val widthDiff = newW - startW
                                val proposedX = startTransX - widthDiff
                                keyboardWrapper.translationX = proposedX.coerceIn(0f, screenW - newW)
                            }

                            keyboardWrapper.layoutParams.width = newW.toInt()
                            keyboardWrapper.requestLayout()

                            val newH = (startH - (dy / density)).coerceIn(minHeight, maxHeight)

                            val heightDiffPx = (newH - startH) * density
                            keyboardWrapper.translationY = startTransY - heightDiffPx

                            keyboardView.theme.keyboardHeightDp = newH
                            keyboardView.requestLayout()
                            keyboardView.invalidate()

                            composeView?.let { cv ->
                                if (cv.visibility == View.VISIBLE) {
                                    keyboardView.measure(
                                        View.MeasureSpec.makeMeasureSpec(keyboardWrapper.width, View.MeasureSpec.EXACTLY),
                                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                                    )
                                    cv.layoutParams.height = keyboardView.measuredHeight
                                    cv.requestLayout()
                                }
                            }
                            true
                        }
                        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                            parent.requestDisallowInterceptTouchEvent(false)
                            keyboardWrapper.background = android.graphics.drawable.GradientDrawable().apply {
                                setColor(keyboardView.theme.backgroundColor)
                                cornerRadius = 16f * density
                            }

                            val prefs = NoxBoardPrefs(this@NoxKeyboardService)
                            prefs.floatingWidthDp = keyboardWrapper.width / density
                            prefs.floatingHeightDp = keyboardView.theme.keyboardHeightDp
                            true
                        }
                        else -> false
                    }
                }
            }

            attachResizeListener(leftResizeHandle, isLeft = true)
            attachResizeListener(rightResizeHandle, isLeft = false)
        }
        keyboardWrapper.addView(dragHandle)

        keyboardView = NoxKeyboardView(this)
        keyboardView.listener = this
        val currentModeState = androidx.compose.runtime.mutableStateOf(keyboardView.currentMode)
        keyboardView.onModeChanged = { currentModeState.value = it }
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
                    val trigger = themeUpdateTrigger.intValue
                    val currentPanel by activePanelState
                    val currentMode by currentModeState
                    val hasSelection by hasSelectionState
                    val suggestions by _suggestionsFlow.collectAsState()
                    val typedWord by _typedWordFlow.collectAsState()
                    val hasActiveSuggestions by _hasActiveSuggestionsFlow.collectAsState()
                    var wordToRemove by remember { mutableStateOf<String?>(null) }

                    val localCtx = androidx.compose.ui.platform.LocalContext.current
                    val prefs = remember { com.noxtan.noxboard.NoxBoardPrefs(localCtx) }
                    val shortcutListStr = prefs.customShortcutRow
                    val shortcutList = remember(shortcutListStr) { shortcutListStr.split(",").filter { it.isNotEmpty() }.take(6) }

                    val bgColor = Color(keyboardView.theme.backgroundColor)
                    val textColor = Color(keyboardView.getResolvedSuggestionTextColor())

                    // Custom Icons တွေကို ပြန်ခေါ်မယ်
                    val toolsIconRes = remember(prefs.iconStyleSetting) { if (prefs.iconStyleSetting == "DEFAULT") com.noxtan.noxboard.R.drawable.ic_tools_menu else localCtx.resources.getIdentifier(prefs.iconStyleSetting, "drawable", localCtx.packageName).takeIf { it != 0 } ?: com.noxtan.noxboard.R.drawable.ic_tools_menu }
                    val emojiIconRes = remember(prefs.iconStyleEmoji) { if (prefs.iconStyleEmoji == "DEFAULT") com.noxtan.noxboard.R.drawable.ic_emoji else localCtx.resources.getIdentifier(prefs.iconStyleEmoji, "drawable", localCtx.packageName).takeIf { it != 0 } ?: com.noxtan.noxboard.R.drawable.ic_emoji }
                    val numpadIconRes = remember(prefs.iconStyleNumpad) { if (prefs.iconStyleNumpad == "DEFAULT") com.noxtan.noxboard.R.drawable.ic_numpad else localCtx.resources.getIdentifier(prefs.iconStyleNumpad, "drawable", localCtx.packageName).takeIf { it != 0 } ?: com.noxtan.noxboard.R.drawable.ic_numpad }

                    val isTypingMode = currentMode == com.noxtan.noxboard.KeyboardMode.MYANMAR ||
                            currentMode == com.noxtan.noxboard.KeyboardMode.ENGLISH ||
                            currentMode == com.noxtan.noxboard.KeyboardMode.THAI

                    if ((currentPanel == "KEYBOARD" && isTypingMode) || currentPanel == "TOOLS") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .background(bgColor)
                        ) {
                            if (currentPanel == "TOOLS") {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        activePanelState.value = "KEYBOARD"
                                        composeView?.visibility = android.view.View.GONE
                                        keyboardView.visibility = android.view.View.VISIBLE
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = textColor)
                                    }
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        shortcutList.forEach { action ->
                                            IconButton(onClick = { onKey(-1000, action) }) {
                                                when (action) {
                                                    "VAULT" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_pass), contentDescription = "Vault", tint = textColor, modifier = Modifier.size(20.dp))
                                                    "CLIPBOARD" -> {
                                                        val icon = if (hasSelection) com.noxtan.noxboard.R.drawable.ic_copy else com.noxtan.noxboard.R.drawable.ic_paste
                                                        Icon(painterResource(id = icon), contentDescription = "Clipboard", tint = textColor, modifier = Modifier.size(20.dp))
                                                    }
                                                    "COPY" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_copy), contentDescription = "Copy", tint = textColor, modifier = Modifier.size(20.dp))
                                                    "PASTE" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_paste), contentDescription = "Paste", tint = textColor, modifier = Modifier.size(20.dp))
                                                    "CUT" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_cut), contentDescription = "Cut", tint = textColor, modifier = Modifier.size(20.dp))
                                                    "UNDO" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_undo), contentDescription = "Undo", tint = textColor, modifier = Modifier.size(20.dp))
                                                    "REDO" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_redo), contentDescription = "Redo", tint = textColor, modifier = Modifier.size(20.dp))
                                                    "EMOJI" -> Icon(painterResource(id = emojiIconRes), contentDescription = "Emoji", tint = textColor, modifier = Modifier.size(20.dp))
                                                    "CHANGE_LANGUAGE" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_earth), contentDescription = "Language", tint = textColor, modifier = Modifier.size(20.dp))
                                                    "TOOLS" -> Icon(painterResource(id = toolsIconRes), contentDescription = "Tools", tint = textColor, modifier = Modifier.size(20.dp))
                                                    "HIDE" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_hide_keys_classic_down), contentDescription = "Hide", tint = textColor, modifier = Modifier.size(20.dp))
                                                    "LEFT" -> Text("◀", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    "RIGHT" -> Text("▶", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    "UP" -> Text("▲", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    "DOWN" -> Text("▼", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    "SELECT_ALL" -> Text("All", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    "COPY_ALL" -> Text("All", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    else -> Text(action.take(3), color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            requestHideSelf(0)
                                            hideToolsMenuPanel()
                                            val intent = android.content.Intent(this@NoxKeyboardService, com.noxtan.noxboard.MainActivity::class.java).apply {
                                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            startActivity(intent)
                                        }
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = textColor)
                                    }
                                }
                            } else {
                                if (wordToRemove != null) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Remove '$wordToRemove'?", color = textColor, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                        Row {
                                            TextButton(onClick = { wordToRemove = null }) { Text("No", color = Color.Gray) }
                                            TextButton(onClick = {
                                                val displayWord = wordToRemove ?: return@TextButton
                                                wordToRemove = null
                                                val rawWord = currentSuggestionsMap[displayWord] ?: displayWord
                                                val isMyanmar = rawWord.any { it in '\u1000'..'\u109F' }
                                                if (isMyanmar) { myTrieEngine.delete(rawWord); myUserTrieEngine.delete(rawWord) } else { enTrieEngine.delete(rawWord); enUserTrieEngine.delete(rawWord) }
                                                temporarilyDeletedWords.add(rawWord)
                                                serviceScope.launch {
                                                    db.suggestionDao().deleteWord(rawWord)
                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { updateSuggestions() }
                                                }
                                            }) { Text("Yes", color = Color.Red, fontWeight = FontWeight.Bold) }
                                        }
                                    }
                                } else if (prefs.isSuggestionEnabled && hasActiveSuggestions) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = {
                                            activePanelState.value = "TOOLS"
                                            showToolsMenuPanel()
                                        }) {
                                            Icon(painterResource(id = toolsIconRes), contentDescription = "Tools", tint = textColor, modifier = Modifier.size(20.dp))
                                        }
                                        androidx.compose.foundation.lazy.LazyRow(
                                            modifier = Modifier.weight(1f).fillMaxHeight(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            contentPadding = PaddingValues(horizontal = 4.dp)
                                        ) {
                                            val filteredSuggestions = suggestions.filter { it != typedWord }
                                            if (typedWord.isNotEmpty()) {
                                                item {
                                                    val scrollState = androidx.compose.foundation.rememberScrollState()

                                                    androidx.compose.runtime.LaunchedEffect(typedWord, scrollState.maxValue) {
                                                        scrollState.scrollTo(scrollState.maxValue)
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .then(
                                                                if (filteredSuggestions.isEmpty()) Modifier.fillParentMaxWidth()
                                                                else Modifier.widthIn(min = 40.dp, max = 130.dp)
                                                            )
                                                            .clickable { insertSuggestion(typedWord) }
                                                            .padding(horizontal = 8.dp),
                                                        contentAlignment = if (filteredSuggestions.isEmpty()) Alignment.CenterStart else Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = typedWord,
                                                            color = textColor,
                                                            fontSize = 15.sp,
                                                            maxLines = 1,
                                                            softWrap = false,
                                                            modifier = Modifier.horizontalScroll(scrollState)
                                                        )
                                                    }
                                                }
                                            }
                                            itemsIndexed(filteredSuggestions) { index, word ->
                                                val isBest = index == 0
                                                Box(modifier = Modifier.fillMaxHeight().combinedClickable(onClick = { insertSuggestion(word) }, onLongClick = { wordToRemove = word }).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                                                    Text(text = word, color = if (isBest) textColor else textColor.copy(alpha=0.7f), fontSize = 15.sp, fontWeight = if(isBest) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                                                }
                                            }
                                        }
                                        val isClassic = prefs.activeLayoutMyanmar == "CLASSIC" || prefs.activeLayoutEnglish == "CLASSIC"
                                        IconButton(onClick = { if (isClassic) showEmojiPicker() else { keyboardView.isNumpadMode = true; keyboardView.invalidate() } }) {
                                            Icon(painterResource(id = if (isClassic) emojiIconRes else numpadIconRes), contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = {
                                            activePanelState.value = "TOOLS"
                                            showToolsMenuPanel()
                                        }) {
                                            Icon(painterResource(id = toolsIconRes), contentDescription = "Tools", tint = textColor, modifier = Modifier.size(20.dp))
                                        }
                                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                            shortcutList.forEach { action ->
                                                IconButton(onClick = { onKey(-1000, action) }) {
                                                    when (action) {
                                                        "VAULT" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_pass), contentDescription = "Vault", tint = textColor, modifier = Modifier.size(20.dp))
                                                        "CLIPBOARD" -> {
                                                            val icon = if (hasSelection) com.noxtan.noxboard.R.drawable.ic_copy else com.noxtan.noxboard.R.drawable.ic_paste
                                                            Icon(painterResource(id = icon), contentDescription = "Clipboard", tint = textColor, modifier = Modifier.size(20.dp))
                                                        }
                                                        "COPY" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_copy), contentDescription = "Copy", tint = textColor, modifier = Modifier.size(20.dp))
                                                        "PASTE" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_paste), contentDescription = "Paste", tint = textColor, modifier = Modifier.size(20.dp))
                                                        "CUT" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_cut), contentDescription = "Cut", tint = textColor, modifier = Modifier.size(20.dp))
                                                        "UNDO" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_undo), contentDescription = "Undo", tint = textColor, modifier = Modifier.size(20.dp))
                                                        "REDO" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_redo), contentDescription = "Redo", tint = textColor, modifier = Modifier.size(20.dp))
                                                        "EMOJI" -> Icon(painterResource(id = emojiIconRes), contentDescription = "Emoji", tint = textColor, modifier = Modifier.size(20.dp))
                                                        "CHANGE_LANGUAGE" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_earth), contentDescription = "Language", tint = textColor, modifier = Modifier.size(20.dp))
                                                        "TOOLS" -> Icon(painterResource(id = toolsIconRes), contentDescription = "Tools", tint = textColor, modifier = Modifier.size(20.dp))
                                                        "HIDE" -> Icon(painterResource(id = com.noxtan.noxboard.R.drawable.ic_hide_keys_classic_down), contentDescription = "Hide", tint = textColor, modifier = Modifier.size(20.dp))
                                                        "LEFT" -> Text("◀", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                        "RIGHT" -> Text("▶", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                        "UP" -> Text("▲", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                        "DOWN" -> Text("▼", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                        "SELECT_ALL" -> Text("All", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                        "COPY_ALL" -> Text("All", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                        else -> Text(action.take(3), color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                        val isClassic =
                                            prefs.activeLayoutMyanmar == "CLASSIC" || prefs.activeLayoutEnglish == "CLASSIC"
                                        IconButton(onClick = {
                                            if (isClassic) showEmojiPicker() else {
                                                keyboardView.isNumpadMode =
                                                    true; keyboardView.invalidate()
                                            }
                                        }) {
                                            Icon(
                                                painterResource(id = if (isClassic) emojiIconRes else numpadIconRes),
                                                contentDescription = null,
                                                tint = textColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val keyboardContainer = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        keyboardContainer.addView(suggestionComposeView, android.widget.LinearLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        keyboardContainer.addView(keyboardView, android.widget.LinearLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        composeView?.let { cv ->
            cv.visibility = View.GONE
            keyboardContainer.addView(cv, android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }

        keyboardWrapper.addView(keyboardContainer)
        frameLayout.addView(keyboardWrapper)
        frameLayout.addView(snapIndicatorView)

        applyFloatingState()

        return frameLayout
    }

    private fun showVaultPanel() {
        val view = composeView ?: return
        try {
            val isFloating = NoxBoardPrefs(this).isFloating
            val effectiveBottomPadding = if (isFloating) 0f else keyboardView.getEffectiveBottomPaddingDp()
            composePaddingState.floatValue = effectiveBottomPadding // State ကို Initial လုပ်ပေးရန်

            val exactHeightPx = if (keyboardView.height > 0) {
                keyboardView.height
            } else {
                keyboardView.measure(
                    android.view.View.MeasureSpec.makeMeasureSpec(keyboardWrapper.width, android.view.View.MeasureSpec.EXACTLY),
                    android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
                )
                keyboardView.measuredHeight
            }

            val headHeightPx = (42f * resources.displayMetrics.density).toInt()
            val panelHeightPx = exactHeightPx + headHeightPx

            activePanelState.value = "VAULT"
            val params = view.layoutParams as? android.widget.LinearLayout.LayoutParams ?: android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                panelHeightPx
            )
            params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            params.height = panelHeightPx
            view.layoutParams = params

            keyboardView.visibility = android.view.View.GONE
            view.visibility = android.view.View.VISIBLE

            view.setContent {
                NoxBoardTheme {
                    var accounts by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<VaultAccountInfo>>(emptyList()) }
                    var isLocked by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                    fun refreshData() {
                        serviceScope.launch {
                            val result = com.noxtan.noxboard.provider.VaultAidlClient.fetchAccounts(this@NoxKeyboardService, activeAppPackageName)
                            kotlinx.coroutines.withContext(Dispatchers.Main) {
                                when (result) {
                                    is com.noxtan.noxboard.provider.VaultAidlClient.VaultResult.Success -> {
                                        accounts = result.accounts
                                        isLocked = false
                                    }
                                    is com.noxtan.noxboard.provider.VaultAidlClient.VaultResult.Locked -> {
                                        accounts = emptyList()
                                        isLocked = true
                                    }
                                    is com.noxtan.noxboard.provider.VaultAidlClient.VaultResult.Error -> {
                                        accounts = emptyList()
                                        isLocked = false
                                    }
                                }
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
                        bottomPaddingDp = composePaddingState.floatValue,
                        accounts = accounts,
                        isLocked = isLocked,
                        onUnlockClick = {
                            isWaitingForVaultUnlock = true

                            val intent = packageManager.getLaunchIntentForPackage("com.noxtan.pass")?.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            if (intent != null) {
                                startActivity(intent)
                            } else {
                                android.widget.Toast.makeText(this@NoxKeyboardService, "Noxtan Pass is not installed", android.widget.Toast.LENGTH_SHORT).show()
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
        activePanelState.value = "KEYBOARD"
        composeView?.visibility = View.GONE
        keyboardView.visibility = View.VISIBLE
    }

    private fun showToolsMenuPanel() {
        val view = composeView ?: return
        try {
            val isFloating = NoxBoardPrefs(this).isFloating
            val effectiveBottomPadding = if (isFloating) 0f else keyboardView.getEffectiveBottomPaddingDp()
            composePaddingState.floatValue = effectiveBottomPadding // State ကို Initial လုပ်ပေးရန်

            val exactHeightPx = if (keyboardView.height > 0) {
                keyboardView.height
            } else {
                keyboardView.measure(
                    View.MeasureSpec.makeMeasureSpec(keyboardWrapper.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                keyboardView.measuredHeight
            }

            activePanelState.value = "TOOLS"
            val params = view.layoutParams as? android.widget.LinearLayout.LayoutParams ?: android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                exactHeightPx
            )
            params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            params.height = exactHeightPx
            view.layoutParams = params

            keyboardView.visibility = android.view.View.GONE
            view.visibility = android.view.View.VISIBLE

            view.setContent {
                com.noxtan.noxboard.ui.theme.NoxBoardTheme {
                    com.noxtan.noxboard.ui.screens.ToolsMenuPanel(
                        bottomPaddingDp = composePaddingState.floatValue,
                        hasSelection = keyboardView.hasSelection,
                        onClose = { hideToolsMenuPanel() },
                        onActionClick = { actionId ->
                            if (actionId == "CLIPBOARD") {
                                onKey(-13, "")
                            } else if (actionId == "HISTORY") {
                                showClipboardPanel()
                            } else if (actionId == "VAULT") {
                                showVaultPanel()
                            } else if (actionId == "EMOJI") {
                                showEmojiPicker()
                            } else {
                                onKey(-1000, actionId)
                            }
                        },
                        onCutClick = {
                            currentInputConnection?.performContextMenuAction(android.R.id.cut)
                        },
                        onCopyClick = {
                            currentInputConnection?.performContextMenuAction(android.R.id.copy)
                        },
                        onPasteClick = {
                            currentInputConnection?.performContextMenuAction(android.R.id.paste)
                        },
                        onSelectAllClick = {
                            currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
                        },
                        onLeftArrowClick = {
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_LEFT))
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_LEFT))
                        },
                        onRightArrowClick = {
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                        },
                        onClipboardHistoryClick = {
                            showClipboardPanel()
                        },
                        onVaultClick = {
                            showVaultPanel()
                        },
                        onSettingsClick = {
                            requestHideSelf(0)
                            hideToolsMenuPanel()
                            val intent = android.content.Intent(this@NoxKeyboardService, MainActivity::class.java).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(intent)
                        },
                        onFloatingClick = {
                            toggleFloatingMode(!NoxBoardPrefs(this@NoxKeyboardService).isFloating)
                        }
                    )
                }
            }
        } catch (e: Throwable) {
            hideToolsMenuPanel()
        }
    }

    private fun showClipboardPanel() {
        val view = composeView ?: return
        try {
            val isFloating = NoxBoardPrefs(this).isFloating
            val effectiveBottomPadding = if (isFloating) 0f else keyboardView.getEffectiveBottomPaddingDp()
            composePaddingState.floatValue = effectiveBottomPadding // State ကို Initial လုပ်ပေးရန်

            val exactHeightPx = if (keyboardView.height > 0) {
                keyboardView.height
            } else {
                keyboardView.measure(
                    android.view.View.MeasureSpec.makeMeasureSpec(keyboardWrapper.width, android.view.View.MeasureSpec.EXACTLY),
                    android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
                )
                keyboardView.measuredHeight
            }

            val headHeightPx = (42f * resources.displayMetrics.density).toInt()
            val panelHeightPx = exactHeightPx + headHeightPx

            activePanelState.value = "CLIPBOARD"
            val params = view.layoutParams as? android.widget.LinearLayout.LayoutParams ?: android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                panelHeightPx
            )
            params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            params.height = panelHeightPx
            view.layoutParams = params

            keyboardView.visibility = android.view.View.GONE
            view.visibility = android.view.View.VISIBLE

            view.setContent {
                com.noxtan.noxboard.ui.theme.NoxBoardTheme {
                    com.noxtan.noxboard.ui.screens.ClipboardHistoryPanel(
                        bottomPaddingDp = composePaddingState.floatValue,
                        historyList = clipboardHistory,
                        onClose = { hideToolsMenuPanel() },
                        onItemClick = { text ->
                            currentInputConnection?.commitText(text, 1)
                            hideToolsMenuPanel()
                        }
                    )
                }
            }
        } catch (e: Throwable) {
            hideToolsMenuPanel()
        }
    }

    private fun hideToolsMenuPanel() {
        activePanelState.value = "KEYBOARD"
        composeView?.visibility = android.view.View.GONE
        keyboardView.visibility = android.view.View.VISIBLE
    }

    private fun showEmojiPicker() {
        val view = composeView ?: return
        try {
            val isFloating = NoxBoardPrefs(this).isFloating
            val effectiveBottomPadding = if (isFloating) 0f else keyboardView.getEffectiveBottomPaddingDp()
            composePaddingState.floatValue = effectiveBottomPadding // State ကို Initial လုပ်ပေးရန်

            val exactHeightPx = if (keyboardView.height > 0) {
                keyboardView.height
            } else {
                keyboardView.measure(
                    android.view.View.MeasureSpec.makeMeasureSpec(keyboardWrapper.width, android.view.View.MeasureSpec.EXACTLY),
                    android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
                )
                keyboardView.measuredHeight
            }

            val headHeightPx = (42f * resources.displayMetrics.density).toInt()
            val panelHeightPx = exactHeightPx + headHeightPx

            activePanelState.value = "EMOJI"
            val params = view.layoutParams as? android.widget.LinearLayout.LayoutParams ?: android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                panelHeightPx
            )
            params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            params.height = panelHeightPx
            view.layoutParams = params

            keyboardView.visibility = android.view.View.GONE
            view.visibility = android.view.View.VISIBLE

            view.setContent {
                com.noxtan.noxboard.ui.theme.NoxBoardTheme {
                    com.noxtan.noxboard.ui.screens.EmojiPanel(
                        bottomPaddingDp = composePaddingState.floatValue,
                        onClose = { closeEmojiPicker() },
                        onEmojiClick = { currentInputConnection?.commitText(it, 1) },
                        onBackspace = {
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DEL))
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DEL))
                        },
                        onSpace = { currentInputConnection?.commitText(" ", 1) }
                    )
                }
            }
        } catch (e: Throwable) {
            closeEmojiPicker()
        }
    }

    override fun onKey(code: Int, text: String): Boolean {
        val ic = currentInputConnection ?: return false

        if (code == -99 && text == "CLOSE_EMOJI") {
            closeEmojiPicker()
            return true
        }

        var success = true

        when (code) {
            -1000 -> {
                android.util.Log.d("NoxGesture", "Gesture Triggered: $text")

                when (text) {
                    "UNDO" -> {
                        val t = android.os.SystemClock.uptimeMillis()
                        ic.sendKeyEvent(android.view.KeyEvent(t, t, android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_Z, 0, android.view.KeyEvent.META_CTRL_ON))
                        ic.sendKeyEvent(android.view.KeyEvent(t, t, android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_Z, 0, android.view.KeyEvent.META_CTRL_ON))
                    }
                    "REDO" -> {
                        val t = android.os.SystemClock.uptimeMillis()
                        val metaState = android.view.KeyEvent.META_CTRL_ON or android.view.KeyEvent.META_SHIFT_ON
                        ic.sendKeyEvent(android.view.KeyEvent(t, t, android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_Z, 0, metaState))
                        ic.sendKeyEvent(android.view.KeyEvent(t, t, android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_Z, 0, metaState))
                    }
                    "COPY" -> {
                        val result = ic.performContextMenuAction(android.R.id.copy)
                        android.util.Log.d("NoxGesture", "COPY action result: $result")
                    }
                    "PASTE" -> ic.performContextMenuAction(android.R.id.paste)
                    "CUT" -> ic.performContextMenuAction(android.R.id.cut)
                    "SELECT_ALL" -> ic.performContextMenuAction(android.R.id.selectAll)

                    "COPY_ALL" -> {
                        ic.performContextMenuAction(android.R.id.selectAll)
                        ic.performContextMenuAction(android.R.id.copy)
                        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                        android.widget.Toast.makeText(this, "Copied All", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    "CHANGE_LANGUAGE" -> {
                        if (::keyboardView.isInitialized) {
                            keyboardView.switchLanguage()
                        }
                    }
                    "VAULT" -> showVaultPanel()
                    "TOOLS" -> showToolsMenuPanel()

                    "CURSOR_LEFT_WORD" -> {
                        val t = android.os.SystemClock.uptimeMillis()
                        ic.sendKeyEvent(android.view.KeyEvent(t, t, android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_LEFT, 0, android.view.KeyEvent.META_CTRL_ON))
                        ic.sendKeyEvent(android.view.KeyEvent(t, t, android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_LEFT, 0, android.view.KeyEvent.META_CTRL_ON))
                    }
                    "CURSOR_RIGHT_WORD" -> {
                        val t = android.os.SystemClock.uptimeMillis()
                        ic.sendKeyEvent(android.view.KeyEvent(t, t, android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT, 0, android.view.KeyEvent.META_CTRL_ON))
                        ic.sendKeyEvent(android.view.KeyEvent(t, t, android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_RIGHT, 0, android.view.KeyEvent.META_CTRL_ON))
                    }
                    "CURSOR_START" -> {
                        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MOVE_HOME))
                        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MOVE_HOME))
                    }
                    "CURSOR_END" -> {
                        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MOVE_END))
                        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MOVE_END))
                    }
                    "EMOJI" -> showEmojiPicker()
                    "CLIPBOARD" -> onKey(-13, "")
                    "HISTORY" -> showClipboardPanel()
                    "HIDE" -> requestHideSelf(0)
                }
                return true
            }

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
                    val isThai = lastSavedWordStr.any { it in '\u0E00'..'\u0E7F' }
                    if (isMyanmar) {
                        myTrieEngine.delete(lastSavedWordStr)
                        myUserTrieEngine.delete(lastSavedWordStr)
                    } else if (isThai) {
                        thTrieEngine.delete(lastSavedWordStr)
                        thUserTrieEngine.delete(lastSavedWordStr)
                    } else {
                        enTrieEngine.delete(lastSavedWordStr)
                        enUserTrieEngine.delete(lastSavedWordStr)
                    }

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

                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
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
                showToolsMenuPanel()
            }
            -2 -> {
                val selectedText = ic.getSelectedText(0)
                if (!selectedText.isNullOrEmpty()) {
                    ic.commitText("", 1)
                } else {
                    val textBefore = ic.getTextBeforeCursor(2, 0)?.toString() ?: ""
                    if (textBefore.isNotEmpty()) {
                        if (textBefore.endsWith("\u200C")) {
                            if (textBefore.length == 2) {
                                ic.deleteSurroundingText(2, 0)
                            } else {
                                ic.deleteSurroundingText(1, 0)
                            }
                        } else {
                            ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DEL))
                            ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DEL))
                        }
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
            -36 -> {
                val textBefore = ic.getTextBeforeCursor(1, 0)?.toString() ?: ""
                if (textBefore == " ") {
                    ic.deleteSurroundingText(1, 0)
                }
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

                if (!isPunctuation && prefs.isAutoCorrectionEnabled) {
                    performAutoCorrect(ic, isInstant = true)
                }

                if (isPunctuation && prefs.isAutoSpaceEnabled) {
                    val noSpaceAfter = setOf("”", "’", "(", "[", "{")
                    if (!noSpaceAfter.contains(text)) {

                        var shouldSpace = true

                        val textBeforeFull = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""

                        val charBeforePunc = if (textBeforeFull.length > 1) textBeforeFull[textBeforeFull.length - 2] else ' '

                        val isAfterDigit = charBeforePunc.isDigit()

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

                        if (text == ".") {
                            val currentWord = textBeforeFull.dropLast(1).split(Regex("\\s+")).lastOrNull() ?: ""

                            if (currentWord.contains("@") ||
                                currentWord.startsWith("www", ignoreCase = true) ||
                                currentWord.startsWith("http", ignoreCase = true) ||
                                currentWord.endsWith("gmail", ignoreCase = true) ||
                                currentWord.endsWith("mail", ignoreCase = true)) {
                                shouldSpace = false
                            }

                            if (isAfterDigit) {
                                shouldSpace = false
                            }
                        } else if (text == ")" || text == ":" || text == ",") {
                            if (isAfterDigit) {
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
    private fun toggleFloatingMode(enable: Boolean) {
        val prefs = NoxBoardPrefs(this)
        prefs.isFloating = enable
        applyFloatingState()

        if (!enable) {
            // System ကို Window Insets အသစ်ပြန်တွက်ခိုင်းရန် (UI တွေကို မပိတ်ပါ)
            setInputView(frameLayout)
        } else {
            activePanelState.value = "KEYBOARD"
            composeView?.visibility = View.GONE
            keyboardView.visibility = View.VISIBLE
        }
    }

    private fun applyFloatingState() {
        try {
            if (!::frameLayout.isInitialized) return

            val prefs = NoxBoardPrefs(this)
            val isFloating = prefs.isFloating
            val displayMetrics = resources.displayMetrics
            val density = displayMetrics.density
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            if (isFloating) {
                dragHandle.visibility = View.VISIBLE

                val params = frameLayout.layoutParams ?: FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                frameLayout.layoutParams = params
                frameLayout.minimumHeight = screenHeight

                val isTablet = resources.configuration.smallestScreenWidthDp >= 600
                val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                val defaultFloatWidth = if (isTablet) (400 * density).toInt() else if (isLandscape) (screenWidth * 0.55f).toInt() else (screenWidth * 0.75f).toInt()

                val minLimit = if (isTablet) {
                    360f * density
                } else if (isLandscape) {
                    maxOf(270f * density, screenWidth * 0.4f)
                } else {
                    maxOf(270f * density, screenWidth * 0.62f)
                }
                val savedWidthDp = prefs.floatingWidthDp
                val floatWidth = if (savedWidthDp > 0f) (savedWidthDp * density).toInt().coerceIn(minLimit.toInt(), (screenWidth * 0.95f).toInt())
                else defaultFloatWidth

                val savedHeightDp = prefs.floatingHeightDp
                if (savedHeightDp > 0f) {
                    keyboardView.theme.keyboardHeightDp = savedHeightDp
                }

                val wrapperParams = keyboardWrapper.layoutParams as? FrameLayout.LayoutParams ?: FrameLayout.LayoutParams(floatWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
                wrapperParams.width = floatWidth
                wrapperParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                wrapperParams.gravity = android.view.Gravity.TOP or android.view.Gravity.START
                keyboardWrapper.layoutParams = wrapperParams

                keyboardWrapper.background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(keyboardView.theme.backgroundColor)
                    cornerRadius = 16f * density
                }
                keyboardWrapper.clipToOutline = true

                // ဒေါင်လိုက်/အလျားလိုက် အလိုက် သက်ဆိုင်ရာ မှတ်ထားတဲ့ နေရာကို ခေါ်ယူမယ်
                val savedX = if (isLandscape) prefs.floatingLandscapeX else prefs.floatingPortraitX
                val savedY = if (isLandscape) prefs.floatingLandscapeY else prefs.floatingPortraitY

                if (savedX == -1f && savedY == -1f) {
                    keyboardWrapper.translationX = (screenWidth - floatWidth) / 2f
                    val kbHeight = try { (keyboardView.getEffectiveKeyboardHeightDp() * density).toInt() } catch(e: Exception) { (300f * density).toInt() }
                    keyboardWrapper.translationY = (screenHeight / 2f) - (kbHeight / 2f)
                } else {
                    val maxX = Math.max(0f, (screenWidth - floatWidth).toFloat())
                    val maxY = Math.max(0f, (screenHeight - (150 * density)))

                    keyboardWrapper.translationX = savedX.coerceIn(0f, maxX)
                    keyboardWrapper.translationY = savedY.coerceIn(0f, maxY)
                }
            } else {
                dragHandle.visibility = View.GONE

                keyboardView.applySettings(prefs)
                applyNavigationBarTheme()

                val params = frameLayout.layoutParams ?: FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                frameLayout.layoutParams = params
                frameLayout.minimumHeight = 0

                val wrapperParams = keyboardWrapper.layoutParams as? FrameLayout.LayoutParams ?: FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                wrapperParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                wrapperParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                wrapperParams.gravity = android.view.Gravity.BOTTOM
                keyboardWrapper.layoutParams = wrapperParams

                keyboardWrapper.translationX = 0f
                keyboardWrapper.translationY = 0f
                keyboardWrapper.background = null
                keyboardWrapper.clipToOutline = false
            }

            // --- အသစ်ထည့်ရန် စတင် ---
            composePaddingState.floatValue = if (isFloating) 0f else keyboardView.getEffectiveBottomPaddingDp()

            composeView?.let { cv ->
                if (cv.visibility == View.VISIBLE) {
                    keyboardView.measure(
                        android.view.View.MeasureSpec.makeMeasureSpec(resources.displayMetrics.widthPixels, android.view.View.MeasureSpec.EXACTLY),
                        android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
                    )
                    val headHeightPx = if (activePanelState.value == "TOOLS") 0 else (42f * density).toInt()
                    val cvParams = cv.layoutParams
                    cvParams.height = keyboardView.measuredHeight + headHeightPx
                    cv.layoutParams = cvParams
                    cv.requestLayout()
                }
            }
            // --- အသစ်ထည့်ရန် ပြီးဆုံး ---

            frameLayout.requestLayout()
            window?.window?.decorView?.requestApplyInsets()

        } catch (e: Exception) {
            android.util.Log.e("NoxFloatingDebug", "Crash in applyFloatingState", e)
        }
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        // Landscape မှာ Android ရဲ့ Fullscreen စာရိုက်ကွင်းကြီး ပေါ်လာတာကို လုံးဝ ပိတ်ပစ်မယ်
        return false
    }

    override fun onComputeInsets(outInsets: Insets?) {
        super.onComputeInsets(outInsets)
        try {
            val prefs = NoxBoardPrefs(this)
            if (prefs.isFloating && ::keyboardWrapper.isInitialized && ::frameLayout.isInitialized) {
                val screenHeight = resources.displayMetrics.heightPixels
                outInsets?.contentTopInsets = screenHeight
                outInsets?.visibleTopInsets = screenHeight

                outInsets?.touchableInsets = Insets.TOUCHABLE_INSETS_REGION
                val rect = android.graphics.Rect()
                keyboardWrapper.getGlobalVisibleRect(rect)
                outInsets?.touchableRegion?.set(rect)
            }
        } catch (e: Exception) {
            android.util.Log.e("NoxFloatingDebug", "Crash in onComputeInsets", e)
        }
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