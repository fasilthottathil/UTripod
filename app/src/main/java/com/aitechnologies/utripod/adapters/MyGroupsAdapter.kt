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
import com.aitechnologies.utripod.models.Groups
import com.aitechnologies.utripod.util.AppUtil.Companion.getTimeAgo
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MyGroupsAdapter(private val context: Context) :
    RecyclerView.Adapter<MyGroupsAdapter.ViewHolder>(), Filterable {

    inner class MyDiffUtil(
        private val oldList: List<Groups>,
        private val newList: List<Groups>
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

    private val groups: ArrayList<Groups> = arrayListOf()
    private var groupsFiltered: ArrayList<Groups> = arrayListOf()
    private var onGroupClickListener: ((Groups) -> Unit)? = null

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
                .load(groupsFiltered[position].imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(imgProfile)

            txtUsername.text = groupsFiltered[position].name
            txtMessage.text = groupsFiltered[position].message
            txtDate.text = getTimeAgo(groupsFiltered[position].timestamp.toDate().time)
            if (groupsFiltered[position].count > 0) {
                txtCount.visibility = VISIBLE
                if (groupsFiltered[position].count > 9)
                    "9+".also { txtCount.text = it }
                else
                    txtCount.text = groupsFiltered[position].count.toString()
            } else {
                txtCount.visibility = GONE
            }

            layout.setOnClickListener {
                onGroupClickListener?.let {
                    it(groupsFiltered[position])
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return groupsFiltered.size
    }

    override fun getItemViewType(position: Int): Int {
        return (position)
    }

    fun setData(newList: List<Groups>) {
        val diffUtil = MyDiffUtil(groups, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        groups.clear()
        groups.addAll(newList.sortedBy { it.timestamp })
        groupsFiltered.clear()
        groupsFiltered.addAll(groups)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setOnChatClickListener(listener: (Groups) -> Unit) {
        onGroupClickListener = listener
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(char: CharSequence?): FilterResults {
                val charString = char ?: ""
                groupsFiltered = if (charString.isEmpty()) groups
                else {
                    val filterList = ArrayList<Groups>()
                    groups.filter {
                        it.name.contains(charString, true)
                    }.forEach { filterList.add(it) }
                    filterList
                }
                return FilterResults().apply { values = groupsFiltered }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                groupsFiltered = if (results?.values == null)
                    arrayListOf()
                else
                    results.values as ArrayList<Groups>
                notifyDataSetChanged()
            }
        }
    }

}