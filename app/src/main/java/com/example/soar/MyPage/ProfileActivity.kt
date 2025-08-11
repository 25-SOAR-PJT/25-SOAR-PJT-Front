package com.example.soar.MyPage


import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.soar.DetailPage.ReviewDetailActivity
import com.example.soar.R
import com.example.soar.databinding.ActivityPolicyBinding
import com.example.soar.databinding.ActivityProfileBinding
import com.example.soar.databinding.StepCsSummaryBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱 바
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.profile)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // TODO: 큐레이션 6번 처럼 정보 받아와서 각 부분에 view(사용자 정보) 만들어서 넣어야 함
        // 각 view 부분 누르면 큐레이션의 해당 부분 가져와서 view 제목 같은거 몇개 안보이게 하고 수정하는 페이지로 대신 사용

        binding.btnChangePw.setOnClickListener {
            val intent = Intent(this, ChangePwActivity::class.java)
            startActivity(intent)
        }
    }
}