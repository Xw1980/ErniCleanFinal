package com.example.erniclean

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import java.io.File
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailSender {
    
    companion object {
        private const val TAG = "EmailSender"
        
        // Configuración del servidor SMTP (Gmail)
        private const val SMTP_HOST = EmailConfig.SMTP_HOST
        private const val SMTP_PORT = EmailConfig.SMTP_PORT
        
        // Credenciales del correo de la empresa
        private const val COMPANY_EMAIL = EmailConfig.COMPANY_EMAIL
        private const val COMPANY_PASSWORD = EmailConfig.COMPANY_PASSWORD
        
        fun sendAppointmentConfirmation(
            context: Context,
            clientEmail: String,
            appointment: Appointment,
            pdfFile: File,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {
            SendEmailTask(context, clientEmail, appointment, pdfFile, onSuccess, onError).execute()
        }
        
        private class SendEmailTask(
            private val context: Context,
            private val clientEmail: String,
            private val appointment: Appointment,
            private val pdfFile: File,
            private val onSuccess: () -> Unit,
            private val onError: (String) -> Unit
        ) : AsyncTask<Void, Void, Boolean>() {
            
            private var errorMessage: String = ""
            
            override fun doInBackground(vararg params: Void?): Boolean {
                return try {
                    val properties = Properties().apply {
                        put("mail.smtp.auth", "true")
                        put("mail.smtp.starttls.enable", "true")
                        put("mail.smtp.host", SMTP_HOST)
                        put("mail.smtp.port", SMTP_PORT)
                    }
                    
                    val session = Session.getInstance(properties, object : Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication {
                            return PasswordAuthentication(COMPANY_EMAIL, COMPANY_PASSWORD)
                        }
                    })
                    
                    val message = MimeMessage(session).apply {
                        setFrom(InternetAddress(COMPANY_EMAIL))
                        setRecipients(Message.RecipientType.TO, InternetAddress.parse(clientEmail))
                        subject = "Confirmación de Cita - ERNICLEAN"
                        
                        // Crear el contenido del mensaje
                        val multipart = MimeMultipart()
                        
                        // Parte del texto
                        val textPart = MimeBodyPart().apply {
                            setText(createEmailBody(appointment), "UTF-8")
                        }
                        multipart.addBodyPart(textPart)
                        
                        // Parte del PDF adjunto
                        val attachmentPart = MimeBodyPart().apply {
                            attachFile(pdfFile)
                            fileName = "Comprobante_Cita_${appointment.id}.pdf"
                        }
                        multipart.addBodyPart(attachmentPart)
                        
                        setContent(multipart)
                    }
                    
                    Transport.send(message)
                    true
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending email: ${e.message}", e)
                    // Proporcionar mensajes de error más específicos
                    errorMessage = when {
                        e.message?.contains("authentication", ignoreCase = true) == true -> 
                            "Error de autenticación de email. Verificar credenciales."
                        e.message?.contains("smtp", ignoreCase = true) == true -> 
                            "Error de conexión SMTP. Verificar configuración."
                        e.message?.contains("timeout", ignoreCase = true) == true -> 
                            "Timeout en conexión de email."
                        else -> "Error al enviar email: ${e.message}"
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
            
            private fun createEmailBody(appointment: Appointment): String {
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", java.util.Locale("es", "ES"))
                val appointmentDate = dateFormat.format(appointment.date)
                
                return """
                    Hola ${appointment.clientName},
                    
                    Su cita ha sido confirmada exitosamente.
                    
                    Detalles de la cita:
                    • Fecha y Hora: $appointmentDate
                    • Dirección: ${appointment.clientAddress}
                    • Servicios: ${appointment.serviceType}
                    
                    Adjunto encontrará el comprobante de su cita en formato PDF.
                    
                    IMPORTANTE:
                    • Si necesita cancelar o reprogramar, contáctenos con anticipación
                    • Nuestro equipo llegará con todos los materiales necesarios
                    
                    Para cualquier consulta, no dude en contactarnos.
                    
                    Gracias por confiar en ERNICLEAN
                    
                    Saludos cordiales,
                    Equipo ERNICLEAN
                """.trimIndent()
            }
        }
    }
} 