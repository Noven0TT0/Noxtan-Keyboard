package com.noxtan.noxboard

import android.graphics.RectF

data class Key(
    val normalText: String = "",
    val shiftText: String = "",
    val code: Int = 0,
    val widthWeight: Float = 1f,
    var scaleX: Float = 1.0f,
    var scaleY: Float = 1.0f,
    var alignX: Float = 0.5f,
    var alignY: Float = 0.5f,
    var customIcon: String? = null,
    var rect: RectF = RectF()
)