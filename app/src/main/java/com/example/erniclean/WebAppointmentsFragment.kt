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
import android.widget.Button

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
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_appointment, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvEditDialogTitle)
        val tvDay = dialogView.findViewById<TextView>(R.id.tvEditDialogDay)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etEditName)
        val etPhone = dialogView.findViewById<android.widget.EditText>(R.id.etEditPhone)
        val etAddress = dialogView.findViewById<android.widget.EditText>(R.id.etEditAddress)
        val etService = dialogView.findViewById<android.widget.EditText>(R.id.etEditService)
        val etExtras = dialogView.findViewById<android.widget.EditText>(R.id.etEditExtras)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnEditConfirm)
        val btnYes = dialogView.findViewById<Button>(R.id.btnEditYes)
        val btnNo = dialogView.findViewById<Button>(R.id.btnEditNo)

        // Título y día
        tvTitle.text = "CITAS"
        val cal = java.util.Calendar.getInstance().apply { time = appointment.date }
        val dayOfWeek = cal.getDisplayName(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.LONG, java.util.Locale("es"))?.uppercase() ?: ""
        val dayNumber = cal.get(java.util.Calendar.DAY_OF_MONTH)
        tvDay.text = "$dayOfWeek $dayNumber"

        // Rellenar campos actuales
        etName.setText(appointment.clientName)
        etPhone.setText(appointment.clientPhone)
        etAddress.setText(appointment.clientAddress)
        etService.setText(appointment.serviceType)
        etExtras.setText(appointment.extras ?: "")

        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnConfirm.setOnClickListener {
            appointment.clientName = etName.text.toString().trim()
            appointment.clientPhone = etPhone.text.toString().trim()
            appointment.clientAddress = etAddress.text.toString().trim()
            appointment.serviceType = etService.text.toString().trim()
            try {
                appointment.extras = etExtras.text.toString().trim()
            } catch (_: Exception) {}
            adapter.updateAppointments(appointments)
            dialog.dismiss()
            android.widget.Toast.makeText(requireContext(), "Cita actualizada", android.widget.Toast.LENGTH_SHORT).show()
        }
        btnYes.setOnClickListener { btnConfirm.performClick() }
        btnNo.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
} 