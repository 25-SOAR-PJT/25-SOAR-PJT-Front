package com.example.soar.Utill

import android.app.Activity
import android.view.MotionEvent
import android.view.View
import com.example.soar.R

class SwipeToDismissUtil(
    private val activity: Activity,
    private val onDismiss: () -> Unit // 액티비티 종료 시 실행할 람다 추가
) {

    private var startY = 0f
    private var isDragging = false
    private val SWIPE_THRESHOLD = 200f

    init {
        val rootView = activity.findViewById<View>(android.R.id.content)

        rootView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    isDragging = false
                }

                MotionEvent.ACTION_MOVE -> {
                    val diffY = event.rawY - startY
                    if (diffY > 0) {
                        rootView.translationY = diffY
                        isDragging = true
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val diffY = event.rawY - startY

                    if (!isDragging) {
                        v.performClick()
                    }

                    if (isDragging && diffY > SWIPE_THRESHOLD) {
                        // 스와이프가 충분하면 onDismiss 람다 호출
                        onDismiss.invoke()
                    } else {
                        // 원래 위치로 복귀
                        rootView.animate().translationY(0f).setDuration(200).start()
                    }
                }
            }
            true
        }
    }
}