package com.example.neighborhoodemergencyconnect.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.example.neighborhoodemergencyconnect.databinding.ItemAlertBinding
import com.example.neighborhoodemergencyconnect.models.Alert
import java.text.SimpleDateFormat
import java.util.Locale
import android.R
import java.util.TimeZone

class AlertAdapter(private val alerts: MutableList<Alert>, private val onClick: (Alert)-> Unit) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>(){

    class AlertViewHolder(val binding: ItemAlertBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.binding.tvType.text = alert.title
        holder.binding.tvLocation.text = "📍 " + alert.shortAddress
        holder.binding.tvTime.text = getRelativeTime(alert.createdAt)
        holder.binding.tvStatus.text = alert.status
        holder.itemView.setOnClickListener{
            onClick(alert)
        }
        when (alert.title.trim()) {
            "Fire" -> holder.binding.imgCategory.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.fireal)
            "Accident" ->holder.binding.imgCategory.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.acci)
            "Crime" -> holder.binding.imgCategory.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.crime)
            "Disaster" -> holder.binding.imgCategory.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.dis)
            else -> holder.binding.imgCategory.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.other)
        }
    }

    override fun getItemCount(): Int {
        return alerts.size
    }


    fun getRelativeTime(createdAt: String): CharSequence {
        val sdf = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault()
        )
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val time = sdf.parse(createdAt)?.time ?: return createdAt

        return DateUtils.getRelativeTimeSpanString(
            time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
    }



}


