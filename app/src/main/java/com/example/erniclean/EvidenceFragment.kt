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
    private lateinit var detailContainer: LinearLayout
    private lateinit var tvDetailName: TextView
    private lateinit var tvDetailPhone: TextView
    private lateinit var tvDetailAddress: TextView
    private lateinit var tvDetailService: TextView
    private lateinit var evidenceGallery: LinearLayout
    private lateinit var btnAddEvidence: ImageView
    private var selectedAppointment: Appointment? = null
    private val evidenceUris = mutableListOf<Uri>()
    private var editEvidenceIndex: Int = -1

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris != null && uris.isNotEmpty()) {
            evidenceUris.addAll(uris)
            showEvidenceGallery()
        }
    }

    private val editEvidenceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val data = result.data
            val action = data?.getStringExtra("action")
            val index = data?.getIntExtra("evidenceIndex", -1) ?: -1
            when (action) {
                "delete" -> {
                    if (index in evidenceUris.indices) {
                        evidenceUris.removeAt(index)
                        showEvidenceGallery()
                    }
                }
                "save" -> {
                    val newUriString = data.getStringExtra("evidenceUri")
                    if (index in evidenceUris.indices && newUriString != null) {
                        evidenceUris[index] = Uri.parse(newUriString)
                        showEvidenceGallery()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_evidence, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCompletedAppointments)
        detailContainer = view.findViewById(R.id.detailContainer)
        tvDetailName = view.findViewById(R.id.tvDetailName)
        tvDetailPhone = view.findViewById(R.id.tvDetailPhone)
        tvDetailAddress = view.findViewById(R.id.tvDetailAddress)
        tvDetailService = view.findViewById(R.id.tvDetailService)
        evidenceGallery = view.findViewById(R.id.evidenceGallery)
        btnAddEvidence = view.findViewById(R.id.btnAddEvidence)

        adapter = AppointmentsAdapter(
            completedAppointments,
            onItemClick = { appointment -> showAppointmentDetail(appointment) },
            onCompleteClick = {},
            onPostponeClick = {},
            onEditClick = {}
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnAddEvidence.setOnClickListener {
            pickMediaLauncher.launch("image/*")
        }

        loadCompletedAppointments()
        showListMode()
        return view
    }

    private fun loadCompletedAppointments() {
        completedAppointments.clear()
        completedAppointments.add(
            Appointment(
                "10", "Ejemplo Cliente", "555-0000", "Calle Ficticia 123", "Limpieza General", java.util.Date(), AppointmentStatus.COMPLETED
            )
        )
        adapter.updateAppointments(completedAppointments)
    }

    private fun showAppointmentDetail(appointment: Appointment) {
        selectedAppointment = appointment
        tvDetailName.text = appointment.clientName
        tvDetailPhone.text = appointment.clientPhone
        tvDetailAddress.text = appointment.clientAddress
        tvDetailService.text = appointment.serviceType
        evidenceUris.clear()
        showEvidenceGallery()
        showDetailMode()
    }

    private fun showEvidenceGallery() {
        evidenceGallery.removeAllViews()
        for ((index, uri) in evidenceUris.withIndex()) {
            val mimeType = requireContext().contentResolver.getType(uri) ?: ""
            val isVideo = mimeType.startsWith("video")
            val view = if (isVideo) {
                val videoView = VideoView(requireContext())
                videoView.layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    setMargins(8, 8, 8, 8)
                }
                videoView.setVideoURI(uri)
                videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    videoView.start()
                }
                videoView
            } else {
                val imageView = ImageView(requireContext())
                imageView.layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    setMargins(8, 8, 8, 8)
                }
                imageView.setImageURI(uri)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView
            }
            view.setOnClickListener {
                val intent = Intent(requireContext(), EditEvidenceActivity::class.java)
                intent.putExtra("evidenceUri", uri.toString())
                intent.putExtra("evidenceType", if (isVideo) "video" else "image")
                intent.putExtra("evidenceIndex", index)
                editEvidenceIndex = index
                editEvidenceLauncher.launch(intent)
            }
            evidenceGallery.addView(view)
        }
    }

    private fun showListMode() {
        recyclerView.isVisible = true
        detailContainer.isVisible = false
    }

    private fun showDetailMode() {
        recyclerView.isVisible = false
        detailContainer.isVisible = true
    }

    fun onBackPressed(): Boolean {
        return if (detailContainer.isVisible) {
            showListMode()
            true
        } else {
            false
        }
    }
} 