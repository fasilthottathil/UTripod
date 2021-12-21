package com.aitechnologies.utripod.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.UvisItemBinding
import com.aitechnologies.utripod.models.HashTag
import com.aitechnologies.utripod.models.UvisModel
import com.aitechnologies.utripod.ui.activities.ViewUsersActivity
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.getTimeAgo
import com.aitechnologies.utripod.uvis.activities.UvisHashTagActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UvisAdapter(
    private val context: Context,
    private var followingList: ArrayList<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MyDiffUtil(
        private val oldList: List<UvisModel>,
        private val newList: List<UvisModel>
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

    companion object {
        const val TYPE_UVIS = 0
        const val TYPE_UVIS_PROMOTION = 1
    }

    private var uvis: ArrayList<UvisModel> = arrayListOf()

    private var onUvisAttachedToWindowListener: ((TypeUvis) -> Unit)? = null
    private var onUvisPromotionAttachedToWindowListener: ((TypeUvisPromotion) -> Unit)? = null
    private var onLikeClickListener: ((UvisModel) -> Unit)? = null
    private var onMoreClickListener: ((UvisModel) -> Unit)? = null
    private var onCommentClickListener: ((UvisModel) -> Unit)? = null
    private var onUsernameClickListener: ((UvisModel) -> Unit)? = null
    private var onProfileImageClickListener: ((UvisModel) -> Unit)? = null
    private var onMusicClickListener: ((String) -> Unit)? = null
    private var onPlayerClickListener:((Int,ImageView)->Unit)? = null
    private var onFollowClickListener:((String)->Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_UVIS -> TypeUvis(
                UvisItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> TypeUvisPromotion(
                UvisItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (uvis[position].viewType) {
            TYPE_UVIS -> (holder as TypeUvis).bind(position)
            TYPE_UVIS_PROMOTION -> (holder as TypeUvisPromotion).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return uvis.size
    }

    override fun getItemViewType(position: Int): Int {
        return uvis[position].viewType!!
    }

    inner class TypeUvis(val binding: UvisItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtProfession.text = uvis[position].profession
                txtUsername.text = uvis[position].username
                txtDescription.text = uvis[position].description
                txtDate.text = getTimeAgo(uvis[position].timestamp!!.toDate().time)
                txtComments.text = uvis[position].comments.toString()

                Glide.with(context)
                    .load(uvis[position].profileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageProfile)

                val likesList =
                    uvis[position].likesList!!.replace("[", "").replace("]", "").split(",")

                var likes = if (likesList[0].isEmpty())
                    0
                else
                    likesList.size

                txtLikes.text = likes.toString()

                var isLiked = false

                if (likesList.isNotEmpty()) {
                    if (isLiked(likesList)) {
                        isLiked = true
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                    }
                } else {
                    imgLike.setImageResource(R.drawable.ic_diamond_white)
                }

                //hashtags
                val list = uvis[position].hashTags!!.replace("[", "").replace("]", "").split(",")
                val hashTag:ArrayList<HashTag> = arrayListOf()
                list.forEach {
                    if (it.isNotBlank())
                        hashTag.add(HashTag(it.trim(),0))
                }
                val hashTagAdapter = HashTagAdapter(hashTag)
                rvTags.apply {
                    setHasFixedSize(true)
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = hashTagAdapter
                }

                hashTagAdapter.setOnHashTagClickListener {
                    context.startActivity(
                        Intent(context, UvisHashTagActivity::class.java)
                            .putExtra("hashtag", it)
                    )
                }

                imgComment.setOnClickListener {
                    onCommentClickListener?.let {
                        it(uvis[position])
                    }
                }

                imgLike.setOnClickListener {
                    if (isLiked) {
                        isLiked = false
                        likes -= 1
                        txtLikes.text = (likes).toString()
                        imgLike.setImageResource(R.drawable.ic_diamond_white)

                    } else {
                        isLiked = true
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                        likes += 1
                        txtLikes.text = (likes).toString()
                    }
                    onLikeClickListener?.let {
                        it(uvis[position])
                    }
                }

                imgMore.setOnClickListener {
                    onMoreClickListener?.let {
                        it(uvis[position])
                    }
                }

                txtUsername.setOnClickListener {
                    onUsernameClickListener?.let {
                        it(uvis[position])
                    }
                }

                imageProfile.setOnClickListener {
                    onProfileImageClickListener?.let {
                        it(uvis[position])
                    }
                }

                imgMusic.setOnClickListener {
                    onMusicClickListener?.let {
                        it(uvis[position].url.toString())
                    }
                }

                var isDoubleTaped = false
                val animationUtils = AnimationUtils.loadAnimation(context,R.anim.bounce)
                player.setOnClickListener {
                    if (binding.imgPlay.visibility == VISIBLE){
                        binding.imgPlay.visibility = INVISIBLE
                    }else{
                        binding.imgPlay.visibility = VISIBLE
                    }
                    onPlayerClickListener?.let {
                        it(uvis[position].viewType!!,imgPlay)
                    }

                    if (isDoubleTaped){
                        if (isLiked) {
                            isLiked = false
                            likes -= 1
                            txtLikes.text = (likes).toString()
                            imgLike.setImageResource(R.drawable.ic_diamond_white)
                        } else {
                            isLiked = true
                            imgLike.setImageResource(R.drawable.ic_diamond_blue)
                            likes += 1
                            txtLikes.text = (likes).toString()
                        }
                        onLikeClickListener?.let {
                            it(uvis[position])
                        }
                        isDoubleTaped = false
                        imgDiamond.startAnimation(animationUtils)
                    }
                    isDoubleTaped = true

                    GlobalScope.launch {
                        delay(300)
                        isDoubleTaped = false
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

                //follow
                if (followingList.contains(context.getUsername())){
                    "Unfollow".also {
                        binding.txtFollow.text = it
                    }
                    binding.txtFollow.setBackgroundResource(R.drawable.unfollow_button_profile)
                }else{
                    "Follow".also {
                        binding.txtFollow.text = it
                    }
                    binding.txtFollow.setBackgroundResource(R.drawable.follow_button_profile)
                }

                binding.txtFollow.setOnClickListener {
                    if (binding.txtFollow.text.toString() == "Follow"){
                        "Unfollow".also {
                            binding.txtFollow.text = it
                        }
                        binding.txtFollow.setBackgroundResource(R.drawable.unfollow_button_profile)
                    }else{
                        "Follow".also {
                            binding.txtFollow.text = it
                        }
                        binding.txtFollow.setBackgroundResource(R.drawable.follow_button_profile)
                    }
                    onFollowClickListener?.invoke(uvis[position].username.toString())
                }

            }
        }
    }

    inner class TypeUvisPromotion(val binding: UvisItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtProfession.text = uvis[position].profession
                txtUsername.text = uvis[position].username
                txtDescription.text = uvis[position].description
                txtDate.text = context.getString(R.string.promoted)
                txtDate.setTextColor(context.getColor(R.color.active))
                txtComments.text = uvis[position].comments.toString()

                Glide.with(context)
                    .load(uvis[position].profileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageProfile)

                val likesList =
                    uvis[position].likesList!!.replace("[", "").replace("]", "").split(",")

                var likes = if (likesList[0].isEmpty())
                    0
                else
                    likesList.size

                txtLikes.text = likes.toString()

                var isLiked = false

                if (likesList.isNotEmpty()) {
                    if (isLiked(likesList)) {
                        isLiked = true
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)

                    }
                } else {
                    imgLike.setImageResource(R.drawable.ic_diamond_white)
                }

                //hashtags
                val list = uvis[position].hashTags!!.replace("[", "").replace("]", "").split(",")
                val hashTag:ArrayList<HashTag> = arrayListOf()
                list.forEach {
                    if (it.isNotBlank())
                        hashTag.add(HashTag(it.trim(),0))
                }
                val hashTagAdapter = HashTagAdapter(hashTag)
                rvTags.apply {
                    setHasFixedSize(true)
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = hashTagAdapter
                }

                hashTagAdapter.setOnHashTagClickListener {
                    context.startActivity(
                        Intent(context, UvisHashTagActivity::class.java)
                            .putExtra("hashtag", it)
                    )
                }

                imgComment.setOnClickListener {
                    onCommentClickListener?.let {
                        it(uvis[position])
                    }
                }

                imgLike.setOnClickListener {
                    if (isLiked) {
                        isLiked = false
                        likes += 1
                        txtLikes.text = (likes).toString()
                        imgLike.setImageResource(R.drawable.ic_diamond_white)
                    } else {
                        isLiked = true
                        imgLike.setImageResource(R.drawable.ic_diamond_blue)
                        likes -= 1
                        txtLikes.text = (likes).toString()
                    }
                    onLikeClickListener?.let {
                        it(uvis[position])
                    }
                }

                imgMore.setOnClickListener {
                    onMoreClickListener?.let {
                        it(uvis[position])
                    }
                }

                txtUsername.setOnClickListener {
                    onUsernameClickListener?.let {
                        it(uvis[position])
                    }
                }

                imageProfile.setOnClickListener {
                    onProfileImageClickListener?.let {
                        it(uvis[position])
                    }
                }

                imgMusic.setOnClickListener {
                    onMusicClickListener?.let {
                        it(uvis[position].url.toString())
                    }
                }



                var isDoubleTaped = false
                val animationUtils = AnimationUtils.loadAnimation(context,R.anim.bounce)
                player.setOnClickListener {
                    if (binding.imgPlay.visibility == VISIBLE){
                        binding.imgPlay.visibility = INVISIBLE
                    }else{
                        binding.imgPlay.visibility = VISIBLE
                    }
                    onPlayerClickListener?.let {
                        it(uvis[position].viewType!!,imgPlay)
                    }

                    if (isDoubleTaped){
                        if (isLiked) {
                            isLiked = false
                            likes -= 1
                            txtLikes.text = (likes).toString()
                            imgLike.setImageResource(R.drawable.ic_diamond_white)
                        } else {
                            isLiked = true
                            imgLike.setImageResource(R.drawable.ic_diamond_blue)
                            likes += 1
                            txtLikes.text = (likes).toString()
                        }
                        onLikeClickListener?.let {
                            it(uvis[position])
                        }
                        isDoubleTaped = false
                        imgDiamond.startAnimation(animationUtils)
                    }

                    isDoubleTaped = true

                    GlobalScope.launch {
                        delay(300)
                        isDoubleTaped = false
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

                //follow
                if (followingList.contains(context.getUsername())){
                   "Unfollow".also {
                       binding.txtFollow.text = it
                   }
                    binding.txtFollow.setBackgroundResource(R.drawable.unfollow_button_profile)
                }else{
                    "Follow".also {
                        binding.txtFollow.text = it
                    }
                    binding.txtFollow.setBackgroundResource(R.drawable.follow_button_profile)
                }

                binding.txtFollow.setOnClickListener {
                    if (binding.txtFollow.text.toString() == "Follow"){
                        "Unfollow".also {
                            binding.txtFollow.text = it
                        }
                        binding.txtFollow.setBackgroundResource(R.drawable.unfollow_button_profile)
                    }else{
                        "Follow".also {
                            binding.txtFollow.text = it
                        }
                        binding.txtFollow.setBackgroundResource(R.drawable.follow_button_profile)
                    }
                    onFollowClickListener?.invoke(uvis[position].username.toString())
                }

            }
        }
    }

    fun setData(newList: List<UvisModel>) {
        val diffUtil = MyDiffUtil(uvis, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        uvis.clear()
        uvis.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }


    fun setOnUvisAttachedToWindowListener(listener: (TypeUvis) -> Unit) {
        onUvisAttachedToWindowListener = listener
    }

    fun setOnUvisPromotionAttachedToWindowListener(listener: (TypeUvisPromotion) -> Unit) {
        onUvisPromotionAttachedToWindowListener = listener
    }

    fun setOnLikeClickListener(listener: (UvisModel) -> Unit) {
        onLikeClickListener = listener
    }


    fun setOnMoreClickListener(listener: (UvisModel) -> Unit) {
        onMoreClickListener = listener
    }

    fun setOnCommentClickListener(listener: (UvisModel) -> Unit) {
        onCommentClickListener = listener
    }

    fun setOnProfileImageClickListener(listener: (UvisModel) -> Unit) {
        onProfileImageClickListener = listener
    }

    fun setOnUsernameClickListener(listener: (UvisModel) -> Unit) {
        onUsernameClickListener = listener
    }

    fun setOnMusicClickListener(listener: (String) -> Unit) {
        onMusicClickListener = listener
    }

    fun setOnPlayerClickListener(listener: (Int,ImageView) -> Unit) {
        onPlayerClickListener = listener
    }

    fun setOnFollowClickListener(listener: (String) -> Unit) {
        onFollowClickListener = listener
    }

    private fun isLiked(likesList: List<String>): Boolean {
        var isLiked = false
        likesList.forEach {
            if (it.trim() == context.getUsername())
                isLiked = true
        }
        return isLiked
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is TypeUvis)
            onUvisAttachedToWindowListener?.let {
                it(holder)
            }
        if (holder is TypeUvisPromotion)
            onUvisPromotionAttachedToWindowListener?.let {
                it(holder)
            }
    }

}