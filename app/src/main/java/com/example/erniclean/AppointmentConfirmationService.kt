package com.example.erniclean

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import java.io.File

class AppointmentConfirmationService {
    
    companion object {
        private const val TAG = "AppointmentConfirmation"
        
        fun confirmAppointmentAndSendEmail(
            context: Context,
            appointment: Appointment,
            clientEmail: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {
            ConfirmationTask(context, appointment, clientEmail, onSuccess, onError).execute()
        }
        
        private class ConfirmationTask(
            private val context: Context,
            private val appointment: Appointment,
            private val clientEmail: String,
            private val onSuccess: () -> Unit,
            private val onError: (String) -> Unit
        ) : AsyncTask<Void, Void, Boolean>() {
            
            private var errorMessage: String = ""
            
            override fun doInBackground(vararg params: Void?): Boolean {
                return try {
                    // Paso 1: Generar el PDF
                    Log.d(TAG, "Generando PDF para la cita ${appointment.id}")
                    val pdfFile = PdfGenerator.generateAppointmentTicket(context, appointment, clientEmail)
                    
                    // Paso 2: Enviar el correo con el PDF adjunto
                    Log.d(TAG, "Enviando correo a $clientEmail")
                    
                    // Usar un callback para manejar el resultado del envío de correo
                    var emailSent = false
                    var emailError = ""
                    
                    EmailSender.sendAppointmentConfirmation(
                        context = context,
                        clientEmail = clientEmail,
                        appointment = appointment,
                        pdfFile = pdfFile,
                        onSuccess = {
                            emailSent = true
                            Log.d(TAG, "Correo enviado exitosamente")
                        },
                        onError = { error ->
                            emailError = error
                            Log.e(TAG, "Error enviando correo: $error")
                        }
                    )
                    
                    // Esperar un poco para que el correo se envíe
                    Thread.sleep(2000)
                    
                    if (emailSent) {
                        // Limpiar el archivo PDF temporal
                        if (pdfFile.exists()) {
                            pdfFile.delete()
                        }
                        true
                    } else {
                        errorMessage = emailError
                        false
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error en el proceso de confirmación: ${e.message}", e)
                    // Determinar si es un error de PDF o de email
                    when {
                        e.message?.contains("PDF", ignoreCase = true) == true -> {
                            errorMessage = "Error al generar PDF: ${e.message}"
                        }
                        e.message?.contains("email", ignoreCase = true) == true -> {
                            errorMessage = "Error al enviar email: ${e.message}"
                        }
                        e.message?.contains("smtp", ignoreCase = true) == true -> {
                            errorMessage = "Error de configuración de email: ${e.message}"
                        }
                        else -> {
                            errorMessage = "Error en el proceso: ${e.message}"
                        }
                    }
                    false
                }
            }
            
            override fun onPostExecute(success: Boolean) {
                if (success) {
                    onSuccess()
                } else {
                    onError(errorMessage)
                }
            }
        }
    }
} 