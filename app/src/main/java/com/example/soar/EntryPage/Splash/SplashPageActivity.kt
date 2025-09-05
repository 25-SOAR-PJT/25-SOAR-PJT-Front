// com/example/soar/EntryPage/Splash/SplashPageActivity.kt

package com.example.soar.EntryPage.Splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.soar.EntryPage.Onboarding.OnBoardingActivity
import com.example.soar.MainActivity
import com.example.soar.R
import com.example.soar.Network.TokenManager

class SplashPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_page)

        TokenManager.init(this)
        validateTokenOnStart()

        val motion = findViewById<MotionLayout>(R.id.motionSplash)

        motion.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionCompleted(layout: MotionLayout, currentId: Int) {
                if (currentId == layout.endState) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        // ✅ 온보딩 완료 여부를 체크할 SharedPreferences를 가져옵니다.
                        val onboardingPrefs = getSharedPreferences("pref_onboarding", MODE_PRIVATE)

                        // ✅ 'completed' 키 값을 읽어옵니다. 처음 실행 시에는 false가 됩니다.
                        val isOnboardingCompleted = onboardingPrefs.getBoolean("completed", false)

                        // ✅ 조건에 따라 다음 화면(메인 or 온보딩)으로 보낼 Intent를 결정합니다.
                        val nextIntent = if (isOnboardingCompleted) {
                            // 온보딩을 이미 완료했다면 MainActivity로 이동
                            Intent(this@SplashPageActivity, MainActivity::class.java)
                        } else {
                            // 온보딩을 아직 완료하지 않았다면 OnBoardingActivity로 이동
                            Intent(this@SplashPageActivity, OnBoardingActivity::class.java)
                        }

                        startActivity(nextIntent)
                        finish()
                    }, 200)
                }
            }

            override fun onTransitionStarted(layout: MotionLayout?, startId: Int, endId: Int) {}
            override fun onTransitionChange(layout: MotionLayout?, startId: Int, endId: Int, progress: Float) {}
            override fun onTransitionTrigger(layout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {}
        })

        motion.post { motion.transitionToEnd() }
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

                val currentTime = System.currentTimeMillis() / 1000

                if (exp < currentTime) {
                    TokenManager.clearTokens()
                }

            } catch (e: Exception) {
                TokenManager.clearTokens()
            }
        }
    }
}