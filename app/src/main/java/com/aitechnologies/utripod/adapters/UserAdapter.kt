package com.aitechnologies.utripod.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.databinding.TagUserItemBinding
import com.aitechnologies.utripod.models.Users
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class UserAdapter(
    private val context: Context
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    inner class MyDiffUtil(
        private val oldList: List<Users>,
        private val newList: List<Users>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].username == newList[newItemPosition].username
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    private val users: ArrayList<Users> = arrayListOf()

    private var onUserClickListener: ((Users) -> Unit)? = null

    class ViewHolder(val binding: TagUserItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TagUserItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            Glide.with(context)
                .load(users[position].profileUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(imgProfile)

            txtUsername.text = users[position].username

            layout.setOnClickListener {
                onUserClickListener?.let {
                    it(users[position])
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun getItemViewType(position: Int): Int {
        return (position)
    }

    fun setOnUserClickListener(listener: (Users) -> Unit) {
        onUserClickListener = listener
    }

    fun setData(newList: List<Users>) {
        val diffUtil = MyDiffUtil(users, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        users.clear()
        users.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

}