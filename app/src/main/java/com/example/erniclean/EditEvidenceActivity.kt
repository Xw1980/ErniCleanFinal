package com.example.erniclean

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.activity.result.contract.ActivityResultContracts

class EditEvidenceActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var videoView: VideoView
    private lateinit var btnDelete: ImageButton
    private lateinit var btnReplace: ImageButton
    private lateinit var btnSave: Button
    private lateinit var tvReplace: TextView
    private lateinit var tvDelete: TextView
    private lateinit var tvSave: TextView
    private var evidenceUri: Uri? = null
    private var isVideo: Boolean = false

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            evidenceUri = uri
            showEvidence()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_evidence)

        imageView = findViewById(R.id.editEvidenceImageView)
        videoView = findViewById(R.id.editEvidenceVideoView)
        btnDelete = findViewById(R.id.btnDeleteEvidence)
        btnReplace = findViewById(R.id.btnReplaceEvidence)
        btnSave = findViewById(R.id.btnSaveEvidence)
        tvReplace = findViewById(R.id.tvReplace)
        tvDelete = findViewById(R.id.tvDelete)
        tvSave = findViewById(R.id.tvSave)

        val uriString = intent.getStringExtra("evidenceUri")
        val type = intent.getStringExtra("evidenceType")
        evidenceUri = if (uriString != null) Uri.parse(uriString) else null
        isVideo = type == "video"
        showEvidence()

        btnReplace.setOnClickListener {
            val mimeType = if (isVideo) "video/*" else "image/*"
            pickMediaLauncher.launch(mimeType)
        }
        btnDelete.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("action", "delete")
            })
            finish()
        }
        btnSave.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("action", "save")
                putExtra("evidenceUri", evidenceUri.toString())
                putExtra("evidenceType", if (isVideo) "video" else "image")
            })
            finish()
        }
    }

    private fun showEvidence() {
        if (evidenceUri == null) return
        if (isVideo) {
            imageView.isVisible = false
            videoView.isVisible = true
            videoView.setVideoURI(evidenceUri)
            videoView.start()
        } else {
            imageView.isVisible = true
            videoView.isVisible = false
            imageView.setImageURI(evidenceUri)
        }
    }
} 