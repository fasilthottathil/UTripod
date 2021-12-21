package com.aitechnologies.utripod.ui.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.FragmentAddBinding
import com.aitechnologies.utripod.interfaces.UploadFileResponse
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.ui.activities.NotificationActivity
import com.aitechnologies.utripod.ui.activities.PromotePostActivity
import com.aitechnologies.utripod.ui.activities.TagUserActivity
import com.aitechnologies.utripod.ui.activities.TrendingPostActivity
import com.aitechnologies.utripod.ui.viewModels.AddPostProviderFactory
import com.aitechnologies.utripod.ui.viewModels.AddPostViewModel
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getProfession
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getProfileUrl
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.uploadFile
import com.aitechnologies.utripod.util.AppUtil.Companion.utripodFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class AddFragment : Fragment() {
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var addPostViewModel: AddPostViewModel
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var tagResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<Array<String>>
    private var uri: Uri? = null
    private var isUploaded = false
    private lateinit var alertDialog: AlertDialog
    private lateinit var textView: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddBinding.inflate(inflater, container, false)


        setUI()

        val postRepository = PostRepository(requireActivity().application)

        val addPostProvider = AddPostProviderFactory(postRepository, requireActivity().application)

        addPostViewModel = ViewModelProvider(this, addPostProvider)[AddPostViewModel::class.java]

        addPostViewModel.message.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> {
                        alertDialog.cancel()
                        requireContext().shortToast(it.message.toString())
                    }
                    1 -> {
                        alertDialog.cancel()
                        requireContext().shortToast(it.message.toString())
                        if (binding.promote.isChecked) {
                            startActivity(
                                Intent(requireContext(), PromotePostActivity::class.java)
                                    .putExtra("bundle", Bundle().apply {
                                        putParcelable("post", addPostViewModel.posts)
                                        putInt("type", 0)
                                    })
                            )
                        }
                        resetViews()
                    }
                    2 -> {
                        showLoading()
                    }
                }
            }

        })

        addPostViewModel.validate.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> binding.edtPost.error = it.message
                    1 -> requireContext().shortToast(it.message.toString())
                    else -> {
                        if (addPostViewModel.posts.type == 0) {
                            addPostViewModel.addPost()
                        } else {
                            if (isUploaded) {
                                addPostViewModel.addPost()
                            } else {
                                showLoading()
                                "Uploading...".also { text -> textView.text = text }
                                if (addPostViewModel.posts.type == 2) {
                                    val compressedFile = requireContext().utripodFile(
                                        System.currentTimeMillis().toString() + ".mp4"
                                    )
                                    VideoCompressor.start(
                                        requireContext(),
                                        uri!!,
                                        null,
                                        compressedFile.absolutePath,
                                        null,
                                        object : CompressionListener {
                                            override fun onCancelled() {

                                            }

                                            override fun onFailure(failureMessage: String) {
                                                Log.d("COMPRESS", failureMessage)

                                            }

                                            override fun onProgress(percent: Float) {
                                            }

                                            override fun onStart() {
                                            }

                                            override fun onSuccess() {
                                                requireActivity().runOnUiThread {
                                                    uploadFile(
                                                        Uri.fromFile(compressedFile),
                                                        addPostViewModel.posts.id.toString(),
                                                        "posts/",
                                                        object : UploadFileResponse {
                                                            override fun onSuccess(filePath: String) {
                                                                alertDialog.cancel()
                                                                addPostViewModel.posts.post =
                                                                    filePath
                                                                addPostViewModel.addPost()
                                                            }

                                                            override fun onProgress(progress: Int) {
                                                                "Uploading $progress %".also { text ->
                                                                    textView.text = text
                                                                }
                                                            }

                                                            override fun onFailure(message: String) {
                                                                alertDialog.cancel()
                                                                requireContext().shortToast(message)
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        },
                                        Configuration(
                                            quality = VideoQuality.VERY_LOW,
                                            frameRate = 24, /*Int, ignore, or null*/
                                            false,
                                        )
                                    )
                                } else {
                                    uploadFile(
                                        uri!!,
                                        addPostViewModel.posts.id.toString(),
                                        "posts/",
                                        object : UploadFileResponse {
                                            override fun onSuccess(filePath: String) {
                                                alertDialog.cancel()
                                                addPostViewModel.posts.post = filePath
                                                addPostViewModel.addPost()
                                            }

                                            override fun onProgress(progress: Int) {
                                                "Uploading $progress %".also { text ->
                                                    textView.text = text
                                                }
                                            }

                                            override fun onFailure(message: String) {
                                                alertDialog.cancel()
                                                requireContext().shortToast(message)
                                            }
                                        }
                                    )
                                }

                            }
                        }
                    }
                }
            }
        })

        imageResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                isUploaded = false
                addPostViewModel.posts.type = 1
                addPostViewModel.posts.viewType = 1
                uri = it.data!!.data
                showImage()
            }
        }

        videoResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                isUploaded = false
                addPostViewModel.posts.type = 2
                addPostViewModel.posts.viewType = 2
                uri = it.data!!.data
                showImage()
            }
        }

        tagResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {

                addPostViewModel.posts.tags =
                    it.data!!.extras!!.getStringArrayList("tags").toString()
                "${it.data!!.extras!!.getStringArrayList("tags")!!.size} user(s) selected".also { text ->
                    binding.txtTag.text = text
                }
            }
        }

        permissionResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            var isGranted = true
            it.forEach { (_, granted) ->
                if (!granted)
                    isGranted = false
            }
            if (isGranted) {
                videoResultLauncher.launch(
                    Intent().apply {
                        type = "video/*"
                        action = Intent.ACTION_GET_CONTENT
                    }
                )
            } else requireContext().shortToast("Permission denied")
        }

        binding.btnPost.setOnClickListener {
            addPostViewModel.posts.id = System.currentTimeMillis().toString()
            addPostViewModel.posts.username = requireContext().getUsername()
            addPostViewModel.posts.profession = requireContext().getProfession()
            addPostViewModel.posts.profileUrl = requireContext().getProfileUrl()
            if (addPostViewModel.posts.type == 0)
                addPostViewModel.posts.post = binding.edtPost.text.toString()
            addPostViewModel.posts.hashTags = binding.edtHashTags.text.toString()
                .replace(" ", "")
                .split("#").toString()
            addPostViewModel.posts.description = binding.edtPost.text.toString()

            addPostViewModel.validate()

        }

        binding.txtUploadImage.setOnClickListener {
            imageResultLauncher.launch(
                Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                }
            )
        }

        binding.txtUploadVideo.setOnClickListener {
            permissionResultLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }

        binding.txtTag.setOnClickListener {
            tagResultLauncher.launch(Intent(requireContext(), TagUserActivity::class.java))
        }


        return binding.root
    }

    private fun setUI() {
        binding.txtUsername.text = requireContext().getUsername()
        Glide.with(requireContext())
            .load(requireContext().getProfileUrl())
            .apply(RequestOptions.circleCropTransform())
            .into(binding.imgProfile)
        binding.imgNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }
        binding.imgTrending.setOnClickListener {
            startActivity(Intent(requireContext(), TrendingPostActivity::class.java))
        }
    }

    private fun resetViews() {
        binding.edtPost.text.clear()
        binding.edtHashTags.text.clear()
        binding.imgPost.visibility = GONE
        binding.promote.isChecked = false
        isUploaded = false
        "Tag friends".also { binding.txtTag.text = it }
        uri = null
    }

    private fun showLoading() {
        val view = inflate(requireContext(), R.layout.progress_view, null)
        textView = view.findViewById(R.id.txtMessage)
        "Loading...".also { textView.text = it }
        alertDialog = AlertDialog.Builder(requireContext())
            .apply {
                setCancelable(false)
                setView(view)
            }.create()
        alertDialog.show()
    }

    private fun showImage() {
        binding.imgPost.visibility = VISIBLE
        Glide.with(requireContext())
            .load(uri!!)
            .into(binding.imgPost)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        AppUtil.releaseUtils()
    }

}