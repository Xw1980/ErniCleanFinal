package com.example.erniclean

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import android.widget.PopupMenu

class AppointmentsAdapter(
    private var appointments: List<Appointment>,
    private val onItemClick: (Appointment) -> Unit,
    private val onCompleteClick: (Appointment) -> Unit,
    private val onPostponeClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentsAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("EE", Locale("es"))
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayOfWeek: TextView = view.findViewById(R.id.dayOfWeek)
        val dayNumber: TextView = view.findViewById(R.id.dayNumber)
        val clientName: TextView = view.findViewById(R.id.clientName)
        val clientPhone: TextView = view.findViewById(R.id.clientPhone)
        val clientAddress: TextView = view.findViewById(R.id.clientAddress)
        val serviceType: TextView = view.findViewById(R.id.serviceType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        val calendar = Calendar.getInstance().apply { time = appointment.date }
        
        holder.dayOfWeek.text = dateFormat.format(appointment.date).uppercase()
        holder.dayNumber.text = calendar.get(Calendar.DAY_OF_MONTH).toString()
        holder.clientName.text = appointment.clientName
        holder.clientPhone.text = appointment.clientPhone
        holder.clientAddress.text = appointment.clientAddress
        holder.serviceType.text = appointment.serviceType

        holder.itemView.setOnClickListener {
            onItemClick(appointment)
        }

        holder.itemView.setOnLongClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.itemView)
            popup.menu.add("Completar")
            popup.menu.add("Posponer")
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Completar" -> onCompleteClick(appointment)
                    "Posponer" -> onPostponeClick(appointment)
                }
                true
            }
            popup.show()
            true
        }
    }

    override fun getItemCount() = appointments.size

    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
} 