package com.example.erniclean

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EvidenceFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentsAdapter
    private var completedAppointments = mutableListOf<Appointment>()
    // Eliminar referencias a detailContainer, tvDetailName, tvDetailPhone, tvDetailAddress, tvDetailService, evidenceGallery, btnAddEvidence, selectedAppointment, evidenceUris, editEvidenceIndex

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris != null && uris.isNotEmpty()) {
            // Eliminar la lógica de guardar evidencia
        }
    }

    private val editEvidenceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val data = result.data
            val action = data?.getStringExtra("action")
            val index = data?.getIntExtra("evidenceIndex", -1) ?: -1
            when (action) {
                "delete" -> {
                    // Eliminar la lógica de eliminar evidencia
                }
                "save" -> {
                    // Eliminar la lógica de guardar evidencia
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_evidence, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCompletedAppointments)
        adapter = AppointmentsAdapter(
            completedAppointments,
            onItemClick = {},
            onCompleteClick = {},
            onPostponeClick = {},
            onEditClick = { appointment ->
                // Al presionar '+', abrir la galería para esa cita
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
        // Eliminar la lógica de mostrar siempre el detalle de la primera cita completada si existe
    }

    // Eliminar showAppointmentDetail, showEvidenceGallery, showListMode, showDetailMode, onBackPressed
} 