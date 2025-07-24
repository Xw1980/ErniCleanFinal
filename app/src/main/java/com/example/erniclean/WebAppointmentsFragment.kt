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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class WebAppointmentsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WebAppointmentsAdapter
    private var appointments = mutableListOf<Appointment>()
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

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

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }

    private fun loadWebAppointments() {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        listenerRegistration?.remove() // Elimina el listener anterior si existe

        listenerRegistration = db.collection("formulario")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.widget.Toast.makeText(requireContext(), "Error al escuchar Firestore: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                appointments.clear()
                for (document in snapshots!!) {
                    val form = document.toObject(Formulario::class.java)
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
                }
                adapter.updateAppointments(appointments)
            }
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
            appointment.extras = etExtras.text.toString().trim()

            // Actualiza en Firestore
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val updatedData = mapOf(
                "nombre" to appointment.clientName,
                "telefono" to appointment.clientPhone,
                "direccion" to appointment.clientAddress,
                "serviciosSeleccionados" to appointment.serviceType.split(",").map { it.trim() },
                "mensaje" to (appointment.extras ?: "")
            )
            db.collection("formulario").document(appointment.id)
                .update(updatedData)
                .addOnSuccessListener {
                    android.widget.Toast.makeText(requireContext(), "Cita actualizada en la base de datos", android.widget.Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    android.widget.Toast.makeText(requireContext(), "Error al actualizar en Firestore: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }

            adapter.updateAppointments(appointments)
            dialog.dismiss()
            android.widget.Toast.makeText(requireContext(), "Cita actualizada", android.widget.Toast.LENGTH_SHORT).show()
        }
        btnNo.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
} 