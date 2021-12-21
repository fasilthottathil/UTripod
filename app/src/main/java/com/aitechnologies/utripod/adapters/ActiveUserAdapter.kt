package com.aitechnologies.utripod.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.databinding.ActiveUserItemBinding
import com.aitechnologies.utripod.models.Chats
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class  ActiveUserAdapter(private val context: Context) :
    RecyclerView.Adapter<ActiveUserAdapter.ViewHolder>() {

    inner class MyDiffUtil(
        private val oldList: List<Chats>,
        private val newList: List<Chats>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].roomId == newList[newItemPosition].roomId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

    }

    private val chats: ArrayList<Chats> = arrayListOf()
    private var onChatClickListener: ((Chats) -> Unit)? = null

    class ViewHolder(val binding: ActiveUserItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ActiveUserItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            Glide.with(context)
                .load(chats[position].profileUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(imgProfile)

            txtUsername.text = chats[position].username

            imgProfile.setOnClickListener {
                onChatClickListener?.let {
                    it(chats[position])
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun getItemViewType(position: Int): Int {
        return (position)
    }

    fun setData(newList: List<Chats>) {
        val diffUtil = MyDiffUtil(chats, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        chats.clear()
        chats.addAll(newList.sortedBy { it.timestamp })
        diffResult.dispatchUpdatesTo(this)
    }

    fun setOnChatClickListener(listener: (Chats) -> Unit) {
        onChatClickListener = listener
    }

}