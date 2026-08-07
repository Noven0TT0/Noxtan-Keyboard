package com.noxtan.noxboard.engines
import android.view.inputmethod.InputConnection

interface MyanmarInputEngine {
    var activeDoubleTapMap: Map<String, String>
    fun handleKeyPress(text: String, ic: InputConnection)
    fun resetState()
}