package com.example.soar.HomePage

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.Network.home.PopularPolicy
import com.example.soar.databinding.ItemPopularBizBinding

class PopularPolicyAdapter : ListAdapter<PopularPolicy, PopularPolicyAdapter.PopularPolicyViewHolder>(DiffCallback) {

    inner class PopularPolicyViewHolder(private val binding: ItemPopularBizBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(policy: PopularPolicy) {
            binding.policyName.text = policy.policyName
            itemView.setOnClickListener {
                val context = it.context
                val intent = Intent(context, DetailPageActivity::class.java).apply {
                    putExtra("policyId", policy.policyId)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularPolicyViewHolder {
        val binding = ItemPopularBizBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PopularPolicyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularPolicyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<PopularPolicy>() {
            override fun areItemsTheSame(old: PopularPolicy, new: PopularPolicy) = old.policyId == new.policyId
            override fun areContentsTheSame(old: PopularPolicy, new: PopularPolicy) = old == new
        }
    }
}