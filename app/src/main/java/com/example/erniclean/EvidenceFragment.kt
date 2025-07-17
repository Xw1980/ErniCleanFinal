package com.example.erniclean

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    val evidencia = Evidencia(citaId = selectedAppointment!!.id.toInt(), uri = uri.toString())
                    evidenciaDao.insertarEvidencia(evidencia)
                }
                cargarEvidencias(selectedAppointment!!.id.toInt())
            }
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
                cargarEvidencias(appointment.id.toInt())
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
            val imageView = ImageView(requireContext())
            imageView.layoutParams = LinearLayout.LayoutParams(200, 200)
            imageView.setPadding(8, 8, 8, 8)
            imageView.setImageURI(Uri.parse(evidencia.uri))
            imageView.setOnClickListener {
                val intent = android.content.Intent(requireContext(), EditEvidenceActivity::class.java)
                intent.putExtra("evidenceUri", evidencia.uri)
                intent.putExtra("evidenceId", evidencia.id)
                intent.putExtra("citaId", evidencia.citaId)
                intent.putExtra("evidenceType", "image")
                editEvidenceLauncher.launch(intent)
            }
            evidenceGallery.addView(imageView)
        }
    }

    private val editEvidenceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val data = result.data
            val action = data?.getStringExtra("action")
            val evidenceId = data?.getIntExtra("evidenceId", -1) ?: -1
            val citaId = selectedAppointment?.id?.toInt() ?: -1
            when (action) {
                "delete", "save" -> {
                    if (citaId != -1) cargarEvidencias(citaId)
                }
            }
        }
    }
} 