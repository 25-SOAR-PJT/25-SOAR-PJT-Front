package com.example.soar.MyPage

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.soar.R
import com.example.soar.databinding.ActivityCommentBinding
import com.example.soar.databinding.ActivityRecordBinding

class RecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱 바
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.comment)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // TODO: recyclerview 만들어둠 (item_record_biz) 아답터 만들어야 함
    }
}