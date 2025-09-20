package com.example.soar.MyPage

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.EmailResultItem
import com.example.soar.EntryPage.SignIn.LoginActivity
import com.example.soar.R
import com.example.soar.databinding.ActivityFindResultBinding

class FindResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFindResultBinding
    // EmailResultAdapter는 lateinit으로 선언만 되어 있음
    private lateinit var adapter: EmailResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱바 설정
        findViewById<TextView>(R.id.text_title).text = ""
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // --- 해결 코드 ---
        // 1. 어댑터 인스턴스 생성 및 초기화
        adapter = EmailResultAdapter()

        // 2. RecyclerView에 어댑터와 레이아웃 매니저 설정
        binding.emailResult.layoutManager = LinearLayoutManager(this)
        binding.emailResult.adapter = adapter
        // ---------------

        // Intent로부터 데이터 받아오기
        val name = intent.getStringExtra("USER_NAME")
        val emails = intent.getStringArrayListExtra("EMAIL_LIST")

        // UI에 데이터 설정
        if (!name.isNullOrEmpty()) {
            binding.resultName.text = "${name}${getString(R.string.result1)}"
        }

        // 받아온 이메일 목록을 어댑터에 전달 (더미 데이터 대신 사용)
        if (!emails.isNullOrEmpty()) {
            adapter.submitList(emails.map { EmailResultItem(it) })
        }

        // 로그인 버튼 클릭 리스너
        binding.btnToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                // 이전 화면 기록을 모두 지워 로그인 후 뒤로 가기 시 앱이 종료되도록 함
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}