package com.example.erniclean

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class WebAppointmentsAdapter(
    private var appointments: List<Appointment>,
    private val onCallClick: (Appointment) -> Unit,
    private val onWhatsappClick: (Appointment) -> Unit,
    private val onItemClick: (Appointment) -> Unit,
    private val onCompleteClick: (Appointment) -> Unit,
    private val onPostponeClick: (Appointment) -> Unit,
    private val onEditClick: (Appointment) -> Unit // Nuevo callback para editar
) : RecyclerView.Adapter<WebAppointmentsAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("EE", Locale("es"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayOfWeek: TextView = view.findViewById(R.id.dayOfWeek)
        val dayNumber: TextView = view.findViewById(R.id.dayNumber)
        val clientName: TextView = view.findViewById(R.id.clientName)
        val clientPhone: TextView = view.findViewById(R.id.clientPhone)
        val clientAddress: TextView = view.findViewById(R.id.clientAddress)
        val serviceType: TextView = view.findViewById(R.id.serviceType)
        val btnCall: ImageButton = view.findViewById(R.id.btnCall)
        val btnWhatsapp: ImageButton = view.findViewById(R.id.btnWhatsapp)
        val btnOptions = view.findViewById<android.widget.ImageButton>(R.id.btnOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_web_appointment, parent, false)
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

        holder.itemView.setOnClickListener { onItemClick(appointment) }
        holder.btnCall.setOnClickListener { onCallClick(appointment) }
        holder.btnWhatsapp.setOnClickListener { onWhatsappClick(appointment) }

        holder.btnOptions.setOnClickListener {
            val popup = android.widget.PopupMenu(holder.itemView.context, holder.btnOptions)
            popup.menu.add("Editar")
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Editar" -> onEditClick(appointment)
                }
                true
            }
            popup.show()
        }
    }

    override fun getItemCount() = appointments.size

    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
} 