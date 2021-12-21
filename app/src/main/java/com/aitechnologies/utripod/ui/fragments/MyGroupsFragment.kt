package com.aitechnologies.utripod.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.adapters.MyGroupsAdapter
import com.aitechnologies.utripod.databinding.FragmentMyGroupsBinding
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.ui.activities.CreateGroupActivity
import com.aitechnologies.utripod.ui.activities.GroupChatActivity
import com.aitechnologies.utripod.ui.viewModels.MyGroupsViewModel
import com.aitechnologies.utripod.ui.viewModels.MyGroupsViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MyGroupsFragment : Fragment() {
    private var _binding: FragmentMyGroupsBinding? = null
    private val binding get() = _binding!!
    private lateinit var myGroupsViewModel: MyGroupsViewModel
    private val myGroupsAdapter by lazy { MyGroupsAdapter(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyGroupsBinding.inflate(inflater, container, false)

        val myGroupsViewModelProvider = MyGroupsViewModelProvider(ChatsRepository())

        myGroupsViewModel = ViewModelProvider(
            this,
            myGroupsViewModelProvider
        )[MyGroupsViewModel::class.java]

        setupRecyclerView()

        myGroupsViewModel.getJoinedGroupsIdList(requireContext().getUsername())

        myGroupsViewModel.idList.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.message) {
                    "error" -> {
                        hideLoading()
                        binding.txtNoChats.visibility = VISIBLE
                        "An error occurred".also { text -> binding.txtNoChats.text = text }
                    }
                    "success" -> {
                        myGroupsViewModel.getJoinedGroups(it.data!!)
                    }
                }
            }
        })

        myGroupsViewModel.groupsListener.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                hideLoading()
                when (it.message) {
                    "error" -> {
                        binding.txtNoChats.visibility = VISIBLE
                        "An error occurred".also { text -> binding.txtNoChats.text = text }
                    }
                    "empty" -> {
                        binding.txtNoChats.visibility = VISIBLE
                        "No joined groups found!".also { text ->
                            binding.txtNoChats.text = text
                        }
                    }
                    "success" -> {
                        binding.txtNoChats.visibility = INVISIBLE
                        myGroupsAdapter.setData(it.data!!)
                    }
                }
            }
        })

        myGroupsAdapter.setOnChatClickListener {
            val bundle = Bundle().apply {
                putParcelable("group", it)
            }
            startActivity(
                Intent(requireContext(), GroupChatActivity::class.java)
                    .putExtra("bundle", bundle)
            )
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), CreateGroupActivity::class.java))
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvMyGroups.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myGroupsAdapter
        }
    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
        binding.txtNoChats.visibility = INVISIBLE
        binding.fabAdd.visibility = VISIBLE
    }

    fun search(query:String){

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvMyGroups.adapter = null
        _binding = null
    }

}