package com.aitechnologies.utripod.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.NotificationNormalItemBinding
import com.aitechnologies.utripod.databinding.NotificationUserItemBinding
import com.aitechnologies.utripod.models.Notification
import com.aitechnologies.utripod.util.AppUtil.Companion.getTimeAgo
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MyDiffUtil(
        private val oldList: List<Notification>,
        private val newList: List<Notification>
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
        const val TYPE_POST = 0
        const val TYPE_UVIS = 1
        const val TYPE_USER = 2
    }

    private var notification: ArrayList<Notification> = arrayListOf()
    private var onPostNotificationClickListener: ((String) -> Unit)? = null
    private var onUvisNotificationClickListener: ((String) -> Unit)? = null
    private var onUserNotificationClickListener: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_POST -> TypePostNotificationViewHolder(
                NotificationNormalItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            TYPE_UVIS -> TypeUvisNotificationViewHolder(
                NotificationNormalItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> TypeUserNotificationViewHolder(
                NotificationUserItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (notification[position].viewType) {
            TYPE_POST -> (holder as TypePostNotificationViewHolder).bind(position)
            TYPE_UVIS -> (holder as TypeUvisNotificationViewHolder).bind(position)
            TYPE_USER -> (holder as TypeUserNotificationViewHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return notification.size
    }

    override fun getItemViewType(position: Int): Int {
        return notification[position].viewType
    }

    inner class TypePostNotificationViewHolder(val binding: NotificationNormalItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtMessage.text = notification[position].message
                txtDate.text = getTimeAgo(notification[position].timestamp.toDate().time)
                Glide.with(context)
                    .load(notification[position].url)
                    .into(imgNotification)

                layout.setOnClickListener {
                    onPostNotificationClickListener?.let {
                        it(notification[position].id)
                    }
                }
            }
        }
    }

    inner class TypeUvisNotificationViewHolder(val binding: NotificationNormalItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtMessage.text = notification[position].message
                txtDate.text = getTimeAgo(notification[position].timestamp.toDate().time)
                Glide.with(context)
                    .load(notification[position].url)
                    .into(imgNotification)

                layout.setOnClickListener {
                    onUvisNotificationClickListener?.let {
                        it(notification[position].id)
                    }
                }
            }
        }
    }

    inner class TypeUserNotificationViewHolder(val binding: NotificationUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                txtMessage.text = notification[position].message
                txtDate.text = getTimeAgo(notification[position].timestamp.toDate().time)
                Glide.with(context)
                    .load(notification[position].url)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgNotification)

                layout.setOnClickListener {
                    onUserNotificationClickListener?.let {
                        it(notification[position].id)
                    }
                }

            }
        }
    }

    fun setData(newList: List<Notification>) {
        val diffUtil = MyDiffUtil(notification, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        notification.clear()
        notification.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setOnPostNotificationClickListener(listener: (String) -> Unit) {
        onPostNotificationClickListener = listener
    }

    fun setOnUvisNotificationClickListener(listener: (String) -> Unit) {
        onUvisNotificationClickListener = listener
    }

    fun setOnUserNotificationClickListener(listener: (String) -> Unit) {
        onUserNotificationClickListener = listener
    }

}
