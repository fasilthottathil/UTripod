package com.aitechnologies.utripod.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aitechnologies.utripod.ui.fragments.MyPostsFragment
import com.aitechnologies.utripod.ui.fragments.MyTaggedPostsFragment
import com.aitechnologies.utripod.uvis.fragments.MyUvisFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ProfileTabAdapter(
    private val username: String,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }


    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MyPostsFragment().newInstance(username)
            1 -> MyTaggedPostsFragment().newInstance(username)
            else -> MyUvisFragment.newInstance(username)
        }
    }
}