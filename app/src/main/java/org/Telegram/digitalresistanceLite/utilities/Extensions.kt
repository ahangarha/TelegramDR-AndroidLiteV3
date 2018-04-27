package org.Telegram.digitalresistanceLite

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View

fun View.fadeIn(duration: Long = 250L, f:() -> Unit = {}) {
  ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).apply {
    setDuration(duration)
    onStart { f() }
    start()
  }
}

fun View.fadeOut(duration: Long = 250L, f:() -> Unit = {}) {
  ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
    setDuration(duration)
    onEnd { f() }
    start()
  }
}

inline fun ObjectAnimator.onStart(crossinline func: () -> Unit) {
  addListener(object : Animator.AnimatorListener {
    override fun onAnimationRepeat(animation: Animator?) {}
    override fun onAnimationEnd(animation: Animator?) {}
    override fun onAnimationCancel(animation: Animator?) {}
    override fun onAnimationStart(animation: Animator?) { func() }
  })
}

inline fun ObjectAnimator.onEnd(crossinline func: () -> Unit) {
  addListener(object : Animator.AnimatorListener {
    override fun onAnimationRepeat(animation: Animator?) {}
    override fun onAnimationEnd(animation: Animator?) { func() }
    override fun onAnimationCancel(animation: Animator?) {}
    override fun onAnimationStart(animation: Animator?) {}
  })
}
