package com.example.erniclean

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.Activity
import coil.load
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.widget.TextView

class EvidenceFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentsAdapter
    private var completedAppointments = mutableListOf<Appointment>()
    private var selectedAppointment: Appointment? = null
    private lateinit var evidenceGallery: LinearLayout
    private var evidencias: List<Evidencia> = emptyList()
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private lateinit var tvEvidenceTitle: TextView
    private lateinit var detailContainer: LinearLayout

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && selectedAppointment != null) {
            lifecycleScope.launch {
                val fileName = "evidencia_${System.currentTimeMillis()}.jpg"
                val destFile = File(requireContext().filesDir, fileName)
                try {
                    requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(destFile).use { output ->
                            inputStream.copyTo(output)
                        }
                    }
                    val fileUri = Uri.fromFile(destFile)
                    val evidencia = Evidencia(citaId = selectedAppointment!!.hashCode(), uri = fileUri.toString())
                    val evidenciaDao = ErniCleanApplication.database.evidenciaDao()
                    evidenciaDao.insertarEvidencia(evidencia)
                    cargarEvidencias(selectedAppointment!!.hashCode())
                    android.widget.Toast.makeText(requireContext(), "Evidencia agregada", android.widget.Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.widget.Toast.makeText(requireContext(), "Error al guardar evidencia", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val editEvidenceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedAppointment?.let { appointment ->
                cargarEvidencias(appointment.hashCode())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_evidence, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCompletedAppointments)
        evidenceGallery = view.findViewById(R.id.evidenceGallery)
        tvEvidenceTitle = view.findViewById(R.id.tvEvidenceTitle)
        detailContainer = view.findViewById(R.id.detailContainer)
        adapter = AppointmentsAdapter(
            completedAppointments,
            onItemClick = { appointment ->
                selectedAppointment = appointment
                // Abrir la galería de evidencias para la cita
                val intent = Intent(requireContext(), EvidenceGalleryActivity::class.java)
                // Usar un ID numérico generado en lugar del ID de Firestore
                intent.putExtra("citaId", appointment.hashCode())
                startActivity(intent)
            },
            onCompleteClick = {},
            onPostponeClick = {},
            onEditClick = { appointment ->
                selectedAppointment = appointment
                pickMediaLauncher.launch("image/*")
            },
            showAddEvidenceButton = true
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        loadCompletedAppointments()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }

    private fun loadCompletedAppointments() {
        // Cargar citas completadas desde Firestore con listener en tiempo real
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        listenerRegistration?.remove() // Elimina el listener anterior si existe

        android.util.Log.d("Firestore", "Cargando citas completadas...")

        listenerRegistration = db.collection("CitasCompletadas")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.util.Log.e("Firestore", "Error al escuchar Firestore: ${e.message}")
                    android.widget.Toast.makeText(requireContext(), "Error al escuchar Firestore: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                
                android.util.Log.d("Firestore", "Citas completadas encontradas: ${snapshots?.size() ?: 0}")
                
                completedAppointments.clear()
                for (document in snapshots!!) {
                    val data = document.data
                    android.util.Log.d("Firestore", "Documento completado: ${document.id}, Datos: $data")
                    
                    // Mapeo más robusto de datos
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
                    
                    val appointment = Appointment(
                        id = document.id,
                        clientName = nombre,
                        clientPhone = telefono,
                        clientAddress = direccion,
                        serviceType = serviciosSeleccionados.joinToString(", "),
                        date = java.util.Date(),
                        extras = mensaje,
                        status = AppointmentStatus.COMPLETED
                    )
                    
                    // Filtrar citas vacías o con datos incompletos
                    if (nombre.isNotEmpty() && telefono.isNotEmpty() && direccion.isNotEmpty() && serviciosSeleccionados.isNotEmpty()) {
                        completedAppointments.add(appointment)
                        android.util.Log.d("Firestore", "Cita completada agregada: ${appointment.clientName}")
                    } else {
                        android.util.Log.d("Firestore", "Cita vacía filtrada: ${document.id}")
                    }
                }
                adapter.updateAppointments(completedAppointments)
                
                // Ocultar título y contenedor si no hay citas
                if (completedAppointments.isEmpty()) {
                    tvEvidenceTitle.visibility = View.GONE
                    detailContainer.visibility = View.GONE
                } else {
                    tvEvidenceTitle.visibility = View.VISIBLE
                    detailContainer.visibility = View.VISIBLE
                }
            }
    }

    private fun cargarEvidencias(citaId: Int) {
        lifecycleScope.launch {
            val evidenciaDao = ErniCleanApplication.database.evidenciaDao()
            evidencias = withContext(Dispatchers.IO) {
                evidenciaDao.getEvidenciasPorCita(citaId)
            }
            mostrarEvidencias()
        }
    }

    private fun mostrarEvidencias() {
        evidenceGallery.removeAllViews()
        for (evidencia in evidencias) {
            val container = LinearLayout(requireContext())
            container.orientation = LinearLayout.VERTICAL
            val imageView = ImageView(requireContext())
            imageView.layoutParams = LinearLayout.LayoutParams(200, 200)
            imageView.setPadding(8, 8, 8, 8)
            Log.d("EVIDENCIA", "Mostrando URI: ${evidencia.uri}")
            // Usar Coil para cargar la imagen
            imageView.load(Uri.parse(evidencia.uri))
            // Botones de editar y borrar
            val btnRow = LinearLayout(requireContext())
            btnRow.orientation = LinearLayout.HORIZONTAL
            val btnEdit = ImageView(requireContext())
            btnEdit.setImageResource(android.R.drawable.ic_menu_edit)
            btnEdit.setOnClickListener {
                val intent = Intent(requireContext(), EditEvidenceActivity::class.java)
                intent.putExtra("evidenceUri", evidencia.uri)
                intent.putExtra("evidenceId", evidencia.id)
                intent.putExtra("citaId", evidencia.citaId)
                intent.putExtra("evidenceType", "image")
                editEvidenceLauncher.launch(intent)
            }
            val btnDelete = ImageView(requireContext())
            btnDelete.setImageResource(android.R.drawable.ic_menu_delete)
            btnDelete.setOnClickListener {
                lifecycleScope.launch {
                    val evidenciaDao = ErniCleanApplication.database.evidenciaDao()
                    evidenciaDao.eliminarEvidencia(evidencia)
                    cargarEvidencias(evidencia.citaId)
                }
            }
            btnRow.addView(btnEdit)
            btnRow.addView(btnDelete)
            container.addView(imageView)
            container.addView(btnRow)
            evidenceGallery.addView(container)
        }
    }
} 