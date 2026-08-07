package com.noxtan.noxboard.layouts

import com.noxtan.noxboard.Key

abstract class BaseLayout {
    protected fun createRow(normals: List<String>, shifts: List<String>, weight: Float = 1f): List<Key> {
        return normals.indices.map { i -> Key(normals[i], shifts[i], 0, weight) }
    }
}