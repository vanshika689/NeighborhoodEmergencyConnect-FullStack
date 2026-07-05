package com.example.neighborhoodemergencyconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.databinding.ReqstructureBinding
import com.example.neighborhoodemergencyconnect.models.UserProfile

class VolReqAdap(
    private val volReqList: List<UserProfile>,
    private val onApprove: (UserProfile) -> Unit,
    private val onReject: (UserProfile) -> Unit
) : RecyclerView.Adapter<VolReqAdap.VolReqViewHolder>() {

    inner class VolReqViewHolder(
        val binding: ReqstructureBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VolReqViewHolder {

        val binding = ReqstructureBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return VolReqViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: VolReqViewHolder,
        position: Int
    ) {

        val user = volReqList[position]

        with(holder.binding) {

            tvName.text = user.name
            tvEmail.text = user.email

            val imageUrl = user.profileImage

            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(ivProfile.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.prof)
                    .error(R.drawable.prof)
                    .into(ivProfile)
            } else {
                ivProfile.setImageResource(R.drawable.prof)
            }

            chipStatus.text = "Pending"

            chipStatus.setChipBackgroundColorResource(R.color.orange_light)
            chipStatus.setTextColor(root.context.getColor(R.color.orange_dark))
            chipStatus.chipStrokeColor =
                root.context.getColorStateList(R.color.orange_dark)

            btnApprove.setOnClickListener {
                onApprove(user)
            }

            btnReject.setOnClickListener {
                onReject(user)
            }
        }
    }

    override fun getItemCount(): Int = volReqList.size
}