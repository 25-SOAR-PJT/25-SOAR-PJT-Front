package com.example.soar.Utill

import android.app.Activity
import android.view.MotionEvent
import android.view.View
import com.example.soar.R

class SwipeToDismissUtil(activity: Activity) {

    private var startY = 0f
    private var isDragging = false
    private val SWIPE_THRESHOLD = 200f  // Activity 닫히는 최소 스와이프 거리

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
                    if (diffY > 0) { // 아래로 드래그 중
                        rootView.translationY = diffY
                        isDragging = true
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val diffY = event.rawY - startY

                    // 드래그 중이 아니면 클릭으로 간주 -> performClick 호출
                    if (!isDragging) {
                        v.performClick()
                    }

                    if (isDragging && diffY > SWIPE_THRESHOLD) {
                        // 스와이프 충분하면 Activity 종료
                        activity.finish()
                        activity.overridePendingTransition(0, R.anim.slide_out_down)
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



