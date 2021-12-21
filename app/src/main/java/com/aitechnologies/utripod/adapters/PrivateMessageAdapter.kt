package com.aitechnologies.utripod.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.databinding.*
import com.aitechnologies.utripod.models.PrivateMessage
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.getTimeAgo
import com.bumptech.glide.Glide

class PrivateMessageAdapter(
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MyDiffUtil(
        private val oldList: List<PrivateMessage>,
        private val newList: List<PrivateMessage>
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

    private val privateMessage: ArrayList<PrivateMessage> = arrayListOf()

    private var onImageClickListener: ((PrivateMessage) -> Unit)? = null
    private var onVideoClickListener: ((PrivateMessage) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT_SEND -> TypeTextSend(
                PrivateChatTextSendItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            TYPE_TEXT_RECEIVE -> TypeTextReceive(
                PrivateChatTextRecieveItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            TYPE_IMAGE_SEND -> TypeImageSend(
                PrivateChatImageSendItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            TYPE_IMAGE_RECEIVE -> TypeTextReceive(
                PrivateChatTextRecieveItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            TYPE_VIDEO_SEND -> TypeVideoSend(
                PrivateChatVideoSendItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            else -> TypeVideoReceive(
                PrivateChatVideoRecieveItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (privateMessage[position].viewType) {
            TYPE_TEXT_SEND -> (holder as TypeTextSend).bind(position)
            TYPE_TEXT_RECEIVE -> (holder as TypeTextReceive).bind(position)
            TYPE_IMAGE_SEND -> (holder as TypeImageSend).bind(position)
            TYPE_IMAGE_RECEIVE -> (holder as TypeImageReceive).bind(position)
            TYPE_VIDEO_SEND -> (holder as TypeVideoSend).bind(position)
            TYPE_VIDEO_RECEIVE -> (holder as TypeVideoReceive).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return privateMessage.size
    }

    override fun getItemViewType(position: Int): Int {
        val username = context.getUsername()
        val viewType = when (privateMessage[position].type) {
            0 -> {
                if (privateMessage[position].username == username)
                    TYPE_TEXT_SEND
                else
                    TYPE_TEXT_RECEIVE
            }
            1 -> {
                if (privateMessage[position].username == username)
                    TYPE_IMAGE_SEND
                else
                    TYPE_IMAGE_RECEIVE
            }
            else -> {
                if (privateMessage[position].username == username)
                    TYPE_VIDEO_SEND
                else
                    TYPE_VIDEO_RECEIVE
            }
        }
        privateMessage[position].viewType = viewType
        return viewType
    }

    inner class TypeTextSend(val binding: PrivateChatTextSendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtMessage.text = privateMessage[position].message
                txtDate.text = getTimeAgo(privateMessage[position].timestamp.toDate().time)
            }
        }
    }

    inner class TypeTextReceive(val binding: PrivateChatTextRecieveItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtMessage.text = privateMessage[position].message
                txtDate.text = getTimeAgo(privateMessage[position].timestamp.toDate().time)



            }
        }
    }

    inner class TypeImageSend(val binding: PrivateChatImageSendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtDate.text = getTimeAgo(privateMessage[position].timestamp.toDate().time)

                Glide.with(context)
                    .load(privateMessage[position].message)
                    .into(imgMessage)


                imgMessage.setOnClickListener {
                    onImageClickListener?.let {
                        it(privateMessage[position])
                    }
                }

            }
        }
    }

    inner class TypeImageReceive(val binding: PrivateChatImageRecieveItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtDate.text = getTimeAgo(privateMessage[position].timestamp.toDate().time)

                Glide.with(context)
                    .load(privateMessage[position].message)
                    .into(imgMessage)


                imgMessage.setOnClickListener {
                    onImageClickListener?.let {
                        it(privateMessage[position])
                    }
                }


            }
        }
    }

    inner class TypeVideoSend(val binding: PrivateChatVideoSendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtDate.text = getTimeAgo(privateMessage[position].timestamp.toDate().time)

                Glide.with(context)
                    .load(privateMessage[position].message)
                    .into(imgMessage)


                imgMessage.setOnClickListener {
                    onVideoClickListener?.let {
                        it(privateMessage[position])
                    }
                }

            }
        }
    }

    inner class TypeVideoReceive(val binding: PrivateChatVideoRecieveItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtDate.text = getTimeAgo(privateMessage[position].timestamp.toDate().time)


                Glide.with(context)
                    .load(privateMessage[position].message)
                    .into(imgMessage)


                imgMessage.setOnClickListener {
                    onVideoClickListener?.let {
                        it(privateMessage[position])
                    }
                }


            }
        }
    }

    fun setData(newList: List<PrivateMessage>) {
        val diffUtil = MyDiffUtil(privateMessage, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        privateMessage.clear()
        privateMessage.addAll(newList.sortedBy { it.timestamp })
        diffResult.dispatchUpdatesTo(this)
    }

    fun setOnImageClickListener(listener: (PrivateMessage) -> Unit) {
        onImageClickListener = listener
    }

    fun setOnVideoClickListener(listener: (PrivateMessage) -> Unit) {
        onVideoClickListener = listener
    }


}