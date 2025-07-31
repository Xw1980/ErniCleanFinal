package com.example.erniclean

import android.content.Context
import android.widget.Toast
import java.util.Date

class TestEmailService {
    
    companion object {
        fun testEmailSending(context: Context) {
            // Crear una cita de prueba
            val testAppointment = Appointment(
                id = "test_${System.currentTimeMillis()}",
                clientName = "Cliente de Prueba",
                clientPhone = "+34 123 456 789",
                clientAddress = "Calle de Prueba 123, Madrid",
                serviceType = "Limpieza General, Limpieza de Cocina",
                date = Date(),
                extras = "Esta es una cita de prueba para verificar el envío de correos",
                clientEmail = "tu-email-de-prueba@gmail.com" // Cambiar por tu email para probar
            )
            
            // Mostrar progreso
            Toast.makeText(context, "Enviando correo de prueba...", Toast.LENGTH_SHORT).show()
            
            // Probar el envío
            AppointmentConfirmationService.confirmAppointmentAndSendEmail(
                context = context,
                appointment = testAppointment,
                clientEmail = testAppointment.clientEmail,
                onSuccess = {
                    Toast.makeText(
                        context, 
                        "¡Correo de prueba enviado exitosamente! Revisa tu bandeja de entrada.", 
                        Toast.LENGTH_LONG
                    ).show()
                },
                onError = { error ->
                    Toast.makeText(
                        context, 
                        "Error al enviar correo de prueba: $error", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
        
        fun testPdfGeneration(context: Context) {
            // Crear una cita de prueba
            val testAppointment = Appointment(
                id = "test_pdf_${System.currentTimeMillis()}",
                clientName = "Cliente de Prueba PDF",
                clientPhone = "+34 123 456 789",
                clientAddress = "Calle de Prueba 123, Madrid",
                serviceType = "Limpieza General, Limpieza de Cocina, Limpieza de Baños",
                date = Date(),
                extras = "Esta es una cita de prueba para verificar la generación de PDF",
                clientEmail = "test@example.com"
            )
            
            try {
                val pdfFile = PdfGenerator.generateAppointmentTicket(context, testAppointment, testAppointment.clientEmail)
                Toast.makeText(
                    context, 
                    "PDF generado exitosamente: ${pdfFile.name}", 
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context, 
                    "Error al generar PDF: ${e.message}", 
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
} 