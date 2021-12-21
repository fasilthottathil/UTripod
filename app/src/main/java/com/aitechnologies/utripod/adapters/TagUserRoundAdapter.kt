package com.aitechnologies.utripod.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.databinding.TagUserRoundItemBinding
import com.aitechnologies.utripod.models.Users
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class TagUserRoundAdapter(
    private val context: Context
) : RecyclerView.Adapter<TagUserRoundAdapter.ViewHolder>() {

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

    private var onClickListener: ((Users) -> Unit)? = null

    class ViewHolder(val binding: TagUserRoundItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TagUserRoundItemBinding.inflate(
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


            imgDelete.setOnClickListener {
                onClickListener?.let {
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

    fun setOnDeleteListener(listener: (Users) -> Unit) {
        onClickListener = listener
    }

    fun setData(newList: List<Users>) {
        val diffUtil = MyDiffUtil(users, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        users.clear()
        users.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

}