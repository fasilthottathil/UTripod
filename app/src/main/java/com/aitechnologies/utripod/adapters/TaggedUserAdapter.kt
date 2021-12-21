package com.aitechnologies.utripod.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.databinding.TagUserItemBinding
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class TaggedUserAdapter(
    private val context: Context,
    private val username:String
) : RecyclerView.Adapter<TaggedUserAdapter.ViewHolder>() {

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
    private var onUserLongClickListener: ((Users) -> Unit)? = null
    private var counter = 0

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
                if (counter > 0) {
                    if (layoutMark.visibility == VISIBLE) {
                        counter--
                        layoutMark.visibility = INVISIBLE
                    } else {
                        counter++
                        layoutMark.visibility = VISIBLE
                    }
                    onUserLongClickListener?.invoke(users[position])
                } else
                    onUserClickListener?.let {
                        it(users[position])
                    }
            }

            layout.setOnLongClickListener {
                if (context.getUsername() == username) {
                    if (layoutMark.visibility == VISIBLE) {
                        counter--
                        layoutMark.visibility = INVISIBLE
                    } else {
                        counter++
                        layoutMark.visibility = VISIBLE
                    }
                    onUserLongClickListener?.invoke(users[position])
                }
                true
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

    fun setOnUserLongClickListener(listener: (Users) -> Unit) {
        onUserLongClickListener = listener
    }

    fun setData(newList: List<Users>) {
        val diffUtil = MyDiffUtil(users, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        users.clear()
        users.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

}