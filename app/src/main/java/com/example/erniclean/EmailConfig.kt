package com.example.erniclean

object EmailConfig {
    // Configuración del servidor SMTP (Gmail)
    const val SMTP_HOST = "smtp.gmail.com"
    const val SMTP_PORT = "587"
    
    // ============================================
    // IMPORTANTE: CONFIGURAR CREDENCIALES REALES
    // ============================================
    
    // Credenciales del correo de la empresa
    // Cambiar estas credenciales por las reales de la empresa
    
    // Ejemplo: const val COMPANY_EMAIL = "erniclean@gmail.com"
    const val COMPANY_EMAIL = "erniclean.trabajos@gmail.com" // Cambiar por el email real
    
    // Ejemplo: const val COMPANY_PASSWORD = "abcd efgh ijkl mnop"
    // IMPORTANTE: Usar contraseña de aplicación de Gmail, NO la contraseña normal
    const val COMPANY_PASSWORD = "jxdn waag eeui csme" // Cambiar por la contraseña de aplicación de Gmail
    
    // ============================================
    // INSTRUCCIONES PARA CONFIGURAR GMAIL:
    // ============================================
    // 1. Ve a tu cuenta de Google
    // 2. Seguridad > Verificación en dos pasos (activar si no está activada)
    // 3. Seguridad > Verificación en dos pasos > Contraseñas de aplicación
    // 4. Selecciona "Otra" y escribe "ErniClean App"
    // 5. Copia la contraseña de 16 caracteres generada
    // 6. Pégala en COMPANY_PASSWORD arriba
    // ============================================
} 