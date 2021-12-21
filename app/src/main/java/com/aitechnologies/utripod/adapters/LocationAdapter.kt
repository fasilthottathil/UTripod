package com.aitechnologies.utripod.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aitechnologies.utripod.databinding.LocationItemBinding
import com.aitechnologies.utripod.models.Location

class LocationAdapter : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    inner class MyDiffUtil(
        private val oldList: List<Location>,
        private val newList: List<Location>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].data == newList[newItemPosition].data
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

    }

    private val location: ArrayList<Location> = arrayListOf()

    private var onItemClickListener: ((Location) -> Unit)? = null

    class ViewHolder(val binding: LocationItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LocationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            txtLocation.text = location[position].data[position].name
            txtRegion.text = location[position].data[position].region
            layout.setOnClickListener {
                onItemClickListener?.let {
                    it(location[position])
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return location.size
    }

    fun setOnItemClickListener(listener: (Location) -> Unit) {
        onItemClickListener = listener
    }

    fun setData(newList: List<Location>) {
        val diffUtil = MyDiffUtil(location, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        location.clear()
        location.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

}