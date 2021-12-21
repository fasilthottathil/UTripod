package com.aitechnologies.utripod.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aitechnologies.utripod.ui.fragments.MyChatsFragment
import com.aitechnologies.utripod.ui.fragments.MyGroupsFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ChatsTabAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val chatsFragment: MyChatsFragment,
    private val groupsFragment: MyGroupsFragment
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }


    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> chatsFragment
            else -> groupsFragment
        }
    }
}