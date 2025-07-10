package com.example.soar.ArchivingPage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.EntryPage.Onboarding.OnBoardingActivity
import com.example.soar.EntryPage.SignIn.LoginActivity
import com.example.soar.R
import com.example.soar.databinding.FragmentArchivingBinding

class ArchivingFragment : Fragment() {
    private var _binding: FragmentArchivingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArchivingBinding.inflate(inflater, container, false)

        binding.btn1.setOnClickListener {
            val intent = Intent(requireContext(), DetailPageActivity::class.java)
            startActivity(intent)
        }

        binding.btn2.setOnClickListener {
            val intent = Intent(requireContext(), OnBoardingActivity::class.java)
            startActivity(intent)
        }

        binding.btn3.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}