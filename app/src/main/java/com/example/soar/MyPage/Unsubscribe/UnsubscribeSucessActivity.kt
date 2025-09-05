package com.example.soar.MyPage.Unsubscribe

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.soar.MainActivity
import com.example.soar.R
import com.example.soar.databinding.ActivityUnsubscribeSucessBinding

class UnsubscribeSucessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnsubscribeSucessBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnsubscribeSucessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<TextView>(R.id.text_title).text = ""
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnToStart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}