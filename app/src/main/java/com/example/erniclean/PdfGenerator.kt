package com.example.erniclean

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument.PageInfo
import android.graphics.pdf.PdfDocument.Page
import android.graphics.Canvas
import android.graphics.Rect
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator {
    
    companion object {
        fun generateAppointmentTicket(context: Context, appointment: Appointment, clientEmail: String): File {
            val fileName = "cita_${appointment.id}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            
            val pdfDocument = PdfDocument()
            val pageInfo = PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Configurar el fondo blanco
            canvas.drawColor(Color.WHITE)
            
            val paint = Paint()
            val rect = Rect()
            
            // Configurar fuente para el título
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 24f
            paint.color = Color.rgb(0, 188, 212) // Cyan color
            
            // Título principal
            val title = "ERNI-CLEAN"
            paint.getTextBounds(title, 0, title.length, rect)
            val titleX = (595 - rect.width()) / 2f
            canvas.drawText(title, titleX, 80f, paint)
            
            // Subtítulo
            paint.textSize = 16f
            paint.color = Color.BLACK
            val subtitle = "COMPROBANTE DE CITA"
            paint.getTextBounds(subtitle, 0, subtitle.length, rect)
            val subtitleX = (595 - rect.width()) / 2f
            canvas.drawText(subtitle, subtitleX, 120f, paint)
            
            // Línea separadora
            paint.strokeWidth = 2f
            canvas.drawLine(50f, 140f, 545f, 140f, paint)
            
            // Información de la cita
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.strokeWidth = 1f
            
            val dateFormat = SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale("es", "ES"))
            val appointmentDate = dateFormat.format(appointment.date)
            
            var yPosition = 180f
            val lineHeight = 25f
            
            // Función para dibujar una línea de información
            fun drawInfoLine(label: String, value: String) {
                // Label en negrita
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.BLACK
                canvas.drawText(label, 60f, yPosition, paint)
                
                // Valor normal
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(64, 64, 64)
                canvas.drawText(value, 200f, yPosition, paint)
                
                yPosition += lineHeight
            }
            
            drawInfoLine("Fecha y Hora:", appointmentDate)
            drawInfoLine("Cliente:", appointment.clientName)
            drawInfoLine("Teléfono:", appointment.clientPhone)
            drawInfoLine("Dirección:", appointment.clientAddress)
            drawInfoLine("Servicios:", appointment.serviceType)
            drawInfoLine("Email:", clientEmail)
            
            // Mensaje adicional (si existe)
            val extras = appointment.extras
            if (!extras.isNullOrEmpty()) {
                drawInfoLine("Notas:", extras)
            }
            
            // Línea separadora
            yPosition += 10f
            canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
            
            // Información adicional
            yPosition += 30f
            paint.textSize = 10f
            paint.color = Color.BLACK
            
            val infoText = """
                IMPORTANTE:
                • Si necesita cancelar o reprogramar, contáctenos con anticipación
                • Nuestro equipo llegará con todos los materiales necesarios
            """.trimIndent()
            
            val infoLines = infoText.split("\n")
            for (line in infoLines) {
                canvas.drawText(line, 60f, yPosition, paint)
                yPosition += 15f
            }
            
            // Pie de página
            yPosition += 20f
            paint.textSize = 9f
            paint.color = Color.rgb(128, 128, 128)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            
            val footerText = """
                Gracias por confiar en ERNI-CLEAN
                Para consultas: +34 XXX XXX XXX
                www.erniclean.com
            """.trimIndent()
            
            val footerLines = footerText.split("\n")
            for (line in footerLines) {
                paint.getTextBounds(line, 0, line.length, rect)
                val footerX = (595 - rect.width()) / 2f
                canvas.drawText(line, footerX, yPosition, paint)
                yPosition += 15f
            }
            
            pdfDocument.finishPage(page)
            
            // Guardar el PDF
            try {
                val fileOutputStream = FileOutputStream(file)
                pdfDocument.writeTo(fileOutputStream)
                fileOutputStream.close()
            } finally {
                pdfDocument.close()
            }
            
            return file
        }
    }
} 