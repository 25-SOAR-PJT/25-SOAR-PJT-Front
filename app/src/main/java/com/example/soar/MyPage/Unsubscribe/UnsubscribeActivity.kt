package com.example.soar.MyPage.Unsubscribe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.soar.databinding.ActivityUnsubscribeBinding

class UnsubscribeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnsubscribeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnsubscribeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNo.setOnClickListener{
            finish()
        }

        binding.btnYes.setOnClickListener {
            val intent = Intent(this, Unsubscribe2Activity::class.java)
            startActivity(intent)
        }


    }
}