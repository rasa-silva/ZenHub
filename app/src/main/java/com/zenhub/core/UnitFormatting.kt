package com.zenhub.core

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)

fun String.asFuzzyDate(): CharSequence {
    val date = dateFormat.parse(this)
    return DateUtils.getRelativeTimeSpanString(date.time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
}

fun Long.asDigitalUnit(): CharSequence {
    return when {
        this > 1024 * 1024 -> (this / 1024 / 1024).toString() + "mb"
        this > 1024 -> (this / 1024).toString() + "kb"
        else -> this.toString() + 'b'
    }
}