package com.example.soar.AlarmPage

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.soar.DetailPage.ReviewDetailActivity
import com.example.soar.R
import com.example.soar.databinding.ActivityAlarmBinding

class AlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱 바
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.alarm_title)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSetting.setOnClickListener {
            val intent = Intent(this, AlarmSettingActivity::class.java)
            startActivity(intent)
        }

        binding.dropdown.setOnClickListener {
            val bottomSheetFragment = AlarmBottomSheetFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        // TODO: 알람 있을때, 없을 때 나눠서 container visibility 조정하면 됨
        // TODO: recyclerview (item_alarm) 만들어 뒀고 아답터 만들어야 함
    }
}