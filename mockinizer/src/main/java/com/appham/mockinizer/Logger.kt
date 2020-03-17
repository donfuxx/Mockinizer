package com.appham.mockinizer

import android.util.Log

object DebugLogger : Logger {
    override fun d(log: String) {
        Log.d("Mockinizer", log)
    }
}

interface Logger {
    fun d(log: String)
}