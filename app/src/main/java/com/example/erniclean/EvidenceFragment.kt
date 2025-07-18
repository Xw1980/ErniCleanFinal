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

class EvidenceFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentsAdapter
    private var completedAppointments = mutableListOf<Appointment>()
    private var selectedAppointment: Appointment? = null
    private lateinit var evidenceGallery: LinearLayout
    private var evidencias: List<Evidencia> = emptyList()

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris != null && uris.isNotEmpty() && selectedAppointment != null) {
            lifecycleScope.launch {
                val evidenciaDao = ErniCleanApplication.database.evidenciaDao()
                uris.forEach { uri ->
                    // Copiar la imagen a la carpeta interna de la app
                    val fileName = "evidencia_${System.currentTimeMillis()}.jpg"
                    val destFile = File(requireContext().filesDir, fileName)
                    try {
                        requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                            FileOutputStream(destFile).use { output ->
                                inputStream.copyTo(output)
                            }
                        }
                        val fileUri = Uri.fromFile(destFile)
                        Log.d("EVIDENCIA", "Guardando URI: $fileUri")
                        val evidencia = Evidencia(citaId = selectedAppointment!!.id.toInt(), uri = fileUri.toString())
                        evidenciaDao.insertarEvidencia(evidencia)
                    } catch (e: Exception) {
                        Log.e("EVIDENCIA", "Error copiando archivo: ${e.message}")
                    }
                }
                cargarEvidencias(selectedAppointment!!.id.toInt())
            }
        }
    }

    private val editEvidenceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && selectedAppointment != null) {
            cargarEvidencias(selectedAppointment!!.id.toInt())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_evidence, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCompletedAppointments)
        evidenceGallery = view.findViewById(R.id.evidenceGallery)
        adapter = AppointmentsAdapter(
            completedAppointments,
            onItemClick = { appointment ->
                selectedAppointment = appointment
                // Abrir la galería de evidencias para la cita
                val intent = Intent(requireContext(), EvidenceGalleryActivity::class.java)
                intent.putExtra("citaId", appointment.id.toInt())
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

    private fun loadCompletedAppointments() {
        completedAppointments.clear()
        // Aquí deberías cargar solo citas completadas desde tu fuente de datos
        completedAppointments.add(
            Appointment(
                "10", "Ejemplo Cliente", "555-0000", "Calle Ficticia 123", "Limpieza General", java.util.Date(), AppointmentStatus.COMPLETED
            )
        )
        adapter.updateAppointments(completedAppointments)
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