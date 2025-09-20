package com.example.soar.MyPage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.example.soar.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProfileBottomSheetFragment : BottomSheetDialogFragment() {

    // 선택된 성별을 저장할 변수 (Boolean?: true=남자, false=여자, null=미설정)
    private var selectedGender: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Activity로부터 전달받은 현재 성별 값으로 초기화
        arguments?.let {
            // Bundle에 "currentGender" 키가 있는지 확인하여 안전하게 값을 가져옴
            if (it.containsKey("currentGender")) {
                selectedGender = it.getBoolean("currentGender")
            } else {
                // 키가 없는 경우, '미설정' 상태를 나타내는 null로 처리
                selectedGender = null
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val maleTextView = view.findViewById<TextView>(R.id.dropdown1)
        val femaleTextView = view.findViewById<TextView>(R.id.dropdown2)
        val unknownTextView = view.findViewById<TextView>(R.id.dropdown3)
        val confirmButton = view.findViewById<Button>(R.id.btn_confirm)

        // 처음 화면에 표시될 때 현재 선택된 성별 UI를 업데이트
        updateSelectionUI(view)

        // 각 성별 옵션 클릭 리스너 설정
        maleTextView.setOnClickListener {
            selectedGender = true
            updateSelectionUI(view)
        }
        femaleTextView.setOnClickListener {
            selectedGender = false
            updateSelectionUI(view)
        }
        unknownTextView.setOnClickListener {
            selectedGender = null
            updateSelectionUI(view)
        }

        // '확인' 버튼 클릭 리스너 설정
        confirmButton.setOnClickListener {
            // 선택된 값을 Activity로 전달
            setFragmentResult("gender_selection", bundleOf("selectedGender" to selectedGender))
            // 바텀 시트 닫기
            dismiss()
        }
    }

    // 선택된 항목에 따라 UI (글자색, 굵기)를 업데이트하는 함수
    private fun updateSelectionUI(view: View) {
        val maleTextView = view.findViewById<TextView>(R.id.dropdown1)
        val femaleTextView = view.findViewById<TextView>(R.id.dropdown2)
        val unknownTextView = view.findViewById<TextView>(R.id.dropdown3)

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.semantic_text_primary)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.ref_gray_600)

        // 남자
        maleTextView.setTextColor(if (selectedGender == true) selectedColor else defaultColor)
        maleTextView.paint.isFakeBoldText = (selectedGender == true)

        // 여자
        femaleTextView.setTextColor(if (selectedGender == false) selectedColor else defaultColor)
        femaleTextView.paint.isFakeBoldText = (selectedGender == false)

        // 미설정
        unknownTextView.setTextColor(if (selectedGender == null) selectedColor else defaultColor)
        unknownTextView.paint.isFakeBoldText = (selectedGender == null)
    }

    // Activity에서 프래그먼트를 생성할 때 값을 안전하게 전달하기 위한 companion object
    companion object {
        fun newInstance(currentGender: Boolean?): ProfileBottomSheetFragment {
            val fragment = ProfileBottomSheetFragment()
            val args = Bundle()
            // currentGender가 null이 아닐 때만 Bundle에 값을 넣음
            currentGender?.let { args.putBoolean("currentGender", it) }
            fragment.arguments = args
            return fragment
        }
    }

    // --- 아래는 바텀 시트의 모서리를 둥글게 하기 위한 스타일 코드입니다 ---
    override fun onCreateDialog(savedInstanceState: Bundle?) =
        object : BottomSheetDialog(requireContext(), theme) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                window?.setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT)
                )
            }
        }

    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
    }
}