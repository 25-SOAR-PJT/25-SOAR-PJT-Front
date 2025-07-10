package com.example.soar.EntryPage.Splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.soar.MainActivity
import com.example.soar.R

class SplashPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_page)

        val motion = findViewById<MotionLayout>(R.id.motionSplash)

        // 애니메이션 시작
        motion.post { motion.transitionToEnd() }

        // 애니메이션 완료 시 0.2초 후 MainActivity로 이동
        motion.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionCompleted(layout: MotionLayout, currentId: Int) {
                if (currentId == layout.endState) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this@SplashPageActivity, MainActivity::class.java))
                        finish()
                    }, 200)
                }
            }

            override fun onTransitionStarted(layout: MotionLayout?, startId: Int, endId: Int) {}
            override fun onTransitionChange(layout: MotionLayout?, startId: Int, endId: Int, progress: Float) {}
            override fun onTransitionTrigger(layout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {}
        })
    }
}