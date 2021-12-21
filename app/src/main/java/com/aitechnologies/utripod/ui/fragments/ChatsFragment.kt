package com.aitechnologies.utripod.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.adapters.ActiveUserAdapter
import com.aitechnologies.utripod.adapters.ChatsTabAdapter
import com.aitechnologies.utripod.databinding.FragmentChatsBinding
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.activities.NotificationActivity
import com.aitechnologies.utripod.ui.activities.PrivateChatActivity
import com.aitechnologies.utripod.ui.activities.TrendingPostActivity
import com.aitechnologies.utripod.ui.viewModels.ChatsProvider
import com.aitechnologies.utripod.ui.viewModels.ChatsViewModel
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ChatsViewModel
    private lateinit var provider: ChatsProvider
    private val activeUserAdapter by lazy { ActiveUserAdapter(requireContext()) }
    private lateinit var myChatsFragment: MyChatsFragment
    private lateinit var myGroupsFragment: MyGroupsFragment
    private var position = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatsBinding.inflate(inflater, container, false)

        myChatsFragment = MyChatsFragment()
        myGroupsFragment = MyGroupsFragment()

        setUI()

        provider = ChatsProvider(UserRepository(), ChatsRepository())
        viewModel = ViewModelProvider(this, provider)[ChatsViewModel::class.java]


        viewModel.activeUsers.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.message) {
                    "success" -> {
                        if (it.data!!.isEmpty()) {
                            binding.txtNoActive.visibility = VISIBLE
                        } else {
                            requireActivity().shortToast("hei")
                            binding.txtNoActive.visibility = INVISIBLE
                            binding.activeUsers.apply {
                                setHasFixedSize(true)
                                layoutManager = LinearLayoutManager(
                                    requireContext(),
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                                adapter = activeUserAdapter
                            }
                            activeUserAdapter.setData(it.data)
                        }
                    }
                    "no users" -> {
                        requireActivity().shortToast("hei")
                        binding.txtNoActive.visibility = VISIBLE
                    }
                }
            }
        })

        activeUserAdapter.setOnChatClickListener {
            startActivity(
                Intent(requireContext(), PrivateChatActivity::class.java)
                    .putExtra("bundle", Bundle().apply { putParcelable("chats", it) })
            )
        }

        viewModel.getFollowings(requireContext().getUsername())

        setupTab()

        binding.edtSearch.addTextChangedListener {
            when (position) {
                0 -> myChatsFragment.search(binding.edtSearch.text.toString())
                1 -> myGroupsFragment.search(binding.edtSearch.text.toString())
            }
        }

        return binding.root
    }

    private fun setUI() {
        binding.imgNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }
        binding.imgTrending.setOnClickListener {
            startActivity(Intent(requireContext(), TrendingPostActivity::class.java))
        }
    }

    private fun setupTab() {
        binding.viewpager.apply {
            adapter = ChatsTabAdapter(
                childFragmentManager,
                lifecycle,
                myChatsFragment,
                myGroupsFragment
            )
            TabLayoutMediator(binding.tabLayout, this) { tab, position ->
                when (position) {
                    0 -> tab.text = "Chats"
                    1 -> tab.text = "Groups"
                }
                binding.viewpager.currentItem = tab.position
            }.attach()
            binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    binding.edtSearch.setText("")
                    position = tab?.position ?: 0
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}