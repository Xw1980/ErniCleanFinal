# Configuración de Envío de Correos Electrónicos

## Descripción
Esta funcionalidad permite enviar automáticamente un PDF con los detalles de la cita al correo del cliente cuando se confirma una cita.

## Configuración Requerida

### 1. Configurar Gmail para Envío de Correos

Para que la aplicación pueda enviar correos desde Gmail, necesitas configurar una "Contraseña de aplicación":

1. **Habilitar Verificación en Dos Pasos**:
   - Ve a tu cuenta de Google
   - Seguridad > Verificación en dos pasos
   - Activa la verificación en dos pasos si no está activada

2. **Generar Contraseña de Aplicación**:
   - Ve a Seguridad > Verificación en dos pasos
   - Desplázate hacia abajo y selecciona "Contraseñas de aplicación"
   - Selecciona "Otra" en el menú desplegable
   - Escribe "ErniClean App" como nombre
   - Haz clic en "Generar"
   - **Guarda la contraseña de 16 caracteres que se genera**

### 2. Configurar Credenciales en la Aplicación

Edita el archivo `app/src/main/java/com/example/erniclean/EmailConfig.kt`:

```kotlin
object EmailConfig {
    const val SMTP_HOST = "smtp.gmail.com"
    const val SMTP_PORT = "587"
    
    // Cambiar estas credenciales por las reales
    const val COMPANY_EMAIL = "tu-email@gmail.com" // Tu correo de Gmail
    const val COMPANY_PASSWORD = "tu-app-password" // La contraseña de aplicación generada
}
```

### 3. Verificar Permisos

Asegúrate de que los siguientes permisos estén en `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

## Funcionamiento

### Cuando se Confirma una Cita:

1. **Generación del PDF**: Se crea automáticamente un PDF profesional con:
   - Logo y nombre de la empresa
   - Detalles completos de la cita
   - Información del cliente
   - Instrucciones importantes
   - Información de contacto

2. **Envío del Correo**: Se envía un correo al cliente con:
   - Asunto: "Confirmación de Cita - ERNI-CLEAN"
   - Cuerpo del mensaje con detalles de la cita
   - PDF adjunto como comprobante

### Estructura del PDF:

- **Encabezado**: Logo y nombre de la empresa
- **Información de la Cita**:
  - Fecha y hora
  - Nombre del cliente
  - Teléfono
  - Dirección
  - Servicios contratados
  - Email del cliente
  - Notas adicionales (si las hay)
- **Instrucciones importantes** para el cliente
- **Pie de página** con información de contacto

## Solución de Problemas

### Error de Autenticación:
- Verifica que la verificación en dos pasos esté activada
- Asegúrate de usar la contraseña de aplicación, no tu contraseña normal
- Verifica que el correo y contraseña estén correctamente escritos

### Error de Conexión:
- Verifica que el dispositivo tenga conexión a internet
- Asegúrate de que los permisos de internet estén configurados

### Error de Envío:
- Verifica que el correo del cliente esté bien formateado
- Revisa los logs de la aplicación para más detalles

## Personalización

### Cambiar el Diseño del PDF:
Edita la clase `PdfGenerator.kt` para modificar:
- Colores y fuentes
- Información mostrada
- Diseño del layout

### Cambiar el Contenido del Correo:
Edita la clase `EmailSender.kt` en la función `createEmailBody()` para modificar:
- Asunto del correo
- Cuerpo del mensaje
- Formato del texto

## Notas Importantes

- **Seguridad**: Nunca subas las credenciales reales al repositorio
- **Pruebas**: Prueba primero con tu propio correo antes de usar en producción
- **Backup**: Guarda las credenciales en un lugar seguro
- **Actualización**: Si cambias la contraseña de Gmail, necesitarás generar una nueva contraseña de aplicación 