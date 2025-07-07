package com.example.soar.EntryPage.Splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.soar.MainActivity
import com.example.soar.R

class SplashPageActivity : AppCompatActivity() {

    private var isAnimationDone = false            // 애니메이션 완료 여부

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_page)

        val motion = findViewById<MotionLayout>(R.id.motionSplash)

        // 1) 레이아웃이 그려진 직후 애니메이션 시작
        motion.post { motion.transitionToEnd() }

        // 2) 애니메이션 완료 신호만 받음 (자동 전환 X)
        motion.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionCompleted(layout: MotionLayout, currentId: Int) {
                if (currentId == layout.endState) {
                    isAnimationDone = true
                    // 원하면 여기서 "Tap to continue" 같은 Toast / 안내 문구 표시
                }
            }
            override fun onTransitionStarted(l: MotionLayout?, s: Int, e: Int) {}
            override fun onTransitionChange(l: MotionLayout?, s: Int, e: Int, p: Float) {}
            override fun onTransitionTrigger(l: MotionLayout?, id: Int, pos: Boolean, p: Float) {}
        })

        // 3) 화면 어디든 클릭하면 → 애니 끝났을 때만 다음 화면
        findViewById<View>(R.id.motionSplash).setOnClickListener {
            if (isAnimationDone) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                // 애니메이션이 끝나기 전에 탭하면 즉시 끝까지 진행(스킵)하도록
                motion.transitionToEnd()
            }
        }
    }
}