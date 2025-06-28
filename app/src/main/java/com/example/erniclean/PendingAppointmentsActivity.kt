package com.example.erniclean

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PendingAppointmentsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentsAdapter
    private var appointments = mutableListOf<Appointment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Reutilizamos el layout principal para mantener el estilo

        recyclerView = findViewById(R.id.appointmentsRecyclerView)
        adapter = AppointmentsAdapter(
            appointments = appointments,
            onItemClick = { appointment -> showAppointmentDetails(appointment) },
            onCompleteClick = { appointment -> showOptionsDialog(appointment) },
            onPostponeClick = { appointment -> showOptionsDialog(appointment) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadPendingAppointments()
    }

    private fun loadPendingAppointments() {
        // Simulación de citas pendientes (en el futuro se cargará de la base de datos)
        appointments.clear()
        appointments.add(Appointment("1", "Juan Pérez", "555-0123", "Calle Principal 123", "Pulido de Marmol", java.util.Date()))
        appointments.add(Appointment("2", "María García", "555-0124", "Avenida Central 456", "Limpieza General", java.util.Date()))
        appointments.add(Appointment("3", "Carlos Sánchez", "555-0125", "Plaza Mayor 789", "Desinfección", java.util.Date()))
        adapter.updateAppointments(appointments)
    }

    private fun showAppointmentDetails(appointment: Appointment) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_appointment_details, null)
        dialogView.findViewById<TextView>(R.id.detailClientName).text = appointment.clientName
        dialogView.findViewById<TextView>(R.id.detailClientPhone).text = appointment.clientPhone
        dialogView.findViewById<TextView>(R.id.detailClientAddress).text = appointment.clientAddress
        dialogView.findViewById<TextView>(R.id.detailServiceType).text = appointment.serviceType

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("CERRAR") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showOptionsDialog(appointment: Appointment) {
        val options = arrayOf("Completar", "Posponer", "Editar", "Cancelar")
        AlertDialog.Builder(this)
            .setTitle("Opciones de la cita")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> completeAppointment(appointment)
                    1 -> postponeAppointment(appointment)
                    2 -> editAppointment(appointment)
                    3 -> cancelAppointment(appointment)
                }
            }
            .setNegativeButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun completeAppointment(appointment: Appointment) {
        appointment.status = AppointmentStatus.COMPLETED
        adapter.updateAppointments(appointments)
    }

    private fun postponeAppointment(appointment: Appointment) {
        appointment.status = AppointmentStatus.POSTPONED
        adapter.updateAppointments(appointments)
    }

    private fun editAppointment(appointment: Appointment) {
        // Aquí puedes abrir una pantalla de edición o mostrar un diálogo para editar
        // Por ahora solo muestra un mensaje
        AlertDialog.Builder(this)
            .setMessage("Funcionalidad de edición próximamente")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun cancelAppointment(appointment: Appointment) {
        // Aquí puedes implementar la lógica para cancelar la cita
        AlertDialog.Builder(this)
            .setMessage("¿Seguro que deseas cancelar la cita?")
            .setPositiveButton("Sí") { dialog, _ ->
                appointments.remove(appointment)
                adapter.updateAppointments(appointments)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }
} 