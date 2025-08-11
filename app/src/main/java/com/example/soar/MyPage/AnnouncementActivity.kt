package com.example.soar.MyPage

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.soar.R
import com.example.soar.databinding.ActivityAnnouncementBinding
import com.example.soar.databinding.ActivityPolicyBinding

class AnnouncementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnnouncementBinding
    //private lateinit var announcementList: List<AnnouncementResponseDto>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱 바
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.announcement)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //fetchAnnouncement()
    }

    // 주석 처리 해둔 것들은 나중에 dto 연결 후에 수정 해서 쓰면 됨
//    private fun fetchAnnouncement() {
//        apiService.getAllAnnouncement().enqueue(object :
//            Callback<ApiResponse<List<AnnouncementResponseDto>>> {
//
//            override fun onResponse(
//                call: Call<ApiResponse<List<AnnouncementResponseDto>>>,
//                response: Response<ApiResponse<List<AnnouncementResponseDto>>>
//            ) {
//                if (response.isSuccessful && response.body()?.status == "success") {
//                    announcementList = response.body()?.data ?: emptyList()
//                    Log.d("AnnouncementActivity", "응답 파싱 성공: $announcementList")
//
//                    // recyclerView 연결
//                    binding.announcementContainer.layoutManager = LinearLayoutManager(this@AnnouncementActivity)
//                    binding.announcementContainer.adapter = AnnouncementAdapter(announcementList)
//                } else {
//                    Toast.makeText(this@AnnouncementActivity, "공지사항 불러오기 실패", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(
//                call: Call<ApiResponse<List<AnnouncementResponseDto>>>,
//                t: Throwable
//            ) {
//                Log.e("AnnouncementActivity", "API 실패", t)
//                Toast.makeText(this@AnnouncementActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
}