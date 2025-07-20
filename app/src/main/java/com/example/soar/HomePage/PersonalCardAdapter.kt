package com.example.soar.HomePage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R
import com.example.soar.databinding.ItemHomeAd2Binding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date

class PersonalCardAdapter(private val items: List<ProgramCard>) :
    RecyclerView.Adapter<PersonalCardAdapter.CardViewHolder>() {

    inner class CardViewHolder(val binding: ItemHomeAd2Binding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemHomeAd2Binding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = items[position]
        holder.binding.location.text = item.location
        holder.binding.title.text = item.title

        val (ddayText, color) = getDDayText(item.date, holder.itemView.context)
        holder.binding.dday.text = ddayText
        holder.binding.dday.setTextColor(color)

        val bookmarkRes = if (item.isBookmarked) {
            R.drawable.icon_bookmark_checked
        } else {
            R.drawable.icon_bookmark
        }
        holder.binding.btnBookmark.setImageResource(bookmarkRes)

        holder.binding.btnBookmark.setOnClickListener {
            item.isBookmarked = !item.isBookmarked
            notifyItemChanged(position)
        }
    }

    fun getDDayText(deadline: LocalDate, context: Context): Pair<String, Int> {
        val today = LocalDate.now()
        val diff = ChronoUnit.DAYS.between(today, deadline).toInt()

        val text = when {
            diff == 0 -> "D-DAY"
            diff > 0  -> "D-$diff"
            else      -> "D+${-diff}"
        }

        val colorResId = if (diff == 0)
            R.color.semantic_accent_deadline_strong
        else
            R.color.semantic_accent_primary_strong

        val color = ContextCompat.getColor(context, colorResId)

        return Pair(text, color)
    }



    override fun getItemCount(): Int = items.size
}
