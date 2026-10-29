#include <jni.h>
#include <string>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <vector>
#include <unordered_map>
#include <queue>
#include <algorithm>

#define LOG_TAG "NativeDict"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#pragma pack(push, 1)
struct TrieNode {
    uint16_t character;
    uint8_t is_end;
    uint8_t pad;
    int32_t freq;
    int32_t child_start_idx;
    int32_t child_count;
};
#pragma pack(pop)

struct Dictionary {
    AAsset* asset = nullptr;
    const TrieNode* nodes = nullptr;
    int32_t node_count = 0;
};

std::unordered_map<std::string, Dictionary> dicts;

bool loadDict(AAssetManager* mgr, const std::string& lang, const std::string& filename) {
    AAsset* asset = AAssetManager_open(mgr, filename.c_str(), AASSET_MODE_BUFFER);
    if (!asset) {
        LOGE("Failed to open %s", filename.c_str());
        return false;
    }
    const void* buf = AAsset_getBuffer(asset);
    if (!buf) {
        LOGE("Failed to get buffer for %s", filename.c_str());
        AAsset_close(asset);
        return false;
    }

    int32_t count = *(static_cast<const int32_t*>(buf));
    const TrieNode* nodes = reinterpret_cast<const TrieNode*>(static_cast<const uint8_t*>(buf) + 4);

    dicts[lang] = {asset, nodes, count};
    LOGI("Loaded %s successfully with %d nodes.", filename.c_str(), count);
    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_noxtan_noxboard_engines_NativeDictionaryEngine_initEngine(JNIEnv *env, jobject thiz, jobject assetManager) {
    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    if (mgr == nullptr) return JNI_FALSE;

    loadDict(mgr, "MYANMAR", "dict_my.bin");
    loadDict(mgr, "MYANMAR_PROVERBS", "proverbs_my.bin");
    loadDict(mgr, "ENGLISH", "dict_en.bin");
    loadDict(mgr, "THAI", "dict_th.bin");

    return JNI_TRUE;
}

struct SearchResult {
    std::u16string word;
    int32_t freq;
    bool operator<(const SearchResult& other) const {
        return freq > other.freq;
    }
};

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_noxtan_noxboard_engines_NativeDictionaryEngine_getSuggestions(JNIEnv *env, jobject thiz, jstring prefixStr, jstring languageStr, jint limit) {
    const char* lang_cstr = env->GetStringUTFChars(languageStr, nullptr);
    std::string lang(lang_cstr);
    env->ReleaseStringUTFChars(languageStr, lang_cstr);

    jclass stringClass = env->FindClass("java/lang/String");
    if (dicts.find(lang) == dicts.end() || dicts[lang].nodes == nullptr) {
        return env->NewObjectArray(0, stringClass, nullptr);
    }

    const Dictionary& dict = dicts[lang];
    const TrieNode* nodes = dict.nodes;

    const jchar* prefix_chars = env->GetStringChars(prefixStr, nullptr);
    jsize prefix_len = env->GetStringLength(prefixStr);
    std::u16string prefix(reinterpret_cast<const char16_t*>(prefix_chars), prefix_len);
    env->ReleaseStringChars(prefixStr, prefix_chars);

    int32_t curr_idx = 0;
    bool found = true;

    for (size_t i = 0; i < prefix.length(); ++i) {
        uint16_t target_char = prefix[i];
        const TrieNode& node = nodes[curr_idx];

        if (node.child_count == 0) {
            found = false; break;
        }

        int32_t start = node.child_start_idx;
        int32_t end = start + node.child_count;

        auto it = std::lower_bound(nodes + start, nodes + end, target_char, [](const TrieNode& n, uint16_t c) {
            return n.character < c;
        });

        if (it != nodes + end && it->character == target_char) {
            curr_idx = std::distance(nodes, it);
        } else {
            found = false; break;
        }
    }

    std::vector<SearchResult> top_results;

    if (found) {
        struct QueueNode {
            int32_t idx;
            std::u16string str;
            int32_t max_freq;
            bool operator<(const QueueNode& other) const {
                return max_freq < other.max_freq;
            }
        };

        std::priority_queue<QueueNode> q;
        q.push({curr_idx, prefix, nodes[curr_idx].freq});

        int visited = 0;
        while(!q.empty() && visited < 3000) {
            QueueNode curr = q.top();
            q.pop();
            visited++;

            const TrieNode& n = nodes[curr.idx];
            if (n.is_end) {
                top_results.push_back({curr.str, n.freq});
                std::push_heap(top_results.begin(), top_results.end());
                if (top_results.size() > limit) {
                    std::pop_heap(top_results.begin(), top_results.end());
                    top_results.pop_back();
                }
            }

            for (int32_t i = 0; i < n.child_count; ++i) {
                int32_t child_idx = n.child_start_idx + i;
                std::u16string next_str = curr.str + static_cast<char16_t>(nodes[child_idx].character);
                q.push({child_idx, next_str, nodes[child_idx].freq});
            }
        }
    }

    std::sort(top_results.begin(), top_results.end(), [](const SearchResult& a, const SearchResult& b){
        return a.freq > b.freq;
    });

    jobjectArray result = env->NewObjectArray(top_results.size(), stringClass, nullptr);
    for (size_t i = 0; i < top_results.size(); ++i) {
        jstring jstr = env->NewString(reinterpret_cast<const jchar*>(top_results[i].word.c_str()), top_results[i].word.length());
        env->SetObjectArrayElement(result, i, jstr);
        env->DeleteLocalRef(jstr);
    }

    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_noxtan_noxboard_engines_NativeDictionaryEngine_destroyEngine(JNIEnv *env, jobject thiz) {
    for (auto& pair : dicts) {
        if (pair.second.asset) {
            AAsset_close(pair.second.asset);
            pair.second.asset = nullptr;
        }
    }
    dicts.clear();
}