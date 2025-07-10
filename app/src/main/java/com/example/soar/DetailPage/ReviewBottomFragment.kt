package com.example.soar.DetailPage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.soar.databinding.FragmentReviewBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ReviewBottomFragment(
    private val onDeleteRequest: () -> Unit,
    private val onEditRequest: () -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: FragmentReviewBottomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        object : com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), theme) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                window?.setBackgroundDrawable(
                    android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
                )
            }
        }

    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEdit.setOnClickListener {
            onEditRequest()
            dismiss()
        }
        binding.btnDelete.setOnClickListener {
            onDeleteRequest()
            dismiss()
        }
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




