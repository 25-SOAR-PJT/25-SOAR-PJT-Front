package com.example.soar.MyPage

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.DetailPage.ReviewDetailActivity
import com.example.soar.EmailResultItem
import com.example.soar.EntryPage.SignIn.LoginActivity
import com.example.soar.R
import com.example.soar.databinding.ActivityFindBinding
import com.example.soar.databinding.ActivityFindResultBinding

class FindResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFindResultBinding
    private lateinit var adapter : EmailResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱바
        findViewById<TextView>(R.id.text_title).text = ""
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.emailResult.layoutManager = LinearLayoutManager(this)
        binding.emailResult.adapter = adapter

        // 더미 데이터
        adapter.submitList(
            listOf(
                EmailResultItem("seodori020@example.com"),
                EmailResultItem("user1@example.com")
            )
        )

        // dto 받아온 후
        //binding.resultName.text = "${name}${getString(R.string.result1)}"

        // 버튼 연결
        binding.btnToLogin.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
}