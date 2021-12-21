package com.aitechnologies.utripod.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.adapters.SearchUserAdapter
import com.aitechnologies.utripod.databinding.FragmentSearchBinding
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.activities.MyProfileActivity
import com.aitechnologies.utripod.ui.activities.NotificationActivity
import com.aitechnologies.utripod.ui.activities.OthersProfileActivity
import com.aitechnologies.utripod.ui.activities.TrendingPostActivity
import com.aitechnologies.utripod.ui.viewModels.SearchViewModel
import com.aitechnologies.utripod.ui.viewModels.SearchViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var stateList: Array<String> = arrayOf()
    private val genderList = arrayListOf("Male", "Female", "Others")
    private var professionList: Array<String> = arrayOf()
    private var ageList: Array<String> = arrayOf()
    private lateinit var searchViewModel: SearchViewModel
    private val userAdapter by lazy { SearchUserAdapter(requireContext()) }
    private val followingList:ArrayList<String> = arrayListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        val searchViewModelProvider = SearchViewModelProvider(
            UserRepository()
        )

        searchViewModel = ViewModelProvider(
            this,
            searchViewModelProvider
        )[SearchViewModel::class.java]


        searchViewModel.getFollowingList(requireContext().getUsername())

        searchViewModel.followersList.observe(viewLifecycleOwner,{
            hideLoading()
            binding.appBarLayout.visibility = VISIBLE
            setSpinner()
            followingList.addAll(it)
            userAdapter.setFollowers(followingList)
        })


        searchViewModel.users.observe(viewLifecycleOwner, @SuppressLint("NotifyDataSetChanged") {
            hideLoading()
            binding.rvUsers.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = userAdapter
            }
            userAdapter.setData(it)
            userAdapter.notifyDataSetChanged()
        })

        binding.edtSearch.setOnEditorActionListener { _, i, _ ->
            val search = binding.edtSearch.text.toString()
            if (i == EditorInfo.IME_ACTION_SEARCH && search.isNotEmpty() && search.isNotBlank()) {
                showLoading()
                searchViewModel.searchUser(search)
            }
            true
        }

        userAdapter.setOnUserClickListener @ExperimentalCoroutinesApi {
            if (it.username == requireContext().getUsername()) {
                startActivity(Intent(requireContext(), MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(requireContext(), OthersProfileActivity::class.java)
                        .putExtra("user", it)
                )
            }
        }

        userAdapter.setOnFollowClickListener  @DelicateCoroutinesApi{
            searchViewModel.followOrUnfollow(
                it,
                requireActivity().application
            )
        }

        binding.imgNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }
        binding.imgTrending.setOnClickListener {
            startActivity(Intent(requireContext(), TrendingPostActivity::class.java))
        }

        return binding.root
    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
    }

    private fun setSpinner() {

        stateList = resources.getStringArray(R.array.india_states)
        ageList = resources.getStringArray(R.array.age)
        professionList = resources.getStringArray(R.array.profession)

        binding.genderSpinner.apply {
            adapter = ArrayAdapter(
                requireContext(),
                R.layout.drop_down_text_white_item,
                genderList
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p1 != null) {
                        (p1 as TextView).setTextColor(Color.GRAY)
                        showLoading()
                        searchViewModel.getUserByGender(genderList[p2])
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
        }

        binding.locationSpinner.apply {
            adapter = ArrayAdapter(
                requireContext(),
                R.layout.drop_down_text_white_item,
                stateList
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p1 != null) {
                        (p1 as TextView).setTextColor(Color.GRAY)
                        showLoading()
                        searchViewModel.getUserByRegion(stateList[p2])
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
        }

        binding.ageSpinner.apply {
            adapter = ArrayAdapter(
                requireContext(),
                R.layout.drop_down_text_white_item,
                ageList
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p1 != null) {
                        (p1 as TextView).setTextColor(Color.GRAY)
                        if (p2 != 0){
                            showLoading()
                            searchViewModel.getUserByAge(ageList[p2])
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
        }

        binding.professionSpinner.apply {
            adapter = ArrayAdapter(
                requireContext(),
                R.layout.drop_down_text_white_item,
                professionList
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p1 != null) {
                        (p1 as TextView).setTextColor(Color.GRAY)
                        showLoading()
                        searchViewModel.getUserByProfession(professionList[p2])
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
        }

    }

    private fun showLoading() {
        binding.progressCircular.visibility = VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvUsers.adapter = null
        _binding = null
    }

}