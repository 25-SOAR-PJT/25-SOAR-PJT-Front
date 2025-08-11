package com.example.soar.MyPage

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.soar.R
import com.example.soar.databinding.ActivityChangePwBinding
import com.example.soar.databinding.ActivityFindBinding

class FindActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: 아이디(이메일) / 이름 / 주민 번호  부분 에러 핸들링 부탁드림 (회원 가입이랑 동일)
        // TODO : 제대로 다 입력 받으면 버튼 활성화 + FindResultActivity로 넘어가기
        // TODO : 비번 찾기는 버튼 클릭 시 토스트 메세지 띄우기.

        // 앱바
        findViewById<TextView>(R.id.text_title).text = ""
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 아이디/ 비번 찾기 부분 파란선 이동 로직
        binding.root.post {
            val pl = binding.tabBar.paddingLeft
            val pr = binding.tabBar.paddingRight
            val contentW = binding.tabBar.width - pl - pr
            val half = contentW / 2

            binding.line.layoutParams = binding.line.layoutParams.apply { width = half }
            binding.line.x = pl.toFloat()            // 왼쪽 절반의 시작점
            binding.line.requestLayout()

            selectTab(binding.toId, binding.toPw)
        }

        // 아이디 찾기, 비번 찾기 둘다 한 화면에 있고 visibility만 조정.
        binding.toId.setOnClickListener {
            selectTab(binding.toId, binding.toPw)
            binding.layoutFindID.visibility = View.VISIBLE
            binding.layoutFindPW.visibility = View.GONE
        }
        binding.toPw.setOnClickListener {
            selectTab(binding.toPw, binding.toId)
            binding.layoutFindID.visibility = View.GONE
            binding.layoutFindPW.visibility = View.VISIBLE
        }


    }


    private fun moveline(target: TextView) {
        val line = binding.line
        val container = binding.tabBar

        val pl = container.paddingLeft
        val pr = container.paddingRight
        val contentW = container.width - pl - pr
        val half = contentW / 2

        // 왼쪽 탭이면 pl, 오른쪽 탭이면 pl + half 위치로
        val targetStartX = (pl + if (target.id == R.id.to_id) 0 else half).toFloat()

        val widthAnim = ValueAnimator.ofInt(line.width, half).apply {
            duration = 220
            addUpdateListener {
                line.layoutParams = line.layoutParams.apply { width = it.animatedValue as Int }
                line.requestLayout()
            }
        }
        val xAnim = ObjectAnimator.ofFloat(line, View.X, line.x, targetStartX).apply { duration = 220 }

        AnimatorSet().apply { playTogether(widthAnim, xAnim); start() }
    }

    private fun selectTab(selected: TextView, other: TextView) {
        selected.setTextColor(ContextCompat.getColor(this, R.color.ref_blue_500))
        other.setTextColor(ContextCompat.getColor(this, R.color.semantic_text_strong))
        moveline(selected)
    }

}