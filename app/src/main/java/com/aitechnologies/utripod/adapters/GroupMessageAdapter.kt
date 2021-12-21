package com.aitechnologies.utripod.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.*
import com.aitechnologies.utripod.models.GroupMessage
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.getTimeAgo
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class GroupMessageAdapter(
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MyDiffUtil(
        private val oldList: List<GroupMessage>,
        private val newList: List<GroupMessage>
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
        const val TYPE_TEXT_SEND = 0
        const val TYPE_TEXT_RECEIVE = 1
        const val TYPE_IMAGE_SEND = 2
        const val TYPE_IMAGE_RECEIVE = 3
        const val TYPE_VIDEO_SEND = 4
        const val TYPE_VIDEO_RECEIVE = 5
    }

    private val groupMessage: ArrayList<GroupMessage> = arrayListOf()

    private var onImageClickListener: ((GroupMessage) -> Unit)? = null
    private var onVideoClickListener: ((GroupMessage) -> Unit)? = null
    private var onProfileImageClickListener: ((String) -> Unit)? = null
    private var onUserNameClickListener: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT_SEND -> TypeTextSend(
                GroupChatTextSendItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            TYPE_TEXT_RECEIVE -> TypeTextReceive(
                GroupChatTextRecieveItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            TYPE_IMAGE_SEND -> TypeImageSend(
                GroupChatImageSendItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            TYPE_IMAGE_RECEIVE -> TypeTextReceive(
                GroupChatTextRecieveItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            TYPE_VIDEO_SEND -> TypeVideoSend(
                GroupChatVideoSendItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            else -> TypeVideoReceive(
                GroupChatVideoRecieveItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (groupMessage[position].viewType) {
            TYPE_TEXT_SEND -> (holder as TypeTextSend).bind(position)
            TYPE_TEXT_RECEIVE -> (holder as TypeTextReceive).bind(position)
            TYPE_IMAGE_SEND -> (holder as TypeImageSend).bind(position)
            TYPE_IMAGE_RECEIVE -> (holder as TypeImageReceive).bind(position)
            TYPE_VIDEO_SEND -> (holder as TypeVideoSend).bind(position)
            TYPE_VIDEO_RECEIVE -> (holder as TypeVideoReceive).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return groupMessage.size
    }

    override fun getItemViewType(position: Int): Int {
        val username = context.getUsername()
        val viewType = when (groupMessage[position].type) {
            0 -> {
                if (groupMessage[position].username == username)
                    TYPE_TEXT_SEND
                else
                    TYPE_TEXT_RECEIVE
            }
            1 -> {
                if (groupMessage[position].username == username)
                    TYPE_IMAGE_SEND
                else
                    TYPE_IMAGE_RECEIVE
            }
            else -> {
                if (groupMessage[position].username == username)
                    TYPE_VIDEO_SEND
                else
                    TYPE_VIDEO_RECEIVE
            }
        }
        groupMessage[position].viewType = viewType
        return viewType
    }

    inner class TypeTextSend(val binding: GroupChatTextSendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtMessage.text = groupMessage[position].message
                txtDate.text = getTimeAgo(groupMessage[position].timestamp.toDate().time)
            }
        }
    }

    inner class TypeTextReceive(val binding: GroupChatTextRecieveItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtMessage.text = groupMessage[position].message
                txtDate.text = getTimeAgo(groupMessage[position].timestamp.toDate().time)
                txtUsername.text = groupMessage[position].username

                Glide.with(context)
                    .load(groupMessage[position].profileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(imgProfile)

                txtUsername.setOnClickListener {
                    onUserNameClickListener?.let {
                        it(groupMessage[position].username)
                    }
                }

                imgProfile.setOnClickListener {
                    onProfileImageClickListener?.let {
                        it(groupMessage[position].username)
                    }
                }

            }
        }
    }

    inner class TypeImageSend(val binding: GroupChatImageSendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtDate.text = getTimeAgo(groupMessage[position].timestamp.toDate().time)

                Glide.with(context)
                    .load(groupMessage[position].message)
                    .into(imgMessage)


                imgMessage.setOnClickListener {
                    onImageClickListener?.let {
                        it(groupMessage[position])
                    }
                }

            }
        }
    }

    inner class TypeImageReceive(val binding: GroupChatImageRecieveItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtDate.text = getTimeAgo(groupMessage[position].timestamp.toDate().time)
                txtUsername.text = groupMessage[position].username

                Glide.with(context)
                    .load(groupMessage[position].profileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(imgProfile)

                Glide.with(context)
                    .load(groupMessage[position].message)
                    .into(imgMessage)

                txtUsername.setOnClickListener {
                    onUserNameClickListener?.let {
                        it(groupMessage[position].username)
                    }
                }

                imgProfile.setOnClickListener {
                    onProfileImageClickListener?.let {
                        it(groupMessage[position].username)
                    }
                }

                imgMessage.setOnClickListener {
                    onImageClickListener?.let {
                        it(groupMessage[position])
                    }
                }


            }
        }
    }

    inner class TypeVideoSend(val binding: GroupChatVideoSendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtDate.text = getTimeAgo(groupMessage[position].timestamp.toDate().time)

                Glide.with(context)
                    .load(groupMessage[position].message)
                    .into(imgMessage)


                imgMessage.setOnClickListener {
                    onVideoClickListener?.let {
                        it(groupMessage[position])
                    }
                }

            }
        }
    }

    inner class TypeVideoReceive(val binding: GroupChatVideoRecieveItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtDate.text = getTimeAgo(groupMessage[position].timestamp.toDate().time)
                txtUsername.text = groupMessage[position].username

                Glide.with(context)
                    .load(groupMessage[position].profileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(imgProfile)

                Glide.with(context)
                    .load(groupMessage[position].message)
                    .into(imgMessage)

                txtUsername.setOnClickListener {
                    onUserNameClickListener?.let {
                        it(groupMessage[position].username)
                    }
                }

                imgProfile.setOnClickListener {
                    onProfileImageClickListener?.let {
                        it(groupMessage[position].username)
                    }
                }

                imgMessage.setOnClickListener {
                    onVideoClickListener?.let {
                        it(groupMessage[position])
                    }
                }


            }
        }
    }

    fun setData(newList: List<GroupMessage>) {
        val diffUtil = MyDiffUtil(groupMessage, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        groupMessage.clear()
        groupMessage.addAll(newList.sortedBy { it.timestamp })
        diffResult.dispatchUpdatesTo(this)
    }

    fun setOnImageClickListener(listener: (GroupMessage) -> Unit) {
        onImageClickListener = listener
    }

    fun setOnVideoClickListener(listener: (GroupMessage) -> Unit) {
        onVideoClickListener = listener
    }

    fun setOnProfileImageClickListener(listener: (String) -> Unit) {
        onProfileImageClickListener = listener
    }

    fun setOnUsernameClickListener(listener: (String) -> Unit) {
        onUserNameClickListener = listener
    }

}