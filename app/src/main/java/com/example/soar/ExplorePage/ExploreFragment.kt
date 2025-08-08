package com.example.soar.ExplorePage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.soar.R
import com.example.soar.databinding.FragmentExploreBinding


class ExploreFragment : Fragment() {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private var fieldId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 홈 화면에서 넘어올 시 받아올 필드 아이디
        fieldId = arguments?.getInt("category_id")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 확인용 임시 바인딩
        binding.textSample.text = fieldId.toString()

        binding.btnPersonalBiz.setOnClickListener {
            val intent = Intent(requireContext(), PersonalBizActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(
                R.anim.slide_in_up,
                R.anim.slide_out_down
            )
        }

    }

    companion object {
    }
}