package com.example.soar.MyPage

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View // View import 추가
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.soar.MyPage.TagSelection.TagSelectionActivity
import com.example.soar.Network.TokenManager
import com.example.soar.Network.tag.TagResponse
import com.example.soar.R
import com.example.soar.databinding.ActivityProfileBinding
import com.example.soar.util.showBlockingToast
import com.google.android.flexbox.FlexboxLayout
import java.util.Calendar


class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    // ✨ 수정 모드 상태를 관리하는 변수
    private var isEditMode = false
    // ✨ 원래 정보를 저장해 둘 변수
    private var originalName: String? = null
    private var originalBirthDate: String? = null
    private var originalGender: Boolean? = null // ✨ 성별 원본 값 저장 변수 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAppbar()
        setupPasswordChangeButton() // ✨ 함수 이름 변경으로 가독성 향상
        setupBirthdatePicker() // ✨ DatePickerDialog 설정 함수 호출
        setupObservers()
        setupFragmentResultListener() // ✨ 프래그먼트 결과 리스너 설정

        profileViewModel.fetchProfileData()
    }

    override fun onResume() {
        super.onResume()
        if (!isEditMode) {
            profileViewModel.fetchProfileData()
        }
    }

    private fun setupAppbar() {

        binding.appbar.textTitle.text = getString(R.string.profile)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ✨ '수정'/'저장' 버튼 리스너 설정
        binding.appbar.btnToEdit.setOnClickListener {
            toggleEditMode()
        }
    }

    // ✨ 기존 setupUserInfo에서 비밀번호 변경 관련 로직만 남김
    private fun setupPasswordChangeButton() {
        if (TokenManager.isKakaoUser()) {
            binding.btnChangePw.visibility = View.GONE
        } else {
            binding.btnChangePw.visibility = View.VISIBLE
            binding.btnChangePw.setOnClickListener {
                val intent = Intent(this, ChangePwActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    private fun setupObservers() {

        // ✨ 추가: 사용자 상세 정보(이름, 생년월일, 성별) Observer
        profileViewModel.userDetailInfo.observe(this, Observer { userInfo ->
            if (userInfo != null) {
                // 수정 모드가 아닐 때만 UI 업데이트
                if (!isEditMode) {
                    binding.name.setText(userInfo.userName)
                    originalName = userInfo.userName

                    userInfo.userBirthDate?.let { date ->
                        originalBirthDate = date
                        val parts = date.split("-")
                        if (parts.size == 3) {
                            binding.year.setText(parts[0])
                            binding.month.setText(parts[1])
                            binding.day.setText(parts[2])
                        }
                    }
                    originalGender = userInfo.userGender // ✨ 원본 성별 값 저장
                    updateGenderChip(userInfo.userGender)

                    toggleEditModeUi(false)
                }
            }
        })

        profileViewModel.userTags.observe(this, Observer { userTags ->
            if (userTags != null) {
                updateCurationChips(userTags)
            }
        })
        profileViewModel.fetchError.observe(this, Observer { errorMessage ->
            if (errorMessage != null) {
                showBlockingToast("프로필 데이터를 불러오지 못했습니다: $errorMessage", hideCancel = true)
            }
        })

        // ✨ 추가: 정보 업데이트 성공/실패 이벤트 처리
        profileViewModel.updateEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { message ->
                showBlockingToast(message, hideCancel = true)
                // 성공 후, 수정 모드를 풀고 데이터를 다시 불러옴
                // ✨ 추가: 이름이 변경되었다면 TokenManager의 이름도 업데이트
                if (originalName != binding.name.text.toString()) {
                    TokenManager.updateUserName(binding.name.text.toString())
                }

                isEditMode = false
                toggleEditModeUi(false)
                profileViewModel.fetchProfileData()
            }
        })
    }

    // ✨ 추가: 수정 모드를 토글하는 함수
    private fun toggleEditMode() {
        isEditMode = !isEditMode
        if (!isEditMode) { // '저장' 버튼을 눌렀을 때
            saveChanges()
        }
        toggleEditModeUi(isEditMode)
    }

    // ✨ 추가: 생년월일 EditText 컨테이너 클릭 시 DatePickerDialog를 띄우는 함수
    private fun setupBirthdatePicker() {
        // DatePickerDialog를 띄우는 로직을 람다로 정의
        val showPickerAction = {
            if (isEditMode) {
                val calendar = Calendar.getInstance()
                val currentYear = binding.year.text.toString().toIntOrNull() ?: calendar.get(Calendar.YEAR)
                val currentMonth = binding.month.text.toString().toIntOrNull()?.minus(1) ?: calendar.get(Calendar.MONTH)
                val currentDay = binding.day.text.toString().toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        binding.year.setText(year.toString())
                        binding.month.setText((month + 1).toString())
                        binding.day.setText(dayOfMonth.toString())
                    },
                    currentYear,
                    currentMonth,
                    currentDay
                ).show()
            }
        }

        // 컨테이너와 각 EditText에 동일한 리스너 설정
        binding.birthDateContainer.setOnClickListener { showPickerAction() }
        binding.year.setOnClickListener { showPickerAction() }
        binding.month.setOnClickListener { showPickerAction() }
        binding.day.setOnClickListener { showPickerAction() }
    }

    // ✨ 추가: 변경된 내용을 ViewModel에 전달하여 저장하는 함수
    private fun saveChanges() {
        val newName = binding.name.text.toString()
        val newYear = binding.year.text.toString()
        val newMonth = binding.month.text.toString()
        val newDay = binding.day.text.toString()

        // 변경된 값만 null이 아닌 값으로 전달
        val nameToSave = if (newName != originalName) newName else null

        val newBirthDate = String.format("%s-%02d-%02d", newYear, newMonth.toInt(), newDay.toInt())
        val birthToSave = if (newBirthDate != originalBirthDate) newBirthDate else null

        // 변경된 내용이 있을 때만 저장 함수 호출
        if (nameToSave != null || birthToSave != null) {
            profileViewModel.saveProfileChanges(nameToSave, birthToSave)
        } else {
            // 변경된 내용이 없으면 그냥 수정 모드 종료
            isEditMode = false
            toggleEditModeUi(false)
        }
    }

    // ✨ 추가: 수정 모드에 따라 UI를 변경하는 함수
    private fun toggleEditModeUi(isEditing: Boolean) {
        binding.name.isEnabled = isEditing
        binding.year.isEnabled = isEditing
        binding.month.isEnabled = isEditing
        binding.day.isEnabled = isEditing

        val defaultTintColor = if (isEditing) R.color.ref_blue_150 else R.color.semantic_surface_assistive_based
        val defaultTextColor = if (isEditing) R.color.ref_blue_600 else R.color.ref_coolgray_700

        listOf(binding.name, binding.year, binding.month, binding.day).forEach { editText ->
            editText.background.setTint(ContextCompat.getColor(this, defaultTintColor))
            (editText as EditText).setTextColor(ContextCompat.getColor(this, defaultTextColor))
        }

        // ✨ --- 성별 칩 스타일링 로직 시작 --\
        // flexbox_gender에 뷰가 있는지 확인 (앱 충돌 방지)
        if (binding.flexboxGender.childCount > 0) {
            val genderChip = binding.flexboxGender.getChildAt(0)
            val genderTextView = genderChip.findViewById<TextView>(R.id.text_keyword)

            val genderTintColor = if (isEditing) R.color.ref_blue_150 else R.color.semantic_surface_assistive_based
            val genderTextColor = if (isEditing) R.color.ref_blue_600 else R.color.ref_coolgray_700

            genderChip.background.setTint(ContextCompat.getColor(this, genderTintColor))
            genderTextView.setTextColor(ContextCompat.getColor(this, genderTextColor))
        }
        // ✨ --- 성별 칩 스타일링 로직 끝 ---

        binding.appbar.btnToEdit.text = if (isEditing) "저장" else "수정"
    }


    // ✨ 추가: 성별 데이터를 받아서 칩(Chip) UI를 업데이트하는 함수
    private fun updateGenderChip(gender: Boolean?) {
        binding.flexboxGender.removeAllViews()
        val genderText = when (gender) {
            true -> "남자"; false -> "여자"; null -> "미설정"
        }

        val chipView = LayoutInflater.from(this).inflate(R.layout.item_summary_chip, binding.flexboxGender, false)
        val textView = chipView.findViewById<TextView>(R.id.text_keyword)
        textView.text = genderText

        // ✨ 수정 모드일 때만 바텀 시트를 띄우도록 클릭 리스너 설정
        chipView.setOnClickListener {
            if (isEditMode) {
                // 현재 성별 값을 담아 바텀 시트 생성 및 표시
                val currentGender = profileViewModel.userDetailInfo.value?.userGender
                ProfileBottomSheetFragment.newInstance(currentGender)
                    .show(supportFragmentManager, "ProfileBottomSheet")
            }
        }
        binding.flexboxGender.addView(chipView)
    }
    private fun updateCurationChips(tags: List<TagResponse>) {
        binding.flexboxLocation.removeAllViews()
        binding.flexboxJob.removeAllViews()
        binding.flexboxEducation.removeAllViews()
        binding.flexboxExtra.removeAllViews()
        binding.flexboxKeyword.removeAllViews()

        val groupedTags = tags.groupBy { it.fieldId }

        val locationTags = groupedTags[9] ?: emptyList()
        val jobTags = groupedTags[5] ?: emptyList()
        val educationTags = groupedTags[8] ?: emptyList()
        val extraTags = groupedTags[7] ?: emptyList()
        val keywordTags = listOfNotNull(
            groupedTags[1], groupedTags[2], groupedTags[3], groupedTags[4]
        ).flatten()

        addChipsToFlexbox(binding.flexboxLocation, locationTags, TagSelectionActivity.STEP1_FIELD_ID)
        addChipsToFlexbox(binding.flexboxJob, jobTags, TagSelectionActivity.STEP2_FIELD_ID)
        addChipsToFlexbox(binding.flexboxEducation, educationTags, TagSelectionActivity.STEP3_FIELD_ID)
        addChipsToFlexbox(binding.flexboxExtra, extraTags, TagSelectionActivity.STEP4_FIELD_ID)
        addChipsToFlexbox(binding.flexboxKeyword, keywordTags, TagSelectionActivity.STEP5_FIELD_ID)
    }

    private fun addChipsToFlexbox(flexbox: FlexboxLayout, tags: List<TagResponse>, fieldId: Int) {
        val margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
        ).toInt()

        val displayTags = if (tags.isNullOrEmpty()) {
            listOf(TagResponse(0, "해당사항 없음", fieldId))
        } else {
            tags
        }

        displayTags.forEach { tag ->
            val chipView = LayoutInflater.from(this).inflate(R.layout.item_summary_chip, flexbox, false)
            val textView = chipView.findViewById<TextView>(R.id.text_keyword)
            textView.text = tag.tagName

            chipView.setOnClickListener {
                val intent = Intent(this, TagSelectionActivity::class.java).apply {
                    putExtra(TagSelectionActivity.EXTRA_EDIT_MODE, true)
                    putExtra(TagSelectionActivity.EXTRA_STARTING_FIELD_ID, fieldId)
                    val userTags = profileViewModel.userTags.value?.toTypedArray()
                    putExtra(TagSelectionActivity.EXTRA_USER_TAGS, userTags)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            (chipView.layoutParams as? FlexboxLayout.LayoutParams)?.setMargins(0, 0, margin, margin)
            flexbox.addView(chipView)
        }
    }

    // ✨ 바텀 시트로부터 결과를 받기 위한 리스너 설정
    private fun setupFragmentResultListener() {
        supportFragmentManager.setFragmentResultListener("gender_selection", this) { _, bundle ->
            // bundle에서 'selectedGender' 키로 값을 가져옴. 키가 없으면 null.
            val selectedGender = bundle.getSerializable("selectedGender") as? Boolean?

            // --- ✨ 이 부분이 핵심적인 변경입니다 ---
            // 1. UI를 즉시 업데이트 (낙관적 업데이트)
            updateGenderChip(selectedGender)

            // 2. ViewModel에 백그라운드 서버 업데이트 요청
            profileViewModel.updateUserGender(selectedGender)
        }
    }
}