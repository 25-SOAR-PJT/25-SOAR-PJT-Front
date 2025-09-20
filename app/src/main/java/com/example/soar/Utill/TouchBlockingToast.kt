package com.example.soar.util

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.soar.databinding.CustomToastBinding
import java.lang.ref.WeakReference
import android.os.Handler
import android.os.Looper
import android.widget.Adapter
// 상단 import 추가
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
/**
 * 커스텀 토스트를 띄우는 동안 전화면 터치 차단 뷰를 동적으로 추가/제거하는 유틸.
 * XML 수정 없이 모든 화면에서 재사용 가능.
 */
object TouchBlockingToast {

    // (실제 Toast는 사용하지 않지만, 호환성 위해 남김)
    private var activeToast: Toast? = null
    private var toastViewRef: WeakReference<View>? = null   // 커스텀 토스트 컨테이너 참조
    private var blockerRef: WeakReference<View>? = null     // 터치 차단 뷰 참조
    private val handler = Handler(Looper.getMainLooper())
    private var pendingHide: Runnable? = null

    fun show(
        activity: Activity,
        message: String,
        long: Boolean = false,
        gravity: Int? = null,            // 기본값: TOP|CENTER_HORIZONTAL
        yOffset: Int = 0,                // 상단 마진(px)
        cancelText: String? = "취소",
        hideCancel: Boolean = false,
        onCancel: (() -> Unit)? = null,
    ) {
        clear(activity)

        val inflater = LayoutInflater.from(activity)
        val toastBinding = CustomToastBinding.inflate(inflater).apply {
            textMessage.text = message
            if (hideCancel) {
                btnCancel.visibility = View.GONE
                textMessage.textAlignment = View.TEXT_ALIGNMENT_CENTER
            } else {
                btnCancel.visibility = View.VISIBLE
                btnCancel.text = cancelText ?: ""
            }
            btnCancel.setOnClickListener {
                onCancel?.invoke()
                clear(activity)
            }
        }

        // 전화면 터치 차단 뷰 부착
        val blocker = createTouchBlocker(activity)
        attachBlocker(activity, blocker)

        // 커스텀 "토스트" 뷰를 decorView 위에 부착 (클릭 가능)
        attachToastView(activity, toastBinding.root, gravity, yOffset)

        // 자동 해제 타이머
        val delayMs = if (long) 4500L else 2500L
        pendingHide = Runnable { clear(activity) }.also { handler.postDelayed(it, delayMs) }
    }

    fun clear(activity: Activity) {
        activeToast?.cancel()
        activeToast = null

        pendingHide?.let { handler.removeCallbacks(it) }
        pendingHide = null

        detachToastView(activity)
        detachBlocker(activity)
    }

    // ---------- 내부 헬퍼 (모두 object 내부!) ----------

    // TouchBlockingToast 내부의 attachToastView를 아래로 교체
    private fun attachToastView(activity: Activity, content: View, gravity: Int?, yOffset: Int) {
        val root = activity.window?.decorView as? ViewGroup ?: return

        val overlay = FrameLayout(activity).apply {
            isClickable = true
            isFocusable = true
        }

        val g = gravity ?: (Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            this.gravity = g

            // 시스템 바 인셋(제스처/내비게이션 바) 반영
            val insets = ViewCompat.getRootWindowInsets(root)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())
            val insetTop = insets?.top ?: 0
            val insetBottom = insets?.bottom ?: 0

            if ((g and Gravity.BOTTOM) == Gravity.BOTTOM) {
                bottomMargin = insetBottom + yOffset
            } else {
                //topMargin = insetTop + yOffset
                bottomMargin = insetBottom + yOffset
            }
        }

        overlay.addView(content, params)
        root.addView(
            overlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // 살짝 토스트같은 등장 애니메이션(선택)
        overlay.alpha = 0f
        overlay.translationY = 24f * activity.resources.displayMetrics.density
        overlay.animate().alpha(1f).translationY(0f).setDuration(160).start()

        toastViewRef = WeakReference(overlay)
    }


    private fun detachToastView(activity: Activity) {
        val root = activity.window?.decorView as? ViewGroup ?: return
        toastViewRef?.get()?.let { root.removeView(it) }
        toastViewRef = null
    }

    private fun createTouchBlocker(activity: Activity): View =
        View(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // 완전 투명하지만 터치를 소비
            isClickable = true
            isFocusable = true
        }

    private fun attachBlocker(activity: Activity, blocker: View) {
        val root = activity.window?.decorView as? ViewGroup ?: return
        // 중복 추가 방지
        detachBlocker(activity)
        root.addView(
            blocker,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        blockerRef = WeakReference(blocker)
    }

    private fun detachBlocker(activity: Activity) {
        val root = activity.window?.decorView as? ViewGroup ?: return
        blockerRef?.get()?.let { root.removeView(it) }
        blockerRef = null
    }
}

// ----------------- 확장 함수 -----------------

fun Activity.showBlockingToast(message: String, long: Boolean = false, hideCancel: Boolean) {
    TouchBlockingToast.show(this, message, long, hideCancel = hideCancel)
}

fun Activity.showBlockingToast(message: String, long: Boolean = false) {
    TouchBlockingToast.show(this, message, long)
}

fun Fragment.showBlockingToast(message: String, long: Boolean = false) {
    activity?.let { TouchBlockingToast.show(it, message, long) }
}

// 새 오버로드: onCancel 콜백 지원
fun Activity.showBlockingToast(
    message: String,
    long: Boolean = false,
    hideCancel: Boolean,
    onCancel: (() -> Unit)? = null
) {
    TouchBlockingToast.show(this, message, long, hideCancel = hideCancel, onCancel = onCancel)
}

fun Fragment.showBlockingToast(
    message: String,
    long: Boolean = false,
    hideCancel: Boolean = false,
    cancelText: String? = "취소",
    onCancel: (() -> Unit)? = null
) {
    activity?.let {
        TouchBlockingToast.show(
            it,
            message,
            long,
            cancelText = cancelText,
            hideCancel = hideCancel,
            onCancel = onCancel
        )
    }
}
