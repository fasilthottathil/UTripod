package com.aitechnologies.utripod.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.adapters.PostAdapter
import com.aitechnologies.utripod.databinding.FragmentMyPostsBinding
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.activities.*
import com.aitechnologies.utripod.ui.viewModels.MyPostsViewModel
import com.aitechnologies.utripod.ui.viewModels.MyPostsViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showPostBottomSheetDialog
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.util.UTripodApp.Companion.getAppInstance
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class MyPostsFragment : Fragment() {
    private var _binding: FragmentMyPostsBinding? = null
    private val binding get() = _binding!!
    private val postAdapter by lazy { PostAdapter(requireContext()) }
    private lateinit var myPostsViewModel: MyPostsViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyPostsBinding.inflate(inflater, container, false)

        val myPostsViewModelProvider = MyPostsViewModelProvider(
            PostRepository(requireActivity().application),
            UserRepository()
        )


        myPostsViewModel = ViewModelProvider(
            requireActivity(),
            myPostsViewModelProvider
        )[MyPostsViewModel::class.java]

        myPostsViewModel.getMyPosts(requireArguments().getString("username").toString())

        myPostsViewModel.myPostsListener.observe(viewLifecycleOwner, { event ->
            hideLoading()
            event.getContentIfNotHandled()?.let {
                binding.rvPosts.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = postAdapter
                }
                if (it.data!!.isNotEmpty())
                    postAdapter.setData(it.data)
            }
        })

        myPostsViewModel.sharePost.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                AppUtil.dismissProgress()
                if (it != "null")
                    startActivity(
                        Intent.createChooser(
                            Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, it)
                                type = "text/plain"
                            },
                            "Share to"
                        )
                    )
            }
        })

        myPostsViewModel.isDeleted.observe(
            viewLifecycleOwner,
            @SuppressLint("NotifyDataSetChanged") {
                AppUtil.dismissProgress()
                binding.rvPosts.adapter?.notifyDataSetChanged()
            })

        myPostsViewModel.userProfile.observe(viewLifecycleOwner, @ExperimentalCoroutinesApi {
            AppUtil.dismissProgress()
            if (it[0].username == getAppInstance().getUsername()) {
                startActivity(Intent(getAppInstance(), MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(getAppInstance(), OthersProfileActivity::class.java)
                        .putExtra("user", it[0])
                        .addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                )
            }
        })
        postAdapter.onLikeClickListener {
            myPostsViewModel.likePost(it)
        }


        postAdapter.onCommentClickListener @ExperimentalCoroutinesApi {
            startActivity(
                Intent(getAppInstance(), PostCommentActivity::class.java)
                    .putExtra("id", it.id)
                    .putExtra("username", it.username)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        postAdapter.onShareClickListener {
            requireContext().showProgress("Loading", false)
            myPostsViewModel.sharePost(it)
        }

        postAdapter.onMoreClickListener {
            if (it.username == getAppInstance().getUsername()) {
                when (it.viewType) {
                    0 -> {
                        requireContext().showPostBottomSheetDialog(0, it, false)
                    }
                    1 -> {
                        requireContext().showPostBottomSheetDialog(0, it, false)
                    }
                    2 -> {
                        requireContext().showPostBottomSheetDialog(0, it, false)
                    }
                    3 -> {
                        requireContext().showPostBottomSheetDialog(1, it, false)
                    }
                    4 -> {
                        requireContext().showPostBottomSheetDialog(1, it, false)
                    }
                    5 -> {
                        requireContext().showPostBottomSheetDialog(1, it, false)
                    }

                }
            } else {
                requireContext().showPostBottomSheetDialog(2, it)
            }
        }

        setOnClickBottomSheetItemListeners()

        AppUtil.onClickBottomSheetItem { type, posts, isPromotion ->
            when (type) {
                0 -> myPostsViewModel.sharePost(posts)
                1 -> {
                    myPostsViewModel.reportPost(posts)
                    getAppInstance().shortToast("reported")
                }
                2 -> {
                    requireContext().showProgress("Deleting...", false)
                    if (isPromotion)
                        myPostsViewModel.deletePromotion(posts)
                    else
                        myPostsViewModel.deletePost(posts)
                }
                3 -> {
                    val bundle = Bundle().apply {
                        putParcelable("post", posts)
                        putInt("type", 0)
                    }
                    val intent = Intent(getAppInstance(), PromotePostActivity::class.java)
                    intent.putExtra("bundle", bundle)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    getAppInstance().startActivity(intent)
                }
                4 -> {
                    val bundle = Bundle().apply {
                        putParcelable("post", posts)
                    }
                    getAppInstance().startActivity(
                        Intent(getAppInstance(), EditPostActivity::class.java)
                            .putExtra("bundle", bundle)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        }

        postAdapter.onImageClickListener {
            startActivity(
                Intent(
                    getAppInstance(),
                    ViewImageActivity::class.java
                ).putExtra("image", it.post)
            )
        }

        postAdapter.onUsernameClickListener { list, posts ->
            if (list.isEmpty()) {
                requireContext().showProgress("Loading...", false)
                myPostsViewModel.getUserProfile(posts.username.toString())
            } else {
                val arrayList: ArrayList<String> = arrayListOf()
                list.forEach {
                    arrayList.add(it)
                }
                getAppInstance().startActivity(
                    Intent(getAppInstance(), TaggedUsersActivity::class.java)
                        .putExtra("list", arrayList)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("bundle", Bundle().apply { putParcelable("post", posts) })
                )
            }
        }

        postAdapter.onVideoClickListener {
            getAppInstance().startActivity(
                Intent(getAppInstance(), ViewVideoActivity::class.java)
                    .putExtra("video", it.post)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        return binding.root
    }

    private fun setOnClickBottomSheetItemListeners() {

    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvPosts.adapter = null
        _binding = null
        postAdapter.releaseExoplayer()
        AppUtil.releaseUtils()
    }

    fun newInstance(username: String): MyPostsFragment {
        return MyPostsFragment().apply {
            arguments = Bundle().apply {
                putString("username", username)
            }
        }
    }


}