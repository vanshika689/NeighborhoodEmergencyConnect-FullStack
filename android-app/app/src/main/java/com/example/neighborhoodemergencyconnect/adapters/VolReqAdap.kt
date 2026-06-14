package com.example.neighborhoodemergencyconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.neighborhoodemergencyconnect.databinding.ReqstructureBinding
import com.example.neighborhoodemergencyconnect.models.UserProfile

class VolReqAdap(
    private val volReqList: List<UserProfile>,
    private val onApprove: (UserProfile) -> Unit,
    private val onReject: (UserProfile) -> Unit
) : RecyclerView.Adapter<VolReqAdap.VolReqViewHolder>() {

    class VolReqViewHolder(
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
        holder.binding.tvName.text = user.name
        holder.binding.tvEmail.text = user.email

        holder.binding.btnApprove.setOnClickListener {
            onApprove(user)
        }

        holder.binding.btnReject.setOnClickListener {
            onReject(user)
        }
    }

    override fun getItemCount(): Int {
        return volReqList.size
    }
}