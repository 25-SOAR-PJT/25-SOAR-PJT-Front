package com.example.soar.ExplorePage

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.example.soar.R
import com.example.soar.Utill.SwipeToDismissUtil
import com.example.soar.databinding.ActivityPersonalBizBinding

class PersonalBizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalBizBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalBizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SwipeToDismissUtil(this)

        // Activity 크기 화면의 80%로 설정
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 1.0).toInt()
        val height = (displayMetrics.heightPixels * 0.8).toInt() // 세로 80%
        window.setLayout(width, height)

        // 화면 아래쪽에 붙이기
        window.setGravity(Gravity.BOTTOM)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        // 액티비티가 종료될 때 아래로 내려가는 애니메이션 적용
        overridePendingTransition(0, R.anim.slide_out_down)
    }
}