package com.noxtan.noxboard

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

object KeyboardSetupHelper {

    fun isKeyboardEnabled(context: Context): Boolean {
        val packageLocal = context.packageName
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethods = imm.enabledInputMethodList
        for (inputMethod in enabledInputMethods) {
            if (inputMethod.packageName == packageLocal) {
                return true
            }
        }
        return false
    }

    fun isKeyboardSelected(context: Context): Boolean {
        val currentIme = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) ?: return false

        val componentName = ComponentName.unflattenFromString(currentIme) ?: return false
        return componentName.packageName == context.packageName
    }
}