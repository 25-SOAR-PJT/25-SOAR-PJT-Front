package com.example.soar.Utill // PolicyActivity와 동일한 패키지 경로

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R

class TermDescAdapter(private val items: List<String>) :
    RecyclerView.Adapter<TermDescAdapter.DescriptionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescriptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_policy_description, parent, false) as TextView
        return DescriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: DescriptionViewHolder, position: Int) {
        holder.textView.text = items[position]
    }

    override fun getItemCount(): Int = items.size

    class DescriptionViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}