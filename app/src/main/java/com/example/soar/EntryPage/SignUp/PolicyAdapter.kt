package com.example.soar.EntryPage.SignUp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R
import com.example.soar.databinding.RowPolicyItemBinding

class PolicyAdapter(
    private val vm: Step1ViewModel,
    private val listener: OnPolicyClickListener
) : ListAdapter<PolicyItem, PolicyAdapter.Holder>(diff) {

    interface OnPolicyClickListener {
        fun onDetailClick(policyId: Int)
    }


    override fun onCreateViewHolder(p: ViewGroup, vType: Int) =
        Holder(RowPolicyItemBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: Holder, pos: Int) = h.bind(getItem(pos))

    inner class Holder(private val b: RowPolicyItemBinding) :
        RecyclerView.ViewHolder(b.root) {

        init {                      // 개별 항목 클릭 시 토글
            //b.root.setOnClickListener { vm.toggleItem(bindingAdapterPosition) }
            b.ivCheck.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    vm.toggleItem(bindingAdapterPosition)
                }
            }

            // 텍스트나 화살표를 클릭하면 상세 보기 페이지로 이동
            val detailClickListener = View.OnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    listener.onDetailClick(getItem(bindingAdapterPosition).id)
                }
            }
            b.tvLabel.setOnClickListener(detailClickListener)
            b.ivArrow.setOnClickListener(detailClickListener)
        }

        /*
       fun bind(item: PolicyItem) = with(b) {
           // 체크박스 이미지

           ivCheck.setImageResource(
               if (item.checked) R.drawable.ic_checkbox_active
               else R.drawable.ic_checkbox_inactive
           )
           ivCheck.isSelected = item.checked

           // 라벨 텍스트 및 색상(선택 상태 반영)
           tvLabel.text = item.title
           tvLabel.isSelected = item.checked      // ← 추가



        } */

        fun bind(item: PolicyItem) = with(b) {
            ivCheck.setImageResource(
                if (item.checked) R.drawable.ic_checkbox_active
                else R.drawable.ic_checkbox_inactive
            )
            ivCheck.isSelected = item.checked
            tvLabel.text = item.title
            tvLabel.isSelected = item.checked
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<PolicyItem>() {
            override fun areItemsTheSame(o: PolicyItem, n: PolicyItem) = o.id == n.id
            override fun areContentsTheSame(o: PolicyItem, n: PolicyItem) = o == n
        }
    }
}