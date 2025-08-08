package com.example.soar.MyPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R

// dto 연결 후에 수정 해서 쓰면 됨

//class AnnouncementAdapter (
////    private val items: List<AnnouncementResponseDto>
//) : RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {
//
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val container: LinearLayout = view.findViewById(R.id.announcement_container)
//        val date: TextView = view.findViewById(R.id.date)
//        val title: TextView = view.findViewById(R.id.announcement_title)
//        val detailContainer: LinearLayout = view.findViewById(R.id.announcement_detail_container)
//        val content: TextView = view.findViewById(R.id.announcement_detail_content)
//        val arrow: ImageView = view.findViewById(R.id.announcement_arrow) // 화살표 추가
//
//
////        fun bind(item: AnnouncementResponseDto, position: Int) {
////            date.text = item.date
////            title.text = item.title
////            content.text = item.content
////            detailContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
////            arrow.rotation = if (item.isExpanded) 90F else 270F
////
////            container.setOnClickListener {
////                item.isExpanded = !item.isExpanded
////                detailContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
////                arrow.rotation = if (item.isExpanded) 90F else 270F
////            }
////        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_announcement, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        //holder.bind(items[position], position)
//    }
//
//    //override fun getItemCount() = items.size
//}