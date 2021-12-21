package com.aitechnologies.utripod.ui.fragments

import android.app.AlertDialog
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
import com.aitechnologies.utripod.databinding.FragmentPostsBinding
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.activities.*
import com.aitechnologies.utripod.ui.viewModels.PostViewModel
import com.aitechnologies.utripod.ui.viewModels.PostViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.onClickBottomSheetItem
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showPostBottomSheetDialog
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi

class PostsFragment : Fragment() {
    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private lateinit var postViewModel: PostViewModel
    private var posts: ArrayList<Posts> = arrayListOf()
    private val postAdapter by lazy { PostAdapter(requireContext()) }
    private var counter = 0
    private var blockedUserList: ArrayList<String> = arrayListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPostsBinding.inflate(inflater, container, false)

        val postRepository = PostRepository(requireActivity().application)

        val provider = PostViewModelProvider(postRepository, UserRepository())

        postViewModel = ViewModelProvider(this, provider)[PostViewModel::class.java]

        startLoading()

        setupRecyclerView()

        binding.swipeRefresh.setOnRefreshListener {
            posts.clear()
            counter = 0
            postViewModel.promotionLoaded = false
            postAdapter.setData(posts)
            postViewModel.getBlockedUsers(requireContext().getUsername())
        }

        postViewModel.getBlockedUsers(requireContext().getUsername())

        postViewModel.blockedUsers.observe(viewLifecycleOwner, {
            blockedUserList.addAll(it)
            postViewModel.loadPosts(blockedUserList)
        })

        postViewModel.posts.observe(viewLifecycleOwner, {
            ++counter
            if (it.isNotEmpty()) {
                it.forEach { post ->
                    var isContain = false
                    if (!blockedUserList.contains(post.username)) {
                        posts.forEach { p ->
                            if (p.id == post.id)
                                isContain = true
                        }
                    }
                    if (!isContain)
                        posts.add(post)
                }
            }
            if (counter == 2) {
                counter = 0
                posts.shuffle()
                postAdapter.setData(posts)
            }
        })

        postViewModel.statusMessage.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { loaded ->
                if (loaded)
                    hideLoading()
            }
        })

        postViewModel.sharePost.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                dismissProgress()
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

        postViewModel.isDeleted.observe(viewLifecycleOwner, {
            dismissProgress()
            posts.remove(it.data)
            postAdapter.setData(posts)
        })

        postViewModel.userProfile.observe(viewLifecycleOwner, @ExperimentalCoroutinesApi {
            dismissProgress()
            if (it[0].username == requireContext().getUsername()) {
                startActivity(Intent(requireContext(), MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(requireContext(), OthersProfileActivity::class.java)
                        .putExtra("user", it[0])
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        })

        postAdapter.onLikeClickListener {
            postViewModel.likePost(it)
        }


        postAdapter.onCommentClickListener @ExperimentalCoroutinesApi {
            startActivity(
                Intent(requireContext(), PostCommentActivity::class.java)
                    .putExtra("id", it.id)
                    .putExtra("username",it.username)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        postAdapter.onShareClickListener {
            requireContext().showProgress("Loading", false)
            postViewModel.sharePost(it)
        }

        postAdapter.onMoreClickListener {
            if (it.username == requireContext().getUsername()) {
                when (it.viewType) {
                    0 -> {
                        requireContext().showPostBottomSheetDialog(0, it)
                    }
                    1 -> {
                        requireContext().showPostBottomSheetDialog(0, it)
                    }
                    2 -> {
                        requireContext().showPostBottomSheetDialog(0, it)
                    }
                    3 -> {
                        requireContext().showPostBottomSheetDialog(1, it)
                    }
                    4 -> {
                        requireContext().showPostBottomSheetDialog(1, it)
                    }
                    5 -> {
                        requireContext().showPostBottomSheetDialog(1, it)
                    }
                }
            } else {
                requireContext().showPostBottomSheetDialog(2, it)
            }
        }

        onClickBottomSheetItem { type, selectedPost, isPromotion ->
            when (type) {
                0 -> postViewModel.sharePost(selectedPost)
                1 -> {
                    postViewModel.reportPost(selectedPost)
                    requireContext().shortToast("reported")
                }
                2 -> {
                    requireContext().showProgress("Deleting...", false)
                    if (isPromotion)
                        postViewModel.deletePromotion(selectedPost)
                    else
                        postViewModel.deletePost(selectedPost)
                }
                3 -> {
                    val bundle = Bundle().apply {
                        putParcelable("post", selectedPost)
                        putInt("type", 0)
                    }
                    val intent = Intent(requireContext(), PromotePostActivity::class.java)
                    intent.putExtra("bundle", bundle)

                    startActivity(intent)
                }
                4 -> {
                    val bundle = Bundle().apply {
                        putParcelable("post", selectedPost)
                    }
                    startActivity(
                        Intent(requireContext(), EditPostActivity::class.java)
                            .putExtra("bundle", bundle)
                    )
                }
                5 -> {
                    AlertDialog.Builder(requireContext()).apply {
                        setMessage("Do you want to block?")
                        setNegativeButton("No") { d, _ -> d.cancel() }
                        setPositiveButton("Yes") { d, _ ->
                            d.cancel()
                            postViewModel.blockUser(
                                requireContext().getUsername(),
                                selectedPost.username!!
                            )
                            posts.remove(selectedPost)
                            postAdapter.setData(posts)
                        }
                    }.create().show()
                }
            }
        }

        postAdapter.onImageClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    ViewImageActivity::class.java
                ).putExtra("image", it.post)
            )
        }

        postAdapter.onUsernameClickListener { list, posts ->
            if (list.isEmpty()) {
                requireContext().showProgress("Loading...", false)
                postViewModel.getUserProfile(posts.username.toString())
            } else {
                val arrayList: ArrayList<String> = arrayListOf()
                list.forEach {
                    arrayList.add(it)
                }
                startActivity(
                    Intent(requireContext(), TaggedUsersActivity::class.java)
                        .putExtra("list", arrayList)
                        .putExtra("bundle", Bundle().apply { putParcelable("post", posts) })
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

    private fun startLoading() {
        binding.shimmerLayout.startShimmer()
    }


    private fun setupRecyclerView() {
        binding.rvPosts.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun hideLoading() {
        binding.swipeRefresh.isRefreshing = false
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvPosts.adapter = null
        _binding = null
        postAdapter.releaseExoplayer()
        AppUtil.releaseUtils()
    }

    fun scrollToTop(){
        if (posts.isNotEmpty())
            binding.rvPosts.smoothScrollToPosition(0)
    }

}