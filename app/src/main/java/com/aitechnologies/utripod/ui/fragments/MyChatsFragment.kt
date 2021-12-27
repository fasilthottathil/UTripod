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
import com.aitechnologies.utripod.adapters.MyChatsAdapter
import com.aitechnologies.utripod.databinding.FragmentMyChatsBinding
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.ui.activities.PrivateChatActivity
import com.aitechnologies.utripod.ui.viewModels.MyChatsViewModel
import com.aitechnologies.utripod.ui.viewModels.MyChatsViewModelProvider
import com.aitechnologies.utripod.util.UTripodApp.Companion.getAppInstance
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MyChatsFragment : Fragment() {
    private var _binding: FragmentMyChatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var myChatsViewModel: MyChatsViewModel
    private val myChatsAdapter by lazy { MyChatsAdapter(getAppInstance()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyChatsBinding.inflate(inflater, container, false)

        val myChatsViewModelProvider = MyChatsViewModelProvider(ChatsRepository())

        myChatsViewModel = ViewModelProvider(
            this,
            myChatsViewModelProvider
        )[MyChatsViewModel::class.java]

        setupRecyclerView()

        myChatsViewModel.getMyChats(requireContext().getUsername())

        myChatsViewModel.chatsListener.observe(viewLifecycleOwner, { event ->
            hideLoading()
            event.getContentIfNotHandled()?.let {
                when (it.message) {
                    "error" -> {
                        binding.txtNoChats.visibility = VISIBLE
                        "An error occurred".also { text -> binding.txtNoChats.text = text }
                    }
                    "success" -> {
                        if (it.data!!.isEmpty()) {
                            binding.txtNoChats.visibility = VISIBLE
                            "No chats found".also { text -> binding.txtNoChats.text = text }
                        } else {
                            binding.txtNoChats.visibility = INVISIBLE
                            myChatsAdapter.setData(it.data)
                        }
                    }
                }
            }
        })

        myChatsAdapter.setOnChatClickListener {
            startActivity(
                Intent(requireContext(), PrivateChatActivity::class.java)
                    .putExtra("bundle", Bundle().apply { putParcelable("chats", it) })
            )
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvMyChats.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myChatsAdapter
        }
    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
        binding.txtNoChats.visibility = INVISIBLE
    }

    fun search(query:String){
        myChatsAdapter.filter.filter(query)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvMyChats.adapter = null
        _binding = null
    }

}