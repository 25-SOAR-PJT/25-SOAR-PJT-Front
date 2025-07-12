package com.example.soar.EntryPage.Splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.soar.MainActivity
import com.example.soar.R
import com.example.soar.Network.TokenManager

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


        TokenManager.init(this) // SharedPreferences 초기화
        validateTokenOnStart()

        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = if (TokenManager.getAccessToken().isNullOrBlank()) {
                Intent(this, MainActivity::class.java) // 로그인 안 된 상태
            } else {
                Intent(this, MainActivity::class.java) // 로그인 유지
            }
            startActivity(nextActivity)
            finish()
        }, 3000) // 3초 후 전환
    }

    private fun validateTokenOnStart() {
        val token = TokenManager.getAccessToken()
        if (!token.isNullOrBlank()) {
            try {
                val parts = token.split(".")
                if (parts.size != 3) {
                    TokenManager.clearTokens()
                    return
                }

                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
                val regex = Regex("\"exp\":(\\d+)")
                val matchResult = regex.find(payload)
                val exp = matchResult?.groupValues?.get(1)?.toLong() ?: 0L

                val currentTime = System.currentTimeMillis() / 1000 // 현재 시간 (초 단위)

                if (exp < currentTime) {
                    // 🔥 만료됨: 토큰 제거
                    TokenManager.clearTokens()
                }

            } catch (e: Exception) {
                // 🔥 토큰 파싱 오류 시 토큰 제거
                TokenManager.clearTokens()
            }
        }
    }
}

