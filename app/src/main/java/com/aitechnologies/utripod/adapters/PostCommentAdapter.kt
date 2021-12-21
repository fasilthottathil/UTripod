package com.aitechnologies.utripod.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.databinding.PostCommentGiphItemBinding
import com.aitechnologies.utripod.databinding.PostCommentTextItemBinding
import com.aitechnologies.utripod.models.PostComment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class PostCommentAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MyDiffUtil(
        private val oldList: List<PostComment>,
        private val newList: List<PostComment>
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
        const val TYPE_TEXT = 0
        const val TYPE_GIPH = 1
    }

    private var postComment: ArrayList<PostComment> = arrayListOf()

    private var onReplyClickListener: ((PostComment) -> Unit)? = null
    private var onMoreClickListener: ((PostComment, View, Int) -> Unit)? = null
    private var onUserClickListener: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT -> TypeTextHolder(
                PostCommentTextItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            else -> TypeGiphHolder(
                PostCommentGiphItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (postComment[position].viewType) {
            TYPE_TEXT -> (holder as TypeTextHolder).bind(position)
            TYPE_GIPH -> (holder as TypeGiphHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return postComment.size
    }

    override fun getItemViewType(position: Int): Int {
        return postComment[position].viewType
    }

    fun setData(newList: List<PostComment>) {
        val diffUtil = MyDiffUtil(postComment, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        postComment.clear()
        postComment.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class TypeTextHolder(val binding: PostCommentTextItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/utripod-c5add.appspot.com/o/profile%2F${postComment[position].username}?alt=media")
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile)
                txtUsername.text = postComment[position].username
                txtComment.text = postComment[position].comment
                if (postComment[position].replies != 0)
                    "View ${postComment[position].replies} replies".also { txtReply.text = it }
                txtReply.setOnClickListener {
                    onReplyClickListener?.let {
                        it(postComment[position])
                    }
                }
                imgMore.setOnClickListener { view ->
                    onMoreClickListener?.let {
                        it(postComment[absoluteAdapterPosition], view, absoluteAdapterPosition)
                    }
                }
                imgProfile.setOnClickListener {
                    onUserClickListener?.invoke(postComment[position].username)
                }
                txtUsername.setOnClickListener {
                    onUserClickListener?.invoke(postComment[position].username)
                }
            }
        }
    }

    inner class TypeGiphHolder(val binding: PostCommentGiphItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/utripod-c5add.appspot.com/o/profile%2F${postComment[position].username}?alt=media")
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile)
                txtUsername.text = postComment[position].username
                gifView.setMediaWithId(postComment[position].comment)
                if (postComment[position].replies != 0)
                    "View ${postComment[position].replies} replies".also { txtReply.text = it }
                txtReply.setOnClickListener {
                    onReplyClickListener?.let {
                        it(postComment[position])
                    }
                }
                imgMore.setOnClickListener { view ->
                    onMoreClickListener?.let {
                        it(postComment[absoluteAdapterPosition], view, absoluteAdapterPosition)
                    }
                }
                imgProfile.setOnClickListener {
                    onUserClickListener?.invoke(postComment[position].username)
                }
                txtUsername.setOnClickListener {
                    onUserClickListener?.invoke(postComment[position].username)
                }
            }
        }
    }

    fun setOnReplyClickListener(listener: ((PostComment) -> Unit)) {
        onReplyClickListener = listener
    }

    fun setOnMoreClickListener(listener: ((PostComment, View, Int) -> Unit)) {
        onMoreClickListener = listener
    }

    fun onUserClickListener(listener: ((String) -> Unit)) {
        onUserClickListener = listener
    }

}