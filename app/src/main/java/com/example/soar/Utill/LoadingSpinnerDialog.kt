package com.example.soar.Utill

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.soar.R

class LoadingSpinnerDialog : DialogFragment() {

    companion object {
        private const val TAG = "LoadingSpinnerDialog"
        fun show(fm: FragmentManager) {
            // 이미 떠 있으면 중복 방지
            if (fm.findFragmentByTag(TAG) == null) {
                LoadingSpinnerDialog().apply { isCancelable = false }
                    .show(fm, TAG)
            }
        }
        fun dismiss(fm: FragmentManager) {
            (fm.findFragmentByTag(TAG) as? com.example.soar.Utill.LoadingSpinnerDialog)?.dismissAllowingStateLoss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_MyApp_MaterialDialog) // ★ 여기만 교체
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.dialog_loading_spinner, container, false)

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setDimAmount(0f) // 배경 딤 완전 제거 (원하면 주석 처리)
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            // BLUR_BEHIND는 기기별 지원 편차가 커서 제거 권장
            clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }
        view?.findViewById<ImageView>(R.id.imgSpinner)?.let { (it.drawable as? Animatable)?.start() }
    }


    override fun onStop() {
        // 애니메이션 정지
        view?.findViewById<ImageView>(R.id.imgSpinner)?.let { iv ->
            (iv.drawable as? Animatable)?.stop()
        }
        super.onStop()
    }
}