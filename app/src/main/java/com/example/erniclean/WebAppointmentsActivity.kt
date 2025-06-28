package com.example.erniclean

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WebAppointmentsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WebAppointmentsAdapter
    private var appointments = mutableListOf<Appointment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Reutilizamos el layout principal para mantener el estilo

        recyclerView = findViewById(R.id.appointmentsRecyclerView)
        adapter = WebAppointmentsAdapter(
            appointments = appointments,
            onCallClick = { appointment -> callClient(appointment.clientPhone) },
            onWhatsappClick = { appointment -> openWhatsapp(appointment.clientPhone) },
            onItemClick = { appointment -> showAppointmentDetails(appointment) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadWebAppointments()
    }

    private fun loadWebAppointments() {
        // Simulación de citas web (en el futuro se cargará de la base de datos)
        appointments.clear()
        appointments.add(Appointment("4", "Ana López", "555-0126", "Calle Sur 321", "Limpieza de Vidrios", java.util.Date()))
        appointments.add(Appointment("5", "Pedro Ruiz", "555-0127", "Avenida Norte 654", "Desinfección", java.util.Date()))
        adapter.updateAppointments(appointments)
    }

    private fun callClient(phone: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phone")
        startActivity(intent)
    }

    private fun openWhatsapp(phone: String) {
        val uri = Uri.parse("https://wa.me/${'$'}phone")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.whatsapp")
        startActivity(intent)
    }

    private fun showAppointmentDetails(appointment: Appointment) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_appointment_details, null)
        dialogView.findViewById<TextView>(R.id.detailClientName).text = appointment.clientName
        dialogView.findViewById<TextView>(R.id.detailClientPhone).text = appointment.clientPhone
        dialogView.findViewById<TextView>(R.id.detailClientAddress).text = appointment.clientAddress
        dialogView.findViewById<TextView>(R.id.detailServiceType).text = appointment.serviceType

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("CERRAR") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
} 