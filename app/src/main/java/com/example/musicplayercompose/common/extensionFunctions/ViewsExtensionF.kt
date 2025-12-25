package com.example.musicplayercompose.common.extensionFunctions

import android.content.Context
import android.os.SystemClock
import android.view.View
import android.view.inputmethod.InputMethodManager

object ViewsExtensionF {
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.hide() {
        visibility = View.GONE
    }

    fun View.hidden() {
        visibility = View.INVISIBLE
    }

    fun View.toggleVisibility() {
        visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    fun View.setOnOneClickListener(interval: Long = 600L, onSafeClick: (View) -> Unit) {
        var lastClickTime = 0L
        setOnClickListener {
            if (SystemClock.elapsedRealtime() - lastClickTime < interval) return@setOnClickListener
            lastClickTime = SystemClock.elapsedRealtime()
            onSafeClick(it)
        }
    }
}