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
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Reutilizamos el layout principal para mantener el estilo

        recyclerView = findViewById(R.id.appointmentsRecyclerView)
        adapter = WebAppointmentsAdapter(
            appointments = appointments,
            onCallClick = { appointment -> callClient(appointment.clientPhone) },
            onWhatsappClick = { appointment -> openWhatsapp(appointment.clientPhone) },
            onItemClick = { appointment -> showAppointmentDetails(appointment) },
            onCompleteClick = { /* TODO: implementar acción completar */ },
            onPostponeClick = { /* TODO: implementar acción posponer */ },
            onEditClick = {} // No hace nada aquí
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadWebAppointments()
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    private fun loadWebAppointments() {
        // Cargar citas web desde Firestore con listener en tiempo real
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        listenerRegistration?.remove() // Elimina el listener anterior si existe

        android.util.Log.d("Firestore", "Cargando citas web desde formularios...")

        listenerRegistration = db.collection("formulario")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.util.Log.e("Firestore", "Error al escuchar Firestore: ${e.message}")
                    android.widget.Toast.makeText(this, "Error al escuchar Firestore: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                
                android.util.Log.d("Firestore", "Citas web encontradas: ${snapshots?.size() ?: 0}")
                
                appointments.clear()
                for (document in snapshots!!) {
                    val form = document.toObject(Formulario::class.java)
                    if (form != null) {
                        val appointment = Appointment(
                            id = document.id,
                            clientName = form.nombre,
                            clientPhone = form.telefono,
                            clientAddress = form.direccion,
                            serviceType = form.serviciosSeleccionados.joinToString(", "),
                            date = java.util.Date(),
                            extras = form.mensaje
                        )
                        appointments.add(appointment)
                        android.util.Log.d("Firestore", "Cita agregada: ${appointment.clientName}")
                    } else {
                        android.util.Log.e("Firestore", "Error al convertir documento ${document.id} a Formulario")
                    }
                }
                adapter.updateAppointments(appointments)
            }
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