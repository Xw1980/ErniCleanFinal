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
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.Calendar
import java.util.Locale

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

        android.util.Log.d("Firestore", "Cargando citas web desde formularios...")
        android.util.Log.d("Firestore", "Instancia de Firestore: $db")

        // Primero verificar si hay datos
        db.collection("formulario").get().addOnSuccessListener { documents ->
            android.util.Log.d("Firestore", "Verificación inicial - Citas web encontradas: ${documents.size()}")
            for (document in documents) {
                android.util.Log.d("Firestore", "Documento inicial: ${document.id} = ${document.data}")
            }
        }.addOnFailureListener { e ->
            android.util.Log.e("Firestore", "Error en verificación inicial: ${e.message}")
        }

        listenerRegistration = db.collection("formulario")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.util.Log.e("Firestore", "Error al escuchar Firestore: ${e.message}")
                    android.widget.Toast.makeText(requireContext(), "Error al escuchar Firestore: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
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
                            extras = form.mensaje,
                            clientEmail = form.email
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

        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmAppointment)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDeleteAppointment)

        // Quitar el fondo blanco del diálogo
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Configurar el ancho del diálogo inmediatamente
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.98).toInt() // 98% del ancho de la pantalla
        android.util.Log.d("Dialog", "Ancho de pantalla: ${displayMetrics.widthPixels}, Ancho del diálogo: $width")
        alertDialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        // Establecer fondo transparente para el diálogo
        alertDialog.setOnShowListener {
            android.util.Log.d("Dialog", "Configurando ancho del diálogo...")
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            
            // Forzar la actualización del layout
            dialogView.requestLayout()
        }

        btnConfirm.setOnClickListener {
            alertDialog.dismiss()
            showDateSelectionDialog(appointment)
        }
        btnDelete.setOnClickListener {
            eliminarCita("formulario", appointment.id) {
                android.widget.Toast.makeText(requireContext(), "Cita eliminada", android.widget.Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }
        }
        alertDialog.show()
    }

    private fun showDateSelectionDialog(appointment: Appointment) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_date_selection, null)
        val calendarView = dialogView.findViewById<com.prolificinteractive.materialcalendarview.MaterialCalendarView>(R.id.calendarView)
        val tvPendingCount = dialogView.findViewById<TextView>(R.id.tvPendingCount)
        val rvPendingAppointments = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvPendingAppointments)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        // Cambiar el título del diálogo
        val titleTextView = dialogView.findViewById<TextView>(android.R.id.title)
        if (titleTextView != null) {
            titleTextView.text = "Elige la fecha"
        }

        // Configurar calendario en español
        calendarView.setWeekDayFormatter { dayOfWeek ->
            when (dayOfWeek.value) {
                1 -> "Dom"
                2 -> "Lun"
                3 -> "Mar"
                4 -> "Mié"
                5 -> "Jue"
                6 -> "Vie"
                7 -> "Sáb"
                else -> ""
            }
        }
        
        // Configurar el título del mes en español
        calendarView.setTitleFormatter { day ->
            val monthName = when (day.month) {
                1 -> "Enero"
                2 -> "Febrero"
                3 -> "Marzo"
                4 -> "Abril"
                5 -> "Mayo"
                6 -> "Junio"
                7 -> "Julio"
                8 -> "Agosto"
                9 -> "Septiembre"
                10 -> "Octubre"
                11 -> "Noviembre"
                12 -> "Diciembre"
                else -> ""
            }
            "$monthName ${day.year}"
        }
        
        // Configurar fecha mínima (hoy) para evitar seleccionar días pasados
        val today = com.prolificinteractive.materialcalendarview.CalendarDay.today()
        
        // Agregar decorador para mostrar días pasados en gris
        calendarView.addDecorator(object : com.prolificinteractive.materialcalendarview.DayViewDecorator {
            override fun shouldDecorate(day: com.prolificinteractive.materialcalendarview.CalendarDay): Boolean {
                return day.isBefore(today)
            }
            
            override fun decorate(view: com.prolificinteractive.materialcalendarview.DayViewFacade) {
                view.setDaysDisabled(true)
                view.addSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#BDBDBD")))
            }
        })
        
        calendarView.setSelectedDate(today)
        
        // Adaptador para las citas pendientes
        val pendingAdapter = PendingAppointmentsDialogAdapter(emptyList())
        rvPendingAppointments.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        rvPendingAppointments.adapter = pendingAdapter

        // Variable para almacenar la fecha seleccionada
        var selectedDate: com.prolificinteractive.materialcalendarview.CalendarDay? = today

        // Listener para cambios de fecha
        calendarView.setOnDateChangedListener { _, date, _ ->
            selectedDate = date
            checkPendingAppointmentsForDate(date, tvPendingCount, rvPendingAppointments, pendingAdapter)
        }

        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)
        
        // Configurar el ancho del diálogo de manera más agresiva
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.98).toInt() // 98% del ancho de la pantalla
        android.util.Log.d("Dialog", "Ancho de pantalla: ${displayMetrics.widthPixels}, Ancho del diálogo: $width")
        
        // Configurar ancho antes de mostrar
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Configurar ancho después de mostrar también
        dialog.setOnShowListener {
            android.util.Log.d("Dialog", "Configurando ancho del diálogo después de mostrar...")
            dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            
            // Forzar la actualización del layout
            dialogView.requestLayout()
            dialogView.invalidate()
        }

        btnConfirm.setOnClickListener {
            if (selectedDate != null) {
                confirmAppointmentWithDate(appointment, selectedDate!!, dialog)
            } else {
                android.widget.Toast.makeText(requireContext(), getString(R.string.please_select_date), android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Verificar citas pendientes para la fecha actual
        checkPendingAppointmentsForDate(today, tvPendingCount, rvPendingAppointments, pendingAdapter)
        
        dialog.show()
    }

    private fun checkPendingAppointmentsForDate(
        date: com.prolificinteractive.materialcalendarview.CalendarDay,
        tvPendingCount: TextView,
        rvPendingAppointments: androidx.recyclerview.widget.RecyclerView,
        adapter: PendingAppointmentsDialogAdapter
    ) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        // Convertir CalendarDay a Date para comparar
        val zoneId = org.threeten.bp.ZoneId.systemDefault()
        val instant = date.date.atStartOfDay(zoneId).toInstant()
        val startOfDay = java.util.Date(instant.toEpochMilli())
        
        val calendar = java.util.Calendar.getInstance()
        calendar.time = startOfDay
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.time

        db.collection("CitasPendientes")
            .whereGreaterThanOrEqualTo("fecha", startOfDay)
            .whereLessThan("fecha", endOfDay)
            .get()
            .addOnSuccessListener { documents ->
                val pendingAppointments = mutableListOf<Appointment>()
                
                for (document in documents) {
                    val data = document.data
                    val nombre = when (val nombreData = data["nombre"]) {
                        is String -> nombreData
                        else -> ""
                    }
                    val telefono = when (val telefonoData = data["telefono"]) {
                        is String -> telefonoData
                        else -> ""
                    }
                    val direccion = when (val direccionData = data["direccion"]) {
                        is String -> direccionData
                        else -> ""
                    }
                    val serviciosSeleccionados = when (val serviciosData = data["serviciosSeleccionados"]) {
                        is List<*> -> serviciosData.filterIsInstance<String>()
                        else -> listOf()
                    }
                    val mensaje = when (val mensajeData = data["mensaje"]) {
                        is String -> mensajeData
                        else -> ""
                    }
                    val fecha = when (val fechaData = data["fecha"]) {
                        is com.google.firebase.Timestamp -> fechaData.toDate()
                        is java.util.Date -> fechaData
                        else -> java.util.Date()
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
                    pendingAppointments.add(appointment)
                }

                if (pendingAppointments.isNotEmpty()) {
                    tvPendingCount.text = getString(R.string.pending_appointments_count, pendingAppointments.size)
                    tvPendingCount.visibility = android.view.View.VISIBLE
                    rvPendingAppointments.visibility = android.view.View.VISIBLE
                    adapter.updateAppointments(pendingAppointments)
                } else {
                    tvPendingCount.visibility = android.view.View.GONE
                    rvPendingAppointments.visibility = android.view.View.GONE
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firestore", "Error al obtener citas pendientes: ${e.message}")
            }
    }

    private fun confirmAppointmentWithDate(
        appointment: Appointment,
        selectedDate: com.prolificinteractive.materialcalendarview.CalendarDay,
        dialog: android.app.Dialog
    ) {
        // Convertir CalendarDay a Date
        val zoneId = org.threeten.bp.ZoneId.systemDefault()
        val instant = selectedDate.date.atStartOfDay(zoneId).toInstant()
        val appointmentDate = java.util.Date(instant.toEpochMilli())

        // Actualizar la fecha de la cita
        appointment.date = appointmentDate

        // Crear los datos de la cita con la fecha seleccionada
        val appointmentData = mapOf(
            "nombre" to appointment.clientName,
            "telefono" to appointment.clientPhone,
            "direccion" to appointment.clientAddress,
            "serviciosSeleccionados" to appointment.serviceType.split(",").map { it.trim() },
            "mensaje" to (appointment.extras ?: ""),
            "fecha" to appointmentDate,
            "status" to "PENDING"
        )

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        // Mostrar progreso
        val progressDialog = android.app.ProgressDialog(requireContext()).apply {
            setMessage("Confirmando cita y enviando correo...")
            setCancelable(false)
            show()
        }
        
        // Primero eliminar de formulario
        db.collection("formulario").document(appointment.id).delete()
            .addOnSuccessListener {
                // Luego crear en CitasPendientes con la fecha seleccionada
                db.collection("CitasPendientes").document(appointment.id).set(appointmentData)
                    .addOnSuccessListener {
                        // Enviar correo con PDF si hay email del cliente
                        if (appointment.clientEmail.isNotEmpty()) {
                            AppointmentConfirmationService.confirmAppointmentAndSendEmail(
                                context = requireContext(),
                                appointment = appointment,
                                clientEmail = appointment.clientEmail,
                                onSuccess = {
                                    progressDialog.dismiss()
                                    dialog.dismiss()
                                    android.widget.Toast.makeText(
                                        requireContext(), 
                                        "Cita confirmada y correo enviado exitosamente", 
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                },
                                onError = { error ->
                                    progressDialog.dismiss()
                                    dialog.dismiss()
                                    android.widget.Toast.makeText(
                                        requireContext(), 
                                        "Cita confirmada pero error al enviar correo: $error", 
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        } else {
                            progressDialog.dismiss()
                            dialog.dismiss()
                            android.widget.Toast.makeText(
                                requireContext(), 
                                getString(R.string.appointment_confirmed_for, selectedDate.day, selectedDate.month, selectedDate.year), 
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        android.util.Log.e("Firestore", "Error al crear cita en CitasPendientes: ${e.message}")
                        android.widget.Toast.makeText(requireContext(), getString(R.string.error_confirming_appointment), android.widget.Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                android.util.Log.e("Firestore", "Error al eliminar cita de formulario: ${e.message}")
                android.widget.Toast.makeText(requireContext(), getString(R.string.error_confirming_appointment), android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    private fun moverCita(origen: String, destino: String, citaId: String, onComplete: () -> Unit) {
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

    private fun eliminarCita(coleccion: String, citaId: String, onComplete: () -> Unit) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection(coleccion).document(citaId).delete().addOnSuccessListener {
            onComplete()
        }
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