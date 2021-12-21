package com.aitechnologies.utripod.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.SearchUserItemBinding
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SearchUserAdapter(
    private val context: Context
) : RecyclerView.Adapter<SearchUserAdapter.ViewHolder>() {

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
    private val followersList:ArrayList<String> = arrayListOf()

    private var onUserClickListener: ((Users) -> Unit)? = null
    private var onFollowClickListener: ((String) -> Unit)? = null

    class ViewHolder(val binding: SearchUserItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SearchUserItemBinding.inflate(
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

            var isFollowing: Boolean

            if (followersList.contains(users[position].username)){
                isFollowing = true
                txtFollow.apply {
                    text = context.getString(R.string.unfollow)
                    setBackgroundResource(R.drawable.unfollow_button_profile)
                }
            }else{
                isFollowing = false
                txtFollow.apply {
                    text = context.getString(R.string.follow)
                    setBackgroundResource(R.drawable.follow_button_profile)
                }
            }

            if (users[position].username == context.getUsername()){
                txtFollow.visibility = GONE
            }

            layout.setOnClickListener {
                onUserClickListener?.let {
                    it(users[position])
                }
            }

            txtFollow.setOnClickListener {
                if (isFollowing){
                    isFollowing = false
                    txtFollow.apply {
                        text = context.getString(R.string.follow)
                        setBackgroundResource(R.drawable.follow_button_profile)
                    }
                }else{
                    isFollowing = true
                    txtFollow.apply {
                        text = context.getString(R.string.unfollow)
                        setBackgroundResource(R.drawable.unfollow_button_profile)
                    }
                }
                onFollowClickListener?.let {
                    it(users[position].username)
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

    fun setOnFollowClickListener(listener: (String) -> Unit) {
        onFollowClickListener = listener
    }

    fun setData(newList: List<Users>) {
        val diffUtil = MyDiffUtil(users, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        users.clear()
        users.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setFollowers(followersList: List<String>){
        this.followersList.clear()
        this.followersList.addAll(followersList)
    }

}