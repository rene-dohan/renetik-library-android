package renetik.android.view.extensions

import android.view.View
import android.view.View.VISIBLE
import android.view.ViewPropertyAnimator
import renetik.android.framework.CSApplication.Companion.application

val shortAnimationDuration =
    application.resources.getInteger(android.R.integer.config_shortAnimTime)

val mediumAnimationDuration =
    application.resources.getInteger(android.R.integer.config_mediumAnimTime)

fun <T : View> T.fadeIn(duration: Int = mediumAnimationDuration): ViewPropertyAnimator? {
    if (isVisible) return null
    val originalAlpha = alpha; visibility = VISIBLE; alpha = 0f
    return animate().alpha(originalAlpha).setDuration(duration.toLong())
}

fun <T : View> T.fadeOut(
    duration: Int = shortAnimationDuration,
    onDone: (() -> Unit)? = null): ViewPropertyAnimator? = when {
    isGone -> {
        onDone?.invoke(); null
    }
    alpha == 0f -> {
        hide(); null
    }
    else -> {
        isClickable = false
        val originalAlpha = alpha
        animate().alpha(0f).setDuration(duration.toLong()).onAnimationEnd {
            isClickable = true; hide(); alpha = originalAlpha
            onDone?.invoke()
        }
    }
}

fun <T : View> T.fadeVisible(fadeVisible: Boolean) = if (fadeVisible) fadeIn() else fadeOut()

fun <T : View> T.fadeGone(fadeGone: Boolean) = if (fadeGone) fadeOut() else fadeIn()

var <T : View> T.isFadeVisible
    get() = visibility == VISIBLE
    set(value) {
        fadeVisible(value)
    }

