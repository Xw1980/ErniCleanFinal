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
    private val onPostponeClick: (Appointment) -> Unit,
    private val onEditClick: (Appointment) -> Unit, // Nuevo callback para editar
    private val showAddEvidenceButton: Boolean = false, // Nuevo parámetro para decidir qué botón mostrar
    private val showEditOption: Boolean = false // Nuevo parámetro para mostrar 'Editar'
) : RecyclerView.Adapter<AppointmentsAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("EE", Locale("es"))
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayOfWeek: TextView = view.findViewById(R.id.dayOfWeek)
        val dayNumber: TextView = view.findViewById(R.id.dayNumber)
        val clientName: TextView = view.findViewById(R.id.clientName)
        val clientPhone: TextView = view.findViewById(R.id.clientPhone)
        val clientAddress: TextView = view.findViewById(R.id.clientAddress)
        val serviceType: TextView = view.findViewById(R.id.serviceType)
        val btnAddEvidence: android.widget.ImageButton? = view.findViewById(R.id.btnAddEvidence)
        val btnOptions: android.widget.ImageButton? = view.findViewById(R.id.btnOptions)
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

        // Ocultar SIEMPRE el botón '+' en todas las pantallas
        holder.btnAddEvidence?.let {
            it.visibility = View.GONE
            it.setOnClickListener(null)
        }

        // Ocultar los tres puntos en la pantalla de evidencias
        holder.btnOptions?.let { btnOptions ->
            if (showAddEvidenceButton) {
                btnOptions.visibility = View.GONE
                btnOptions.setOnClickListener(null)
            } else {
                btnOptions.visibility = View.VISIBLE
                btnOptions.setOnClickListener {
                    val popup = PopupMenu(holder.itemView.context, btnOptions)
                    popup.menu.add("Completar")
                    popup.menu.add("Posponer")
                    if (showEditOption) {
                        popup.menu.add("Editar")
                    }
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.title) {
                            "Completar" -> onCompleteClick(appointment)
                            "Posponer" -> onPostponeClick(appointment)
                            "Editar" -> onEditClick(appointment)
                        }
                        true
                    }
                    popup.show()
                }
            }
        }
    }

    override fun getItemCount() = appointments.size

    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
} 