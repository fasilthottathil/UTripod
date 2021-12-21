package com.aitechnologies.utripod.ui.fragments

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
import com.aitechnologies.utripod.ui.viewModels.MyTaggedPostsViewModel
import com.aitechnologies.utripod.ui.viewModels.MyTaggedPostsViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showPostBottomSheetDialog
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class MyTaggedPostsFragment : Fragment() {
    private var _binding: FragmentMyPostsBinding? = null
    private val binding get() = _binding!!
    private val postAdapter by lazy { PostAdapter(requireContext()) }
    private lateinit var myTaggedPostsViewModel: MyTaggedPostsViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyPostsBinding.inflate(inflater, container, false)

        val myTaggedPostsViewModelProvider = MyTaggedPostsViewModelProvider(
            PostRepository(requireActivity().application),
            UserRepository()
        )

        myTaggedPostsViewModel = ViewModelProvider(
            this,
            myTaggedPostsViewModelProvider
        )[MyTaggedPostsViewModel::class.java]

        myTaggedPostsViewModel.getTaggedPosts(requireArguments().getString("username").toString())

        myTaggedPostsViewModel.myPostsListener.observe(viewLifecycleOwner, { event ->
            hideLoading()
            event.getContentIfNotHandled()?.let {
                if (it.message == "success") {
                    binding.rvPosts.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = postAdapter
                    }
                    if (it.data!!.isNotEmpty())
                        postAdapter.setData(it.data)
                }
            }
        })

        myTaggedPostsViewModel.sharePost.observe(viewLifecycleOwner, { event ->
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

        myTaggedPostsViewModel.isDeleted.observe(viewLifecycleOwner, {
            AppUtil.dismissProgress()
        })

        myTaggedPostsViewModel.userProfile.observe(viewLifecycleOwner, @ExperimentalCoroutinesApi {
            AppUtil.dismissProgress()
            if (it[0].username == requireContext().getUsername()) {
                startActivity(Intent(requireContext(), MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(requireContext(), OthersProfileActivity::class.java)
                        .putExtra("user", it[0])
                )
            }
        })

        postAdapter.onLikeClickListener {
            myTaggedPostsViewModel.likePost(it)
        }


        postAdapter.onCommentClickListener @ExperimentalCoroutinesApi {
            startActivity(
                Intent(requireContext(), PostCommentActivity::class.java)
                    .putExtra("id", it.id)
                    .putExtra("username",it.username)
            )
        }

        postAdapter.onShareClickListener {
            requireContext().showProgress("Loading", false)
            myTaggedPostsViewModel.sharePost(it)
        }

        postAdapter.onMoreClickListener {
            if (!isAdded) return@onMoreClickListener
            if (it.username == requireContext().getUsername()) {
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

        AppUtil.onClickBottomSheetItem { type, posts, isPromotion ->
            if (!isAdded) return@onClickBottomSheetItem
            when (type) {
                0 -> myTaggedPostsViewModel.sharePost(posts)
                1 -> {
                    myTaggedPostsViewModel.reportPost(posts)
                    requireContext().shortToast("reported")
                }
                2 -> {
                    requireContext().showProgress("Deleting...", false)
                    if (isPromotion)
                        myTaggedPostsViewModel.deletePromotion(posts)
                    else
                        myTaggedPostsViewModel.deletePost(posts)
                }
                3 -> {
                    val bundle = Bundle().apply {
                        putParcelable("post", posts)
                        putInt("type", 0)
                    }
                    val intent = Intent(requireContext(), PromotePostActivity::class.java)
                    intent.putExtra("bundle", bundle)

                    startActivity(intent)
                }
                4 -> {
                    val bundle = Bundle().apply {
                        putParcelable("post", posts)
                    }
                    startActivity(
                        Intent(requireContext(), EditPostActivity::class.java)
                            .putExtra("bundle", bundle)
                    )
                }
            }
        }

        postAdapter.onImageClickListener {
            if (!isAdded) return@onImageClickListener
            startActivity(
                Intent(
                    requireContext(),
                    ViewImageActivity::class.java
                ).putExtra("image", it.post)
            )
        }

        postAdapter.onUsernameClickListener { list, posts ->
            if (!isAdded) return@onUsernameClickListener
            if (list.isEmpty()) {
                requireContext().showProgress("Loading...", false)
                myTaggedPostsViewModel.getUserProfile(posts.username.toString())
            } else {
                val arrayList: ArrayList<String> = arrayListOf()
                list.forEach {
                    arrayList.add(it)
                }
                startActivity(
                    Intent(requireContext(), TaggedUsersActivity::class.java)
                        .putExtra("list", arrayList)
                )
            }
        }

        postAdapter.onVideoClickListener {
            startActivity(
                Intent(requireContext(), ViewVideoActivity::class.java)
                    .putExtra("video", it.post)
            )
        }

        return binding.root
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

    fun newInstance(username: String): MyTaggedPostsFragment {
        return MyTaggedPostsFragment().apply {
            arguments = Bundle().apply {
                putString("username", username)
            }
        }
    }

}