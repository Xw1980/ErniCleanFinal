package com.example.erniclean

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WebAppointmentsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WebAppointmentsAdapter
    private var appointments = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_web_appointments, container, false)
        recyclerView = view.findViewById(R.id.appointmentsRecyclerView)
        adapter = WebAppointmentsAdapter(
            appointments = appointments,
            onCallClick = { appointment -> callClient(appointment.clientPhone) },
            onWhatsappClick = { appointment -> openWhatsapp(appointment.clientPhone) },
            onItemClick = { appointment -> showAppointmentDetails(appointment) },
            onCompleteClick = { /* TODO: implementar acción completar */ },
            onPostponeClick = { /* TODO: implementar acción posponer */ },
            onEditClick = { appointment -> editAppointment(appointment) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        loadWebAppointments()
        return view
    }

    private fun loadWebAppointments() {
        appointments.clear()
        appointments.add(Appointment("4", "Ana López", "555-0126", "Calle Sur 321", "Limpieza de Vidrios", java.util.Date()))
        appointments.add(Appointment("5", "Pedro Ruiz", "555-0127", "Avenida Norte 654", "Desinfección", java.util.Date()))
        adapter.updateAppointments(appointments)
    }

    private fun callClient(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        startActivity(intent)
    }

    // La acción de WhatsApp ya es funcional y no muestra mensaje de 'próximamente'.
    private fun openWhatsapp(phone: String) {
        val uri = Uri.parse("https://wa.me/$phone")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.whatsapp")
        startActivity(intent)
    }

    private fun showAppointmentDetails(appointment: Appointment) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_appointment_details, null)
        dialogView.findViewById<TextView>(R.id.detailClientName).text = appointment.clientName
        dialogView.findViewById<TextView>(R.id.detailClientPhone).text = appointment.clientPhone
        dialogView.findViewById<TextView>(R.id.detailClientAddress).text = appointment.clientAddress
        dialogView.findViewById<TextView>(R.id.detailServiceType).text = appointment.serviceType

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("CERRAR") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun editAppointment(appointment: Appointment) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_appointment_edit, null)
        val editName = dialogView.findViewById<android.widget.EditText>(R.id.editClientName)
        val editPhone = dialogView.findViewById<android.widget.EditText>(R.id.editClientPhone)
        val editAddress = dialogView.findViewById<android.widget.EditText>(R.id.editClientAddress)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSaveEditAppointment)

        // Rellenar campos actuales
        editName.setText(appointment.clientName)
        editPhone.setText(appointment.clientPhone)
        editAddress.setText(appointment.clientAddress)

        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnSave.setOnClickListener {
            val newName = editName.text.toString().trim()
            val newPhone = editPhone.text.toString().trim()
            val newAddress = editAddress.text.toString().trim()
            if (newName.isEmpty() || newPhone.isEmpty() || newAddress.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Todos los campos son obligatorios", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                appointment.clientName = newName
                appointment.clientPhone = newPhone
                appointment.clientAddress = newAddress
                adapter.updateAppointments(appointments)
                dialog.dismiss()
                android.widget.Toast.makeText(requireContext(), "Cita actualizada", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }
} 