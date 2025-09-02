package com.example.soar.MyPage.Unsubscribe

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.soar.R
import com.example.soar.databinding.ActivityUnsubscribe2Binding

class Unsubscribe2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityUnsubscribe2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnsubscribe2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<TextView>(R.id.text_title).text = getString(R.string.unsubscribe)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnUnsubscribe.setOnClickListener {
            val intent = Intent(this, Unsubscribe2Activity::class.java)
            startActivity(intent)
        }

    }
}