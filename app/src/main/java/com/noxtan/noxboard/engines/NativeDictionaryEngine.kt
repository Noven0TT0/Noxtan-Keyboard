package com.noxtan.noxboard.engines

import android.content.res.AssetManager

object NativeDictionaryEngine {
    init {
        System.loadLibrary("noxboard_native")
    }

    external fun initEngine(assetManager: AssetManager): Boolean

    external fun getSuggestions(prefix: String, language: String, limit: Int): Array<String>

    external fun destroyEngine()
}