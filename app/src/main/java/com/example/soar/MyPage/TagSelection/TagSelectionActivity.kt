package com.example.soar.MyPage.TagSelection

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavOptions
import com.example.soar.R
import com.example.soar.databinding.ActivityTagPageBinding
import com.example.soar.Network.TokenManager
import com.example.soar.Network.tag.TagResponse

data class TagUiModel(
    val tagId: Int,
    val tagName: String,
    val fieldId: Int,
    val isSelected: Boolean
)

open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}

class TagSelectionActivity : AppCompatActivity() {

    // [추가] Intent를 위한 상수 선언
    companion object {
        const val EXTRA_EDIT_MODE = "EXTRA_EDIT_MODE"
        const val EXTRA_STARTING_FIELD_ID = "EXTRA_STARTING_FIELD_ID"
        const val EXTRA_USER_TAGS = "EXTRA_USER_TAGS"

        // 필드 ID와 시작 Fragment ID 매핑을 위한 상수
        const val STEP1_FIELD_ID = 9
        const val STEP2_FIELD_ID = 5
        const val STEP3_FIELD_ID = 8
        const val STEP4_FIELD_ID = 7
        const val STEP5_FIELD_ID = 1 // 1,2,3,4는 모두 step5로 이동
    }

    private lateinit var binding: ActivityTagPageBinding

    private val viewModel: TagSelectionViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.color.ref_white)
        binding = ActivityTagPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("TagSelectionActivity","TagSelectionActivity 진입")

        val signInInfo = TokenManager.getSignInInfo()



        // ViewModel을 통해 태그 데이터를 한 번만 로드
        viewModel.loadAllTags()

        // ── NavController ─────────────────────
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController

        // [추가] 프로필 화면에서 진입한 경우 데이터 처리
        val isEditMode = intent.getBooleanExtra(EXTRA_EDIT_MODE, false)
        val startingFieldId = intent.getIntExtra(EXTRA_STARTING_FIELD_ID, -1)

        // ✨ [수정] getParcelableArrayExtra에 제네릭 타입을 지정하지 않고,
        // as Array<Parcelable>로 변환 후 toList()를 호출합니다.
        val userTags = intent.getParcelableArrayExtra(EXTRA_USER_TAGS)
            ?.filterIsInstance<TagResponse>()
            ?.toTypedArray()

        // ✨ [수정] 수신된 userTags를 로그로 출력하여 확인
        if (userTags != null) {
            Log.d("TagSelectionActivity", "수신된 태그 데이터: ${userTags.contentToString()}")
        } else {
            Log.d("TagSelectionActivity", "수신된 태그 데이터가 없습니다.")
        }

        if (isEditMode) {
            viewModel.setEditMode(true)
            userTags?.let { viewModel.populateFromUserTags(it.toList()) }

            val startFragmentId = when (startingFieldId) {
                STEP1_FIELD_ID -> R.id.step1Fragment
                STEP2_FIELD_ID -> R.id.step2Fragment
                STEP3_FIELD_ID -> R.id.step3Fragment
                STEP4_FIELD_ID -> R.id.step4Fragment
                STEP5_FIELD_ID -> R.id.step5Fragment
                else -> R.id.step5Fragment
            }

            // ✨ [수정] NavGraph의 시작점을 동적으로 설정하여 불필요한 화면 로드를 방지합니다.
            val navGraph = navController.navInflater.inflate(R.navigation.nav_tag)
            navGraph.setStartDestination(startFragmentId)
            navController.graph = navGraph

        }

        binding.appbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.appbar.textTitle.text = " "
    }
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}