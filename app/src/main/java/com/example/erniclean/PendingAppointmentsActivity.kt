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
import android.widget.Toast

class PendingAppointmentsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentsAdapter
    private var appointments = mutableListOf<Appointment>()
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // --- TRANSPARENCIA EN BARRAS DEL SISTEMA ---
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        // -------------------------------------------
        // Cambia el layout a uno propio si existe, por ejemplo:
        // setContentView(R.layout.activity_pending_appointments)
        // Si no existe, crea el layout con un RecyclerView con id appointmentsRecyclerView
        recyclerView = findViewById(R.id.appointmentsRecyclerView)
        adapter = AppointmentsAdapter(
            appointments,
            onItemClick = { /* No hacer nada al clickear la tarjeta */ },
            onCompleteClick = { appointment ->
                android.util.Log.d("Firestore", "Callback onCompleteClick recibido en Activity para cita: ${appointment.id}")
                completeAppointment(appointment)
            },
            onPostponeClick = { appointment -> postponeAppointment(appointment) },
            onEditClick = { appointment -> editAppointment(appointment) },
            showAddEvidenceButton = false,
            showEditOption = true
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        loadPendingAppointments()
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    private fun loadPendingAppointments() {
        // Cargar citas pendientes desde Firestore con listener en tiempo real
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        listenerRegistration?.remove() // Elimina el listener anterior si existe

        listenerRegistration = db.collection("CitasPendientes")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.widget.Toast.makeText(this, "Error al escuchar Firestore: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                appointments.clear()
                for (document in snapshots!!) {
                    val data = document.data
                    val nombre = data["nombre"] as? String ?: ""
                    val telefono = data["telefono"] as? String ?: ""
                    val direccion = data["direccion"] as? String ?: ""
                    val serviciosSeleccionados = data["serviciosSeleccionados"] as? List<String> ?: emptyList()
                    val mensaje = when (val mensajeData = data["mensaje"]) {
                        is String -> mensajeData
                        else -> ""
                    }
                    
                    // Mapear la fecha desde Firestore
                    val fecha = when (val fechaData = data["fecha"]) {
                        is com.google.firebase.Timestamp -> fechaData.toDate()
                        is java.util.Date -> fechaData
                        else -> java.util.Date() // Fecha actual como fallback
                    }
                    
                    val appointment = Appointment(
                        id = document.id,
                        clientName = nombre,
                        clientPhone = telefono,
                        clientAddress = direccion,
                        serviceType = serviciosSeleccionados.joinToString(", "),
                        date = fecha,
                        extras = mensaje
                    )
                    appointments.add(appointment)
                }
                adapter.updateAppointments(appointments)
            }
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
        android.util.Log.d("Firestore", "Función completeAppointment ejecutada en Activity para cita: ${appointment.id}")
        moverCitaFirestore("CitasPendientes", "CitasCompletadas", appointment.id) {
            android.util.Log.d("Firestore", "Callback de moverCitaFirestore ejecutado en Activity")
            appointments.remove(appointment)
            adapter.updateAppointments(appointments)
            Toast.makeText(this@PendingAppointmentsActivity, "Cita marcada como completada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moverCitaFirestore(origen: String, destino: String, citaId: String, onComplete: () -> Unit) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        android.util.Log.d("Firestore", "Intentando mover cita: $citaId de '$origen' a '$destino'")
        
        val docRef = db.collection(origen).document(citaId)
        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val data = document.data
                android.util.Log.d("Firestore", "Documento encontrado en $origen: $data")
                
                docRef.delete().addOnSuccessListener {
                    android.util.Log.d("Firestore", "Documento eliminado de $origen")
                    
                    if (data != null) {
                        db.collection(destino).document(citaId).set(data).addOnSuccessListener {
                            android.util.Log.d("Firestore", "Documento creado en $destino exitosamente")
                            onComplete()
                        }.addOnFailureListener { e ->
                            android.util.Log.e("Firestore", "Error al crear documento en $destino: ${e.message}")
                            onComplete()
                        }
                    } else {
                        android.util.Log.e("Firestore", "Datos del documento son null")
                        onComplete()
                    }
                }.addOnFailureListener { e ->
                    android.util.Log.e("Firestore", "Error al eliminar documento de $origen: ${e.message}")
                    onComplete()
                }
            } else {
                android.util.Log.e("Firestore", "Documento no encontrado en $origen con ID: $citaId")
                onComplete()
            }
        }.addOnFailureListener { e ->
            android.util.Log.e("Firestore", "Error al obtener documento de $origen: ${e.message}")
            onComplete()
        }
    }

    private fun postponeAppointment(appointment: Appointment) {
        // Por ahora mostrar un mensaje, pero se puede implementar el diálogo completo
        Toast.makeText(this@PendingAppointmentsActivity, "Función de posponer no implementada completamente", Toast.LENGTH_SHORT).show()
        
        // TODO: Implementar el diálogo completo de posponer como en el Fragment
        // Por ahora solo actualizar la fecha en Firestore como ejemplo
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val updatedData = mapOf(
            "fecha" to appointment.date,
            "status" to "POSTPONED"
        )
        
        android.util.Log.d("Firestore", "Actualizando fecha de cita en Activity: ${appointment.id}")
        
        db.collection("CitasPendientes").document(appointment.id)
            .update(updatedData)
            .addOnSuccessListener {
                android.util.Log.d("Firestore", "Fecha de cita actualizada exitosamente en Activity")
                Toast.makeText(this@PendingAppointmentsActivity, "Cita pospuesta", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firestore", "Error al actualizar fecha de cita en Activity: ${e.message}")
                Toast.makeText(this@PendingAppointmentsActivity, "Error al posponer la cita", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editAppointment(appointment: Appointment) {
        // Implementar lógica de editar
        Toast.makeText(this@PendingAppointmentsActivity, "Función de editar no implementada", Toast.LENGTH_SHORT).show()
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