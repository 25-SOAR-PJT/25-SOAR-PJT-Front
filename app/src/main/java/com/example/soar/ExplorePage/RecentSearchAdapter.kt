package com.example.soar.ExplorePage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.databinding.ItemRecentSearchBinding

class RecentSearchAdapter (
    private val context: Context,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {

    private val recentSearches: MutableList<String> = mutableListOf()

    // Function to update the data and notify the adapter
    fun updateData(newSearches: List<String>) {
        recentSearches.clear()
        recentSearches.addAll(newSearches)
        notifyDataSetChanged()
    }

    // Function to get the current data (useful for saving)
    fun getData(): List<String> {
        return recentSearches
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the item layout using view binding
        val binding = ItemRecentSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchTerm = recentSearches[position]
        holder.bind(searchTerm) // Use the bind method in ViewHolder
    }

    override fun getItemCount(): Int {
        return recentSearches.size
    }

    // ViewHolder class updated to use View Binding
    inner class ViewHolder(private val binding: ItemRecentSearchBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(searchTerm: String) {
            binding.textSearch.text = searchTerm

            // Set click listener for the search term itself (to re-search)
            binding.textSearch.setOnClickListener {
                onItemClick(searchTerm)
            }

            // Set click listener for the delete button
            binding.btnDelete.setOnClickListener {
                // Call delete method on the activity/fragment
                if (context is SearchActivity) {
                    context.deleteRecentSearch(searchTerm)
                }
            }
        }
    }
}