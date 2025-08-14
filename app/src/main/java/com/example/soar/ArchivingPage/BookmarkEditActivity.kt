package com.example.soar.ArchivingPage

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.soar.R
import com.example.soar.databinding.ActivityBookmarkEditBinding
import com.example.soar.databinding.ActivityDetailPageBinding

class BookmarkEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookmarkEditBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱 바
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.edit2)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        // TODO: BookMarkEditAdapter 안의 내용 수정해서 쓰면 됨 .
        // TODO: 아답터 연결 후에 체크박스 클릭시 btn1, btn2 색도 변경 하는 로직 넣기
    }

}