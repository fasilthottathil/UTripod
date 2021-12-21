package com.aitechnologies.utripod.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.*
import com.aitechnologies.utripod.models.HashTag
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.ui.activities.PostHashTagActivity
import com.aitechnologies.utripod.ui.activities.ViewUsersActivity
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.getTimeAgo
import com.aitechnologies.utripod.util.UTripodApp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource

class PostAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2
        const val TYPE_TEXT_PROMOTION = 3
        const val TYPE_IMAGE_PROMOTION = 4
        const val TYPE_VIDEO_PROMOTION = 5
    }

    private val posts: ArrayList<Posts> = arrayListOf()

    private var onLikeClickListener: ((Posts) -> Unit)? = null
    private var onCommentClickListener: ((Posts) -> Unit)? = null
    private var onShareClickListener: ((Posts) -> Unit)? = null
    private var onMoreClickListener: ((Posts) -> Unit)? = null
    private var onImageClickListener: ((Posts) -> Unit)? = null
    private var onVideoClickListener: ((Posts) -> Unit)? = null
    private var onUsernameClickListener: ((List<String>, Posts) -> Unit)? = null

    private var exoplayerList: ArrayList<ExoPlayer> = arrayListOf()

    inner class MyDiffUtil(
        private val oldList: List<Posts>,
        private val newList: List<Posts>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT -> TypeTextViewHolder(
                PostTextItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_IMAGE -> TypeImageViewHolder(
                PostImageItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_VIDEO -> TypeVideoViewHolder(
                PostVideoItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_TEXT_PROMOTION -> TypeTextPromotionViewHolder(
                PostTextPromotionItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_IMAGE_PROMOTION -> TypeImagePromotionViewHolder(
                PostImagePromotionItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> TypeVideoPromotionViewHolder(
                PostVideoPromotionItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (posts[position].viewType) {
            TYPE_TEXT -> (holder as TypeTextViewHolder).bind(position)
            TYPE_IMAGE -> (holder as TypeImageViewHolder).bind(position)
            TYPE_VIDEO -> (holder as TypeVideoViewHolder).bind(position)
            TYPE_TEXT_PROMOTION -> (holder as TypeTextPromotionViewHolder).bind(position)
            TYPE_IMAGE_PROMOTION -> (holder as TypeImagePromotionViewHolder).bind(position)
            TYPE_VIDEO_PROMOTION -> (holder as TypeVideoPromotionViewHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun getItemViewType(position: Int): Int {
        return posts[position].viewType!!
    }

    inner class TypeTextViewHolder(
        val binding: PostTextItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {

                Glide.with(context)
                    .load(posts[position].profileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile)

                val tags = posts[position].tags!!.replace("[", "").replace("]", "").split(",")
                    .filter { it.isNotEmpty() }

                txtUsername.text = if (tags.isEmpty()) {
                    posts[position].username
                } else {
                    if (tags.size >= 2)
                        posts[position].username + "," + tags[0] + " and ${tags.size - 1} others"
                    else
                        posts[position].username + " and " + tags[0]
                }

                txtProfession.text = posts[position].profession.toString()
                txtPost.text = posts[position].post
                txtComments.text = posts[position].comments.toString()
                txtShares.text = posts[position].shares.toString()
                txtDate.text = getTimeAgo(posts[position].timestamp!!.toDate().time)

                var isLiked = false

                val likesList =
                    posts[position].likesList!!.replace("[", "").replace("]", "").split(",")

                var likes = if (likesList[0].isEmpty())
                    0
                else
                    likesList.size

                txtLikes.text = likes.toString()

                if (likesList.isNotEmpty()) {
                    likesList.forEach {
                        if (it.trim() == context.getUsername())
                            isLiked = true
                    }
                    if (isLiked) {
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                    }
                } else {
                    imgLike.setImageResource(R.drawable.ic_diamond_white)
                }

                //hashtags
                val list = posts[position].hashTags!!.replace("[", "").replace("]", "").split(",")
                val hashTag:ArrayList<HashTag> = arrayListOf()
                list.forEach {
                    if (it.isNotBlank())
                        hashTag.add(HashTag(it.trim(),0))
                }
                val hashTagAdapter = HashTagAdapter(hashTag)

                rvHashtags.apply {
                    setHasFixedSize(true)
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = hashTagAdapter
                }

                hashTagAdapter.setOnHashTagClickListener {
                    context.startActivity(
                        Intent(context, PostHashTagActivity::class.java)
                            .putExtra("hashtag", it)
                    )
                }

                imgLike.setOnClickListener {
                    if (isLiked) {
                        likes -= 1
                        isLiked = false
                        txtLikes.text = (likes).toString()
                        imgLike.setImageResource(R.drawable.ic_diamond_white)
                    } else {
                        likes += 1
                        isLiked = true
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                        txtLikes.text = (likes).toString()
                    }
                    onLikeClickListener?.let {
                        it(posts[position])
                    }
                }

                imgComment.setOnClickListener {
                    onCommentClickListener?.let {
                        it(posts[position])
                    }
                }

                imgShare.setOnClickListener {
                    onShareClickListener?.let {
                        it(posts[position])
                    }
                }

                imgMore.setOnClickListener {
                    onMoreClickListener?.let {
                        it(posts[position])
                    }
                }

                txtUsername.setOnClickListener {
                    val tagList = arrayListOf<String>()
                    tags.forEach {
                        tagList.add(it.trim())
                    }
                    onUsernameClickListener?.let {
                        it(tagList, posts[position])
                    }
                }

                val likeArrayList: ArrayList<String> = arrayListOf()
                likesList.forEach {
                    likeArrayList.add(it.trim())
                }
                txtLikes.setOnClickListener {
                    context.startActivity(
                        Intent(context, ViewUsersActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("usernameList", likeArrayList)
                    )
                }

            }
        }
    }

    inner class TypeTextPromotionViewHolder(
        val binding: PostTextPromotionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {

                Glide.with(context)
                    .load(posts[position].profileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile)

                val tags = posts[position].tags!!.replace("[", "").replace("]", "").split(",")
                    .filter { it.isNotEmpty() }

                txtUsername.text = if (tags.isEmpty()) {
                    posts[position].username
                } else {
                    if (tags.size >= 2)
                        posts[position].username + "," + tags[0] + " and ${tags.size - 1} others"
                    else
                        posts[position].username + " and " + tags[0]
                }

                txtProfession.text = posts[position].profession.toString()
                txtPost.text = posts[position].post
                txtComments.text = posts[position].comments.toString()
                txtShares.text = posts[position].shares.toString()

                val likesList =
                    posts[position].likesList!!.replace("[", "").replace("]", "").split(",")


                var likes = if (likesList[0].isEmpty())
                    0
                else
                    likesList.size


                txtLikes.text = likes.toString()

                var isLiked = false

                if (likesList.isNotEmpty()) {
                    likesList.forEach {
                        if (it.trim() == context.getUsername())
                            isLiked = true
                    }
                    if (isLiked)
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                } else {
                    imgLike.setImageResource(R.drawable.ic_diamond_white)
                }

                //hashtags
                val list = posts[position].hashTags!!.replace("[", "").replace("]", "").split(",")
                val hashTag:ArrayList<HashTag> = arrayListOf()
                list.forEach {
                    if (it.isNotBlank())
                        hashTag.add(HashTag(it.trim(),0))
                }
                val hashTagAdapter = HashTagAdapter(hashTag)

                rvHashtags.apply {
                    setHasFixedSize(true)
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = hashTagAdapter
                }

                hashTagAdapter.setOnHashTagClickListener {
                    context.startActivity(
                        Intent(context, PostHashTagActivity::class.java)
                            .putExtra("hashtag", it)
                    )
                }

                imgLike.setOnClickListener {
                    if (isLiked) {
                        isLiked = false
                        likes -= 1
                        txtLikes.text = (likes).toString()
                        imgLike.setImageResource(R.drawable.ic_diamond_white)
                    } else {
                        isLiked = true
                        likes += 1
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                        txtLikes.text = (likes).toString()
                    }
                    onLikeClickListener?.let {
                        it(posts[position])
                    }
                }

                imgComment.setOnClickListener {
                    onCommentClickListener?.let {
                        it(posts[position])
                    }
                }

                imgShare.setOnClickListener {
                    onShareClickListener?.let {
                        it(posts[position])
                    }
                }

                imgMore.setOnClickListener {
                    onMoreClickListener?.let {
                        it(posts[position])
                    }
                }

                txtUsername.setOnClickListener {
                    val tagList = arrayListOf<String>()
                    tags.forEach {
                        tagList.add(it.trim())
                    }
                    onUsernameClickListener?.let {
                        it(tagList, posts[position])
                    }
                }

                val likeArrayList: ArrayList<String> = arrayListOf()
                likesList.forEach {
                    likeArrayList.add(it.trim())
                }
                txtLikes.setOnClickListener {
                    context.startActivity(
                        Intent(context, ViewUsersActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("usernameList", likeArrayList)
                    )
                }

            }
        }
    }

    inner class TypeImageViewHolder(
        val binding: PostImageItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {

                Glide.with(context)
                    .load(posts[position].profileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile)

                Glide.with(context)
                    .load(posts[position].post)
                    .placeholder(R.drawable.image_place_holder)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)

                    .into(imgPost)

                val tags = posts[position].tags!!.replace("[", "").replace("]", "").split(",")
                    .filter { it.isNotEmpty() }

                txtUsername.text = if (tags.isEmpty()) {
                    posts[position].username
                } else {
                    if (tags.size >= 2)
                        posts[position].username + "," + tags[0] + " and ${tags.size - 1} others"
                    else
                        posts[position].username + " and " + tags[0]
                }

                txtProfession.text = posts[position].profession.toString()
                txtComments.text = posts[position].comments.toString()
                txtShares.text = posts[position].shares.toString()
                txtDate.text = getTimeAgo(posts[position].timestamp!!.toDate().time)
                txtDescription.text = posts[position].description

                val likesList =
                    posts[position].likesList!!.replace("[", "").replace("]", "").split(",")

                var likes = if (likesList[0].isEmpty())
                    0
                else
                    likesList.size
                txtLikes.text = likes.toString()
                var isLiked = false

                if (likesList.isNotEmpty()) {
                    likesList.forEach {
                        if (it.trim() == context.getUsername())
                            isLiked = true
                    }
                    if (isLiked)
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                } else {
                    imgLike.setImageResource(R.drawable.ic_diamond_white)
                }

                //hashtags
                val list = posts[position].hashTags!!.replace("[", "").replace("]", "").split(",")
                val hashTag:ArrayList<HashTag> = arrayListOf()
                list.forEach {
                    if (it.isNotBlank())
                        hashTag.add(HashTag(it.trim(),0))
                }
                val hashTagAdapter = HashTagAdapter(hashTag)

                rvHashtags.apply {
                    setHasFixedSize(true)
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = hashTagAdapter
                }

                hashTagAdapter.setOnHashTagClickListener {
                    context.startActivity(
                        Intent(context, PostHashTagActivity::class.java)
                            .putExtra("hashtag", it)
                    )
                }

                imgLike.setOnClickListener {
                    if (isLiked) {
                        likes -= 1
                        isLiked = false
                        txtLikes.text = (likes).toString()
                        imgLike.setImageResource(R.drawable.ic_diamond_white)
                    } else {
                        isLiked = true
                        likes += 1
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                        txtLikes.text = (likes).toString()
                    }
                    onLikeClickListener?.let {
                        it(posts[position])
                    }
                }

                imgComment.setOnClickListener {
                    onCommentClickListener?.let {
                        it(posts[position])
                    }
                }

                imgShare.setOnClickListener {
                    onShareClickListener?.let {
                        it(posts[position])
                    }
                }

                imgMore.setOnClickListener {
                    onMoreClickListener?.let {
                        it(posts[position])
                    }
                }

                imgPost.setOnClickListener {
                    onImageClickListener?.let {
                        it(posts[position])
                    }
                }

                txtUsername.setOnClickListener {
                    val tagList = arrayListOf<String>()
                    tags.forEach {
                        tagList.add(it.trim())
                    }
                    onUsernameClickListener?.let {
                        it(tagList, posts[position])
                    }
                }

                val likeArrayList: ArrayList<String> = arrayListOf()
                likesList.forEach {
                    likeArrayList.add(it.trim())
                }
                txtLikes.setOnClickListener {
                    context.startActivity(
                        Intent(context, ViewUsersActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("usernameList", likeArrayList)
                    )
                }

            }
        }
    }

    inner class TypeImagePromotionViewHolder(
        val binding: PostImagePromotionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {

                Glide.with(context)
                    .load(posts[position].profileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile)

                Glide.with(context)
                    .load(posts[position].post)
                    .placeholder(R.drawable.image_place_holder)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(imgPost)

                val tags = posts[position].tags!!.replace("[", "").replace("]", "").split(",")
                    .filter { it.isNotEmpty() }

                txtUsername.text = if (tags.isEmpty()) {
                    posts[position].username
                } else {
                    if (tags.size >= 2)
                        posts[position].username + "," + tags[0] + " and ${tags.size - 1} others"
                    else
                        posts[position].username + " and " + tags[0]
                }

                txtProfession.text = posts[position].profession.toString()
                txtComments.text = posts[position].comments.toString()
                txtShares.text = posts[position].shares.toString()
                txtDescription.text = posts[position].description


                val likesList =
                    posts[position].likesList!!.replace("[", "").replace("]", "").split(",")


                var likes = if (likesList[0].isEmpty())
                    0
                else
                    likesList.size
                txtLikes.text = likes.toString()

                var isLiked = false

                if (likesList.isNotEmpty()) {
                    likesList.forEach {
                        if (it.trim() == context.getUsername())
                            isLiked = true
                    }
                    if (isLiked)
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                } else {
                    imgLike.setImageResource(R.drawable.ic_diamond_white)
                }

                //hashtags
                val list = posts[position].hashTags!!.replace("[", "").replace("]", "").split(",")
                val hashTag:ArrayList<HashTag> = arrayListOf()
                list.forEach {
                    if (it.isNotBlank())
                        hashTag.add(HashTag(it.trim(),0))
                }
                val hashTagAdapter = HashTagAdapter(hashTag)

                rvHashtags.apply {
                    setHasFixedSize(true)
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = hashTagAdapter
                }

                hashTagAdapter.setOnHashTagClickListener {
                    context.startActivity(
                        Intent(context, PostHashTagActivity::class.java)
                            .putExtra("hashtag", it)
                    )
                }

                imgLike.setOnClickListener {
                    if (isLiked) {
                        likes -= 1
                        isLiked = false
                        txtLikes.text = (likes).toString()
                        imgLike.setImageResource(R.drawable.ic_diamond_white)
                    } else {
                        isLiked = true
                        likes += 1
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                        txtLikes.text = (likes).toString()
                    }
                    onLikeClickListener?.let {
                        it(posts[position])
                    }
                }

                imgComment.setOnClickListener {
                    onCommentClickListener?.let {
                        it(posts[position])
                    }
                }

                imgShare.setOnClickListener {
                    onShareClickListener?.let {
                        it(posts[position])
                    }
                }

                imgMore.setOnClickListener {
                    onMoreClickListener?.let {
                        it(posts[position])
                    }
                }

                imgPost.setOnClickListener {
                    onImageClickListener?.let {
                        it(posts[position])
                    }
                }

                txtUsername.setOnClickListener {
                    val tagList = arrayListOf<String>()
                    tags.forEach {
                        tagList.add(it.trim())
                    }
                    onUsernameClickListener?.let {
                        it(tagList, posts[position])
                    }
                }

                val likeArrayList: ArrayList<String> = arrayListOf()
                likesList.forEach {
                    likeArrayList.add(it.trim())
                }
                txtLikes.setOnClickListener {
                    context.startActivity(
                        Intent(context, ViewUsersActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("usernameList", likeArrayList)
                    )
                }

            }
        }
    }

    inner class TypeVideoViewHolder(
        val binding: PostVideoItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {

                Glide.with(context)
                    .load(posts[position].profileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile)


                val tags = posts[position].tags!!.replace("[", "").replace("]", "").split(",")
                    .filter { it.isNotEmpty() }

                txtUsername.text = if (tags.isEmpty()) {
                    posts[position].username
                } else {
                    if (tags.size >= 2)
                        posts[position].username + "," + tags[0] + " and ${tags.size - 1} others"
                    else
                        posts[position].username + " and " + tags[0]
                }


                txtProfession.text = posts[position].profession.toString()
                txtComments.text = posts[position].comments.toString()
                txtShares.text = posts[position].shares.toString()
                txtDate.text = getTimeAgo(posts[position].timestamp!!.toDate().time)
                txtDescription.text = posts[position].description


                val likesList =
                    posts[position].likesList!!.replace("[", "").replace("]", "").split(",")


                var likes = if (likesList[0].isEmpty())
                    0
                else
                    likesList.size
                txtLikes.text = likes.toString()

                var isLiked = false

                if (likesList.isNotEmpty()) {
                    likesList.forEach {
                        if (it.trim() == context.getUsername())
                            isLiked = true
                    }
                    if (isLiked)
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                } else {
                    imgLike.setImageResource(R.drawable.ic_diamond_white)
                }

                //hashtags
                val list = posts[position].hashTags!!.replace("[", "").replace("]", "").split(",")
                val hashTag:ArrayList<HashTag> = arrayListOf()
                list.forEach {
                    if (it.isNotBlank())
                        hashTag.add(HashTag(it.trim(),0))
                }
                val hashTagAdapter = HashTagAdapter(hashTag)
                rvHashtags.apply {
                    setHasFixedSize(true)
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = hashTagAdapter
                }

                hashTagAdapter.setOnHashTagClickListener {
                    context.startActivity(
                        Intent(context, PostHashTagActivity::class.java)
                            .putExtra("hashtag", it)
                    )
                }

                imgLike.setOnClickListener {
                    if (isLiked) {
                        likes -= 1
                        isLiked = false
                        txtLikes.text = (likes).toString()
                        imgLike.setImageResource(R.drawable.ic_diamond_white)
                    } else {
                        isLiked = true
                        likes += 1
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                        txtLikes.text = (likes).toString()
                    }
                    onLikeClickListener?.let {
                        it(posts[position])
                    }
                }

                imgComment.setOnClickListener {
                    onCommentClickListener?.let {
                        it(posts[position])
                    }
                }

                imgShare.setOnClickListener {
                    onShareClickListener?.let {
                        it(posts[position])
                    }
                }

                imgMore.setOnClickListener {
                    onMoreClickListener?.let {
                        it(posts[position])
                    }
                }

                videoPost.videoSurfaceView?.setOnClickListener {
                    onVideoClickListener?.let {
                        it(posts[position])
                    }
                }

                txtUsername.setOnClickListener {
                    val tagList = arrayListOf<String>()
                    tags.forEach {
                        tagList.add(it.trim())
                    }
                    onUsernameClickListener?.let {
                        it(tagList, posts[position])
                    }
                }
                val likeArrayList: ArrayList<String> = arrayListOf()
                likesList.forEach {
                    likeArrayList.add(it.trim())
                }
                txtLikes.setOnClickListener {
                    context.startActivity(
                        Intent(context, ViewUsersActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("usernameList", likeArrayList)
                    )
                }

            }
        }
    }

    inner class TypeVideoPromotionViewHolder(
        val binding: PostVideoPromotionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {

                Glide.with(context)
                    .load(posts[position].profileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile)


                val tags = posts[position].tags!!.replace("[", "").replace("]", "").split(",")
                    .filter { it.isNotEmpty() }

                txtUsername.text = if (tags.isEmpty()) {
                    posts[position].username
                } else {
                    if (tags.size >= 2)
                        posts[position].username + "," + tags[0] + " and ${tags.size - 1} others"
                    else
                        posts[position].username + " and " + tags[0]
                }

                txtProfession.text = posts[position].profession.toString()
                txtComments.text = posts[position].comments.toString()
                txtShares.text = posts[position].shares.toString()
                txtDescription.text = posts[position].description

                val likesList =
                    posts[position].likesList!!.replace("[", "").replace("]", "").split(",")


                var likes = if (likesList[0].isEmpty())
                    0
                else
                    likesList.size
                txtLikes.text = likes.toString()

                var isLiked = false

                if (likesList.isNotEmpty()) {
                    likesList.forEach {
                        if (it.trim() == context.getUsername())
                            isLiked = true
                    }
                    if (isLiked)
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                } else {
                    imgLike.setImageResource(R.drawable.ic_diamond_white)
                }

                //hashtags
                val list = posts[position].hashTags!!.replace("[", "").replace("]", "").split(",")
                val hashTag:ArrayList<HashTag> = arrayListOf()
                list.forEach {
                    if (it.isNotBlank())
                        hashTag.add(HashTag(it.trim(),0))
                }
                val hashTagAdapter = HashTagAdapter(hashTag)

                rvHashtags.apply {
                    setHasFixedSize(true)
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = hashTagAdapter
                }

                hashTagAdapter.setOnHashTagClickListener {
                    context.startActivity(
                        Intent(context, PostHashTagActivity::class.java)
                            .putExtra("hashtag", it)
                    )
                }


                imgLike.setOnClickListener {
                    if (isLiked) {
                        isLiked = false
                        likes -= 1
                        txtLikes.text = (likes).toString()
                        imgLike.setImageResource(R.drawable.ic_diamond_white)
                    } else {
                        isLiked = true
                        likes += 1
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                        txtLikes.text = (likes).toString()
                    }
                    onLikeClickListener?.let {
                        it(posts[position])
                    }
                }

                imgComment.setOnClickListener {
                    onCommentClickListener?.let {
                        it(posts[position])
                    }
                }

                imgShare.setOnClickListener {
                    onShareClickListener?.let {
                        it(posts[position])
                    }
                }

                imgMore.setOnClickListener {
                    onMoreClickListener?.let {
                        it(posts[position])
                    }
                }

                videoPost.videoSurfaceView?.setOnClickListener {
                    onVideoClickListener?.let {
                        it(posts[position])
                    }
                }

                txtUsername.setOnClickListener {
                    val tagList = arrayListOf<String>()
                    tags.forEach {
                        tagList.add(it.trim())
                    }
                    onUsernameClickListener?.let {
                        it(tagList, posts[position])
                    }
                }

                val likeArrayList: ArrayList<String> = arrayListOf()
                likesList.forEach {
                    likeArrayList.add(it.trim())
                }
                txtLikes.setOnClickListener {
                    context.startActivity(
                        Intent(context, ViewUsersActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("usernameList", likeArrayList)
                    )
                }

            }
        }
    }

    fun setData(newList: List<Posts>) {
        val diffUtil = MyDiffUtil(posts, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        posts.clear()
        posts.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun onLikeClickListener(listener: (Posts) -> Unit) {
        onLikeClickListener = listener
    }

    fun onCommentClickListener(listener: (Posts) -> Unit) {
        onCommentClickListener = listener
    }

    fun onShareClickListener(listener: (Posts) -> Unit) {
        onShareClickListener = listener
    }

    fun onMoreClickListener(listener: (Posts) -> Unit) {
        onMoreClickListener = listener
    }

    fun onImageClickListener(listener: (Posts) -> Unit) {
        onImageClickListener = listener
    }

    fun onVideoClickListener(listener: (Posts) -> Unit) {
        onVideoClickListener = listener
    }

    fun onUsernameClickListener(listener: (List<String>, Posts) -> Unit) {
        onUsernameClickListener = listener
    }



    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is TypeVideoViewHolder) {
            if (holder.binding.videoPost.player == null) {
                val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true)
                DefaultDataSource.Factory(
                    context,
                    httpDataSourceFactory
                )
                val cacheDataSourceFactory = CacheDataSource.Factory()
                    .setCache(UTripodApp.simpleCache)
                    .setUpstreamDataSourceFactory(httpDataSourceFactory)
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                val exoPlayer = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
                    .setLoadControl(AppUtil.getLoadControl())
                    .build()

                exoPlayer.addMediaItem(MediaItem.fromUri(posts[holder.absoluteAdapterPosition].post.toString()))
                exoPlayer.volume = 0F
                exoPlayer.repeatMode = REPEAT_MODE_ALL
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                holder.binding.videoPost.player = null
                holder.binding.videoPost.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                exoPlayer.videoScalingMode = VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

                holder.binding.videoPost.player = exoPlayer

                exoplayerList.add(exoPlayer)

                exoPlayer.addAnalyticsListener(object : AnalyticsListener {
                    override fun onVideoSizeChanged(
                        eventTime: AnalyticsListener.EventTime,
                        width: Int,
                        height: Int,
                        unappliedRotationDegrees: Int,
                        pixelWidthHeightRatio: Float
                    ) {
                        val layoutParams = holder.binding.videoPost.layoutParams
                        layoutParams.width = 850
                        layoutParams.height = height
                        if (layoutParams.height >= 1000)
                            layoutParams.height = 900
                        holder.binding.videoPost.layoutParams = layoutParams
                    }
                })
            }

        }
        if (holder is TypeVideoPromotionViewHolder) {
            if (holder.binding.videoPost.player == null) {
                val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true)
                DefaultDataSource.Factory(
                    context,
                    httpDataSourceFactory
                )
                val cacheDataSourceFactory = CacheDataSource.Factory()
                    .setCache(UTripodApp.simpleCache)
                    .setUpstreamDataSourceFactory(httpDataSourceFactory)
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                val exoPlayer = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
                    .setLoadControl(AppUtil.getLoadControl())
                    .build()

                exoPlayer.addMediaItem(MediaItem.fromUri(posts[holder.absoluteAdapterPosition].post.toString()))
                exoPlayer.volume = 0F
                exoPlayer.repeatMode = REPEAT_MODE_ALL
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                holder.binding.videoPost.player = null
                holder.binding.videoPost.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                exoPlayer.videoScalingMode = VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

                holder.binding.videoPost.player = exoPlayer

                exoplayerList.add(exoPlayer)

                exoPlayer.addAnalyticsListener(object : AnalyticsListener {
                    override fun onVideoSizeChanged(
                        eventTime: AnalyticsListener.EventTime,
                        width: Int,
                        height: Int,
                        unappliedRotationDegrees: Int,
                        pixelWidthHeightRatio: Float
                    ) {
                        val layoutParams = holder.binding.videoPost.layoutParams
                        layoutParams.width = 850
                        layoutParams.height = height
                        if (layoutParams.height >= 1000)
                            layoutParams.height = 900
                        holder.binding.videoPost.layoutParams = layoutParams
                    }
                })
            }
        }
    }

    fun releaseExoplayer() {
        exoplayerList.forEach {
            it.release()
        }
    }
}