package com.aitechnologies.utripod.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aitechnologies.utripod.adapters.HomeTabAdapter
import com.aitechnologies.utripod.databinding.FragmentHomeBinding
import com.aitechnologies.utripod.ui.activities.MainActivity
import com.aitechnologies.utripod.ui.activities.NotificationActivity
import com.aitechnologies.utripod.ui.activities.TrendingPostActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var postsFragment: PostsFragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        postsFragment = PostsFragment()

        setupTab()

        binding.imgTrending.setOnClickListener {
            startActivity(Intent(requireContext(), TrendingPostActivity::class.java))
        }

        binding.imgNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }

        binding.imgLogo.setOnClickListener {
            startActivity(Intent(requireContext(),MainActivity::class.java))
            requireActivity().finish()
            requireActivity().overridePendingTransition(0,0)
        }

        return binding.root
    }

    private fun setupTab() {
        binding.viewpager.apply {
            adapter = HomeTabAdapter(childFragmentManager, lifecycle,postsFragment)
            TabLayoutMediator(binding.tabLayout, this) { tab, position ->
                when (position) {
                    0 -> tab.text = "HOME"
                    1 -> tab.text = "UVIS"
                }
                binding.viewpager.currentItem = tab.position
            }.attach()
        }

        binding.tabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    postsFragment.scrollToTop()
                }
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewpager.adapter = null
        _binding = null
    }

}