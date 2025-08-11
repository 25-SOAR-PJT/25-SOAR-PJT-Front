
package com.example.soar.MyPage

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.soar.R
import com.example.soar.databinding.ActivityDetailPageBinding
import com.example.soar.databinding.ActivityPolicyBinding

data class PolicySet(
    val title: View,
    val content: View,
    val arrow: ImageView,
    val line: View
)

class PolicyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPolicyBinding
    private lateinit var policySets : List<PolicySet>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱 바
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.policy)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        policySets = listOf(
            PolicySet(
                binding.policy1Title,
                binding.policy1DetailContainer,
                binding.policy1Arrow,
                binding.line1
            ),
            PolicySet(
                binding.policy2Title,
                binding.policy2DetailContainer,
                binding.policy2Arrow,
                binding.line2
            ),
            PolicySet(
                binding.policy3Title,
                binding.policy3DetailContainer,
                binding.policy3Arrow,
                binding.line3
            )
        )

        setuppolicyListeners()
    }

    // 클릭 시 약관 펼치기/접기
    private fun setuppolicyListeners() {
        policySets.forEach { policy ->
            policy.title.setOnClickListener {
                if (policy.content.visibility == View.VISIBLE) {
                    policy.content.visibility = View.GONE
                    policy.arrow.rotation = 270F
                    policy.line.visibility = View.VISIBLE
                } else {
                    policy.content.visibility = View.VISIBLE
                    policy.arrow.rotation = 90F
                    policy.line.visibility = View.GONE
                }
            }
        }
    }
}