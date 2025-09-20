package com.example.soar.Utill // 패키지 경로는 실제 프로젝트에 맞게 조정하세요.

import android.graphics.drawable.Animatable
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.soar.R

/**
 * 스피너(로딩 애니메이션)를 보여주고, 콘텐츠 뷰들을 숨깁니다.
 * @param spinnerImageView 애니메이션을 표시할 ImageView
 * @param contentToHide 숨길 뷰들 (RecyclerView, TextView 등 가변 인자)
 */
fun Fragment.showSpinner(spinnerImageView: ImageView, vararg contentToHide: View) {
    // 숨길 뷰들을 모두 GONE 처리
    contentToHide.forEach { it.visibility = View.GONE }

    // 스피너를 보여주고 애니메이션 시작
    spinnerImageView.visibility = View.VISIBLE
    spinnerImageView.setImageResource(R.drawable.spinner_loop_keyframes) // 애니메이션 drawable 설정
    (spinnerImageView.drawable as? Animatable)?.start()
}

/**
 * 스피너(로딩 애니메이션)를 숨기고, 콘텐츠 뷰들을 보여줍니다.
 * @param spinnerImageView 애니메이션을 표시했던 ImageView
 * @param contentToShow 보여줄 뷰들 (RecyclerView, TextView 등 가변 인자)
 */
fun Fragment.hideSpinner(spinnerImageView: ImageView, vararg contentToShow: View) {
    // 애니메이션을 멈추고 스피너를 숨김
    (spinnerImageView.drawable as? Animatable)?.stop()
    spinnerImageView.visibility = View.GONE

    // 보여줄 뷰들을 모두 VISIBLE 처리
    contentToShow.forEach { it.visibility = View.VISIBLE }
}

