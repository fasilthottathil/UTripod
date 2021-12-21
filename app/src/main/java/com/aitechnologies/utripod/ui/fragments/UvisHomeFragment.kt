package com.aitechnologies.utripod.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aitechnologies.utripod.adapters.UvisHomeTabAdapter
import com.aitechnologies.utripod.databinding.FragmentUvisHomeBinding

class UvisHomeFragment : Fragment() {
    private var _binding: FragmentUvisHomeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUvisHomeBinding.inflate(inflater, container, false)

        binding.viewpager.apply {
            adapter = UvisHomeTabAdapter(childFragmentManager, lifecycle)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewpager.adapter = null
        _binding = null
    }

}