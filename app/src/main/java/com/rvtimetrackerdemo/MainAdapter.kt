package com.rvtimetrackerdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class MainAdapter :
    RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    private val items = ArrayList<Item>()
    fun setItems(items: List<Item>) {
        val diffResult = DiffUtil.calculateDiff(MainDiffCallBack(this.items, items))
        this.items.clear()
        this.items.addAll(items)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item, parent, false)
        return MainViewHolder(view)
    }

    override fun getItemCount(): Int {
        return this.items.size
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.itemView.tag = this.items[position].toString()
        holder.bind(this.items[position], position)
    }

    inner class MainViewHolder(itemView: View) : ViewHolder(itemView) {

        private var textView: TextView

        init {
            textView = itemView.findViewById(R.id.textView)
        }

        fun bind(item: Item, position: Int) {
            val name =
                item.name + " " + item.sirName + "\nposition = " + position + "\nuniqueId: " + item.uniqueId
            textView.text = name
            itemView.setOnClickListener {
                rvItemClickListener?.let { it(item, position) }
            }
        }

    }

    private var rvItemClickListener: ((Item, Int) -> Unit)? = null

    fun setOnRvItemClickListener(listener: (Item, Int) -> Unit) {
        this.rvItemClickListener = listener
    }
}

class MainDiffCallBack(private val oldList: List<Item>, private val newList: List<Item>) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].uniqueId == newList[newItemPosition].uniqueId

    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
