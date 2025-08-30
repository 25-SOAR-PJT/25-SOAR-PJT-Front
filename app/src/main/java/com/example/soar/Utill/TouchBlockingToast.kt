package com.example.soar.util

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.soar.databinding.CustomToastBinding
import java.lang.ref.WeakReference

/**
 * 커스텀 토스트를 띄우는 동안 전화면 터치 차단 뷰를 동적으로 추가/제거하는 유틸.
 * XML 수정 없이 모든 화면에서 재사용 가능.
 */
object TouchBlockingToast {

    private var activeToast: Toast? = null
    private var blockerRef: WeakReference<View>? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pendingHide: Runnable? = null

    /**
     * @param message 토스트 메시지
     * @param long true면 LENGTH_LONG(약 3.5s), false면 LENGTH_SHORT(약 2s)
     * @param gravity 토스트 위치(기본: TOP|CENTER_HORIZONTAL)
     * @param yOffset Y 오프셋(px). dp 변환해서 넘겨도 됨.
     */
    fun show(
        activity: Activity,
        message: String,
        long: Boolean = false,
        gravity: Int? = null,          // <-- Int? 로 변경
        yOffset: Int = 0,
        cancelText: String? = "취소",
        hideCancel: Boolean = false
    ) {
        // 기존 토스트/차단뷰 정리
        clear(activity)

        val inflater = LayoutInflater.from(activity)
        val toastBinding = CustomToastBinding.inflate(inflater)

        toastBinding.textMessage.text = message

        // 취소 버튼 관련 설정
        if (hideCancel) {
            toastBinding.btnCancel.visibility = View.GONE
            // 메시지 가운데 정렬
            toastBinding.textMessage.textAlignment = View.TEXT_ALIGNMENT_CENTER
        } else {
            toastBinding.btnCancel.visibility = View.VISIBLE
            toastBinding.btnCancel.text = cancelText ?: ""
        }

        val toast = Toast(activity).apply {
            duration = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            view = toastBinding.root
            gravity?.let { setGravity(it, 0, yOffset) }
        }


        // 취소 버튼 클릭 시 → 토스트 닫고 차단 해제
        toastBinding.btnCancel.setOnClickListener {
            toast.cancel()
            clear(activity)
        }

        // 전화면 터치 차단뷰 동적 추가
        val blocker = createTouchBlocker(activity)
        attachBlocker(activity, blocker)

        // 토스트 표시
        activeToast = toast
        toast.show()

        // 토스트 길이에 맞춰 차단 해제 예약
        val delayMs = if (long) 4500L else 2500L
        pendingHide = Runnable {
            clear(activity)
        }.also { handler.postDelayed(it, delayMs) }
    }

    /** 현재 표시 중인 토스트/차단뷰 정리 */
    fun clear(activity: Activity) {
        activeToast?.cancel()
        activeToast = null

        pendingHide?.let { handler.removeCallbacks(it) }
        pendingHide = null

        detachBlocker(activity)
    }

    // -------- 내부 헬퍼 --------

    private fun createTouchBlocker(activity: Activity): View {
        return View(activity).apply {
            // 전체 화면 크기
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // 투명해도 클릭/포커스 가능해야 터치를 소비함
            isClickable = true
            isFocusable = true
            // 필요하면 반투명 색을 주어 안내 가능
            // setBackgroundColor(Color.parseColor("#00000000"))
        }
    }

    private fun attachBlocker(activity: Activity, blocker: View) {
        val root = activity.window?.decorView as? ViewGroup ?: return
        // 중복 추가 방지
        detachBlocker(activity)
        root.addView(blocker)
        blockerRef = WeakReference(blocker)
    }

    private fun detachBlocker(activity: Activity) {
        val root = activity.window?.decorView as? ViewGroup ?: return
        blockerRef?.get()?.let { v ->
            root.removeView(v)
        }
        blockerRef = null
    }
}

fun Activity.showBlockingToast(message: String, long: Boolean = false, hideCancel: Boolean) {
    TouchBlockingToast.show(this, message, long, hideCancel = hideCancel)
}

fun Activity.showBlockingToast(message: String, long: Boolean = false) {
    TouchBlockingToast.show(this, message, long)
}

fun Fragment.showBlockingToast(message: String, long: Boolean = false) {
    activity?.let { TouchBlockingToast.show(it, message, long) }
}