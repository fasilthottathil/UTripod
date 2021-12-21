package com.aitechnologies.utripod.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.databinding.ChatsItemBinding
import com.aitechnologies.utripod.models.Chats
import com.aitechnologies.utripod.util.AppUtil.Companion.getTimeAgo
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MyChatsAdapter(private val context: Context) :
    RecyclerView.Adapter<MyChatsAdapter.ViewHolder>(), Filterable {

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
    private var chatsFiltered: ArrayList<Chats> = arrayListOf()
    private var onChatClickListener: ((Chats) -> Unit)? = null

    class ViewHolder(val binding: ChatsItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ChatsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            Glide.with(context)
                .load(chatsFiltered[position].profileUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(imgProfile)

            txtUsername.text = chatsFiltered[position].username
            txtMessage.text = chatsFiltered[position].message
            txtDate.text = getTimeAgo(chatsFiltered[position].timestamp.toDate().time)
            if (chatsFiltered[position].count > 0) {
                txtCount.visibility = VISIBLE
                if (chats[position].count > 9)
                    "9+".also { txtCount.text = it }
                else
                    txtCount.text = chatsFiltered[position].count.toString()
            } else {
                txtCount.visibility = GONE
            }

            layout.setOnClickListener {
                onChatClickListener?.let {
                    it(chatsFiltered[position])
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return chatsFiltered.size
    }

    override fun getItemViewType(position: Int): Int {
        return (position)
    }

    fun setData(newList: List<Chats>) {
        val diffUtil = MyDiffUtil(chats, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        chats.clear()
        chats.addAll(newList.sortedBy { it.timestamp })
        chatsFiltered.clear()
        chatsFiltered.addAll(chats)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setOnChatClickListener(listener: (Chats) -> Unit) {
        onChatClickListener = listener
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(char: CharSequence?): FilterResults {
                val charString = char ?: ""
                chatsFiltered = if (charString.isEmpty()) chats
                else {
                    val filterList = ArrayList<Chats>()
                    chats.filter {
                        (it.username.contains(charString, true))
                    }.forEach { filterList.add(it) }
                    filterList
                }
                return FilterResults().apply {
                    values = chatsFiltered
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                chatsFiltered = if (results?.values == null)
                    arrayListOf()
                else
                    results.values as ArrayList<Chats>
                notifyDataSetChanged()
            }
        }
    }

}