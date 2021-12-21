package com.aitechnologies.utripod.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.databinding.HashTagItemBinding
import com.aitechnologies.utripod.models.HashTag

class HashTagAdapter(private val list: List<HashTag>) :
    RecyclerView.Adapter<HashTagAdapter.ViewHolder>() {

    private var onHashTagClickListener: ((String) -> Unit)? = null

    class ViewHolder(val binding: HashTagItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HashTagItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (list[position].tag.isNotBlank() && list[position].tag.isNotEmpty())
            holder.binding.apply {
                if (list[position].count != 0) {
                    "#${
                        list[position].tag.replace(
                            "#",
                            ""
                        )
                    } [${list[position].count}]".also { txtHasTag.text = it }
                } else {
                    "#${
                        list[position].tag.replace(
                            "#",
                            ""
                        )
                    }".also { txtHasTag.text = it }
                }


                txtHasTag.setOnClickListener {
                    onHashTagClickListener?.let {
                        it(list[position].tag)
                    }
                }
            }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return (position)
    }

    fun setOnHashTagClickListener(listener: (String) -> Unit) {
        onHashTagClickListener = listener
    }

}