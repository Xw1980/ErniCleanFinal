package com.example.erniclean

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class PendingAppointmentsDialogAdapter(
    private var appointments: List<Appointment>
) : RecyclerView.Adapter<PendingAppointmentsDialogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClientName: TextView = view.findViewById(R.id.tvClientName)
        val tvServiceType: TextView = view.findViewById(R.id.tvServiceType)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_appointment_dialog, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.tvClientName.text = appointment.clientName
        holder.tvServiceType.text = appointment.serviceType
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale("es"))
        holder.tvTime.text = timeFormat.format(appointment.date)
    }

    override fun getItemCount() = appointments.size

    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }

} 