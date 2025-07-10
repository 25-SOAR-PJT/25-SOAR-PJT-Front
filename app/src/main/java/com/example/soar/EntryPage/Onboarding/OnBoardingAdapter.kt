package com.example.soar.EntryPage.Onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R

class OnBoardAdapter(
    private val items: List<OnBoard>
) : RecyclerView.Adapter<OnBoardAdapter.OnBoardVH>() {

    inner class OnBoardVH(view: View) : RecyclerView.ViewHolder(view) {
        private val iv: ImageView = view.findViewById(R.id.ivIllustration)
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvSub: TextView = view.findViewById(R.id.tvSubTitle)

        fun bind(item: OnBoard) {
            iv.setImageResource(item.imageRes)
            tvTitle.setText(item.titleRes)
            tvSub.setText(item.subRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardVH =
        OnBoardVH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_onboarding, parent, false)
        )

    override fun onBindViewHolder(holder: OnBoardVH, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}

