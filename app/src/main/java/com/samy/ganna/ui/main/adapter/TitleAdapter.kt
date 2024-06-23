package com.samy.ganna.ui.main.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.samy.ganna.databinding.ItemTitleBinding
import com.samy.ganna.pojo.Title
import com.samy.ganna.utils.Constants
import com.samy.ganna.utils.Utils
import com.samy.ganna.utils.Utils.replaceArabicNumbers
import javax.inject.Inject


class TitleAdapter @Inject constructor() :
    ListAdapter<Title, TitleAdapter.ViewHolder>(DiffCallback()) {


    class DiffCallback : DiffUtil.ItemCallback<Title>() {
        override fun areItemsTheSame(
            oldItem: Title, newItem: Title
        ): Boolean = newItem == oldItem

        override fun areContentsTheSame(
            oldItem: Title, newItem: Title
        ): Boolean = newItem == oldItem
    }

    inner class ViewHolder(val binding: ItemTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Title, position: Int) {
            binding.tv.text = data.title.replaceArabicNumbers()
            binding.tv.isSelected = data.selected


//            if (data.isnad.isEmpty()) binding.lineBetween.visibility = View.INVISIBLE

            onclick(data,position)
            initialTextSize()
        }

        private fun onclick(data: Title,position: Int) {
            binding.tv.setOnClickListener {
                onItemClickListener?.let { it1 -> it1(it,data,position) }
            }
//            binding.tvMatn.setOnClickListener {
//                onItemClickListener?.let { it1 -> it1(data) }
//            }


        }

        private fun initialTextSize() {
            val small =
                Utils.getSharedPreferencesFloat(
                    binding.root.context,
                    Constants.FontSizeFile,
                    Constants.SMALL,
                    15.60f
                )
            val median =
                Utils.getSharedPreferencesFloat(
                    binding.root.context,
                    Constants.FontSizeFile,
                    Constants.MEDIAN,
                    18.20f
                )
            val high =
                Utils.getSharedPreferencesFloat(
                    binding.root.context,
                    Constants.FontSizeFile,
                    Constants.HIGH,
                    20.80f
                )

            binding.tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, median)
//            binding.tvIsnad.setTextSize(TypedValue.COMPLEX_UNIT_SP, small)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemTitleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)

    }

    private var onItemClickListener: ((View, Title,Int) -> Unit)? = null


    fun setOnItemClickListener(listener: (View,Title,Int) -> Unit) {
        onItemClickListener = listener
    }



}