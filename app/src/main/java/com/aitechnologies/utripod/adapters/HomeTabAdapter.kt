package com.aitechnologies.utripod.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aitechnologies.utripod.ui.fragments.PostsFragment
import com.aitechnologies.utripod.ui.fragments.UvisHomeFragment

class HomeTabAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val postsFragment: PostsFragment
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> postsFragment
            else -> UvisHomeFragment()
        }
    }
}