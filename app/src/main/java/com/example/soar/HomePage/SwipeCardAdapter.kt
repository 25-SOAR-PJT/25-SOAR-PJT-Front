package com.example.soar.HomePage

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R
import com.example.soar.Network.home.BannerResponse
import com.example.soar.databinding.ItemSwipeCardBinding
import com.example.soar.util.showBlockingToast

// ✨ 생성자 파라미터를 MutableList로 변경하여 내용 수정이 가능하게 함
class SwipeCardAdapter(private val items: MutableList<BannerResponse>) :
    RecyclerView.Adapter<SwipeCardAdapter.SwipeCardViewHolder>() {

    inner class SwipeCardViewHolder(val binding: ItemSwipeCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // ✨ 데이터를 새로 받아 갱신하고, UI에 변경사항을 알리는 함수 추가
    fun updateData(newItems: List<BannerResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged() // 매우 중요: 데이터가 바뀌었음을 알림
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSwipeCardBinding.inflate(inflater, parent, false)
        return SwipeCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SwipeCardViewHolder, position: Int) {
        val item = items[position]

        try {
            val imageBytes = Base64.decode(item.base64Image, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.binding.itemImage.setImageBitmap(decodedImage)
        } catch (e: Exception) {
            Log.e("SwipeCardAdapter", "Base64 디코딩 실패", e)
            holder.binding.itemImage.setImageResource(R.drawable.swipe_img1)
        }

        holder.binding.itemImage.setOnClickListener {
            val url = item.url
            if (url.startsWith("http://") || url.startsWith("https://")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                holder.itemView.context.startActivity(intent)
            } else {
                (holder.itemView.context as? Activity)?.showBlockingToast("유효하지 않은 URL입니다.", hideCancel = true)
            }
        }
    }

    override fun getItemCount() = items.size
}