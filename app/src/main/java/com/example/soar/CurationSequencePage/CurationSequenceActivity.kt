package com.example.soar.CurationSequencePage

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.fragment.NavHostFragment
import com.example.soar.R
import com.example.soar.databinding.ActivityCsPageBinding
import com.example.soar.MainActivity
import com.example.soar.Network.TokenManager

data class TagUiModel(
    val tagId: Int,
    val tagName: String,
    val fieldId: Int,
    val isSelected: Boolean
)

/**
 * LiveData와 함께 사용하여 Toast, SnackBar, Navigation 등 일회성 이벤트를 처리하기 위한 Wrapper 클래스.
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // 외부에서는 값을 변경할 수 없도록 private set으로 설정

    /**
     * 아직 처리되지 않았을 경우에만 content를 반환하고, 처리되었음을 표시합니다.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 처리 여부와 관계없이 content를 반환합니다.
     */
    fun peekContent(): T = content
}

class CurationSequenceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCsPageBinding

    private val viewModel: CurationSequenceViewModel by viewModels()

    private val steps = listOf(
        R.id.step0Fragment,
        R.id.step1Fragment,
        R.id.step2Fragment,
        R.id.step3Fragment,
        R.id.step4Fragment,
        R.id.step5Fragment
    )

    val getUserInfo = TokenManager.getUserInfo()
    val signInInfo = TokenManager.getSignInInfo()
    val userName = getUserInfo?.userName ?:signInInfo?.userName ?: "사용자"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.color.ref_white)
        binding = ActivityCsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel.setUserName(userName)

        // [추가] ViewModel을 통해 태그 데이터를 한 번만 로드합니다.
        // 액티비티 Context 대신 applicationContext를 넘겨주는 것이 더 안전합니다.
        viewModel.loadAllTags()

        // ── NavController ─────────────────────
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController

        binding.appbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.appbar.textTitle.text = " "

    }


    // `Step8Fragment`에서 호출할 새로운 함수
    fun navigateToMainAndOpenPersonalBiz() {
        val intent = Intent(this, MainActivity::class.java).apply {
            // 기존 스택 정리 후 MainActivity만 남김
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("start_destination", "explore")     // 시작 프래그먼트 지시
            putExtra("open_personal_biz", true)          // PersonalBizActivity 띄우기 지시
        }
        startActivity(intent)
        finish()
    }

}

/* ---------- LiveData 확장 : 여러 값을 combine ---------- */
fun <T> List<LiveData<T>>.combineLatest(block: (List<T>) -> T): LiveData<T> =
    MediatorLiveData<T>().also { m ->
        val data = MutableList(size) { null as T? }
        forEachIndexed { i, src ->
            m.addSource(src) { v ->
                data[i] = v; if (data.all { it != null }) m.value = block(data.filterNotNull())
            }
        }
    }