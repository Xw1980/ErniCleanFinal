package com.example.erniclean

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EvidenceGalleryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddEvidence: ImageButton
    private lateinit var btnSelectEvidence: ImageButton
    private lateinit var btnDeleteEvidence: ImageButton
    private var citaId: Int = -1
    private var evidencias: List<Evidencia> = emptyList()
    private var selectionMode = false
    private val selectedItems = mutableSetOf<Int>()
    private val pickMediaLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris != null && uris.isNotEmpty()) {
            lifecycleScope.launch {
                val evidenciaDao = ErniCleanApplication.database.evidenciaDao()
                for (uri in uris) {
                    val mimeType = contentResolver.getType(uri) ?: ""
                    val isVideo = mimeType.startsWith("video")
                    val ext = if (isVideo) ".mp4" else ".jpg"
                    val fileName = "evidencia_${System.currentTimeMillis()}$ext"
                    val destFile = File(filesDir, fileName)
                    try {
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            FileOutputStream(destFile).use { output ->
                                inputStream.copyTo(output)
                            }
                        }
                        val fileUri = Uri.fromFile(destFile)
                        val evidencia = Evidencia(citaId = citaId, uri = fileUri.toString())
                        evidenciaDao.insertarEvidencia(evidencia)
                    } catch (e: Exception) {
                        Toast.makeText(this@EvidenceGalleryActivity, "Error copiando archivo", Toast.LENGTH_SHORT).show()
                    }
                }
                cargarEvidencias()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evidence_gallery)
        recyclerView = findViewById(R.id.recyclerViewGallery)
        btnAddEvidence = findViewById(R.id.btnAddEvidence)
        btnSelectEvidence = findViewById(R.id.btnSelectEvidence)
        btnDeleteEvidence = findViewById(R.id.btnDeleteEvidence)
        citaId = intent.getIntExtra("citaId", -1)
        recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(3, 24, true))
        btnAddEvidence.setOnClickListener {
            pickMediaLauncher.launch(arrayOf("image/*", "video/*"))
        }
        btnSelectEvidence.setOnClickListener {
            selectionMode = !selectionMode
            selectedItems.clear()
            updateFabMenu()
            recyclerView.adapter = EvidenceAdapter(evidencias, selectionMode, selectedItems, ::onItemClick)
        }
        btnDeleteEvidence.setOnClickListener {
            if (selectedItems.isNotEmpty()) {
                lifecycleScope.launch {
                    val evidenciaDao = ErniCleanApplication.database.evidenciaDao()
                    val toDelete = evidencias.filter { selectedItems.contains(it.id) }
                    toDelete.forEach { evidenciaDao.eliminarEvidencia(it) }
                    selectionMode = false
                    selectedItems.clear()
                    cargarEvidencias()
                }
            }
        }
        updateFabMenu()
        cargarEvidencias()
    }

    private fun updateFabMenu() {
        btnDeleteEvidence.visibility = if (selectionMode && selectedItems.isNotEmpty()) View.VISIBLE else View.GONE
        btnSelectEvidence.setImageResource(if (selectionMode) android.R.drawable.checkbox_on_background else android.R.drawable.ic_menu_agenda)
    }

    private fun cargarEvidencias() {
        lifecycleScope.launch {
            val evidenciaDao = ErniCleanApplication.database.evidenciaDao()
            evidencias = withContext(Dispatchers.IO) {
                evidenciaDao.getEvidenciasPorCita(citaId)
            }
            recyclerView.adapter = EvidenceAdapter(evidencias, selectionMode, selectedItems, ::onItemClick)
            updateFabMenu()
        }
    }

    private fun onItemClick(evidencia: Evidencia) {
        if (selectionMode) {
            if (selectedItems.contains(evidencia.id)) {
                selectedItems.remove(evidencia.id)
            } else {
                selectedItems.add(evidencia.id)
            }
            recyclerView.adapter?.notifyDataSetChanged()
            updateFabMenu()
        } else {
            // Mostrar previsualización en un DialogFragment
            EvidencePreviewDialogFragment.newInstance(evidencia.uri).show(supportFragmentManager, "preview")
        }
    }

    class EvidenceAdapter(
        private val items: List<Evidencia>,
        private val selectionMode: Boolean,
        private val selectedItems: Set<Int>,
        private val onItemClick: (Evidencia) -> Unit
    ) : RecyclerView.Adapter<EvidenceAdapter.EvidenceViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvidenceViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evidence_gallery, parent, false)
            return EvidenceViewHolder(view)
        }
        override fun getItemCount() = items.size
        override fun onBindViewHolder(holder: EvidenceViewHolder, position: Int) {
            holder.bind(items[position], selectionMode, selectedItems, onItemClick)
        }
        class EvidenceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val imageView: ImageView = view.findViewById(R.id.ivEvidence)
            private val playIcon: ImageView = view.findViewById(R.id.ivPlayIcon)
            private val selectionOverlay: View = view.findViewById(R.id.selectionOverlay)
            fun bind(evidencia: Evidencia, selectionMode: Boolean, selectedItems: Set<Int>, onItemClick: (Evidencia) -> Unit) {
                val isVideo = evidencia.uri.endsWith(".mp4")
                if (isVideo) {
                    imageView.setImageResource(android.R.drawable.ic_media_play)
                    playIcon.visibility = View.VISIBLE
                } else {
                    imageView.load(Uri.parse(evidencia.uri))
                    playIcon.visibility = View.GONE
                }
                selectionOverlay.visibility = if (selectionMode && selectedItems.contains(evidencia.id)) View.VISIBLE else View.GONE
                itemView.setOnClickListener { onItemClick(evidencia) }
            }
        }
    }
}

// Agregar clase para el espacio entre ítems
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount
        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            if (position < spanCount) outRect.top = spacing
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) outRect.top = spacing
        }
    }
}

// Agregar DialogFragment para previsualización
class EvidencePreviewDialogFragment : androidx.fragment.app.DialogFragment() {
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_evidence_preview, container, false)
        val uri = requireArguments().getString("uri") ?: return view
        val imageView = view.findViewById<ImageView>(R.id.previewImageView)
        val videoView = view.findViewById<VideoView>(R.id.previewVideoView)
        if (uri.endsWith(".mp4")) {
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            videoView.setVideoURI(Uri.parse(uri))
            videoView.start()
        } else {
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            imageView.load(Uri.parse(uri))
        }
        // Cerrar al tocar fuera
        view.setOnClickListener { dismiss() }
        return view
    }
    companion object {
        fun newInstance(uri: String): EvidencePreviewDialogFragment {
            val frag = EvidencePreviewDialogFragment()
            val args = Bundle()
            args.putString("uri", uri)
            frag.arguments = args
            return frag
        }
    }
} 