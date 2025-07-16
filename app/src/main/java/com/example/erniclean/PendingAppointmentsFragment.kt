package com.example.erniclean

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.Dialog
import android.widget.Button
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.Calendar
import java.util.Locale
import android.widget.Toast

class PendingAppointmentsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentsAdapter
    private var appointments = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pending_appointments, container, false)
        recyclerView = view.findViewById(R.id.appointmentsRecyclerView)
        adapter = AppointmentsAdapter(
            appointments = appointments,
            onItemClick = { /* Eliminado: showAppointmentDetails(appointment) */ },
            onCompleteClick = { appointment -> completeAppointment(appointment) },
            onPostponeClick = { appointment -> postponeAppointment(appointment) },
            onEditClick = { appointment -> editAppointment(appointment) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        loadPendingAppointments()
        return view
    }

    private fun loadPendingAppointments() {
        appointments.clear()
        appointments.add(Appointment("1", "Juan Pérez", "555-0123", "Calle Principal 123", "Pulido de Marmol", java.util.Date()))
        appointments.add(Appointment("2", "María García", "555-0124", "Avenida Central 456", "Limpieza General", java.util.Date()))
        appointments.add(Appointment("3", "Carlos Sánchez", "555-0125", "Plaza Mayor 789", "Desinfección", java.util.Date()))
        adapter.updateAppointments(appointments)
    }

    private fun showOptionsDialog(appointment: Appointment) {
        val options = arrayOf("Completar", "Posponer", "Editar", "Cancelar")
        AlertDialog.Builder(requireContext())
            .setTitle("Opciones de la cita")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> completeAppointment(appointment)
                    1 -> postponeAppointment(appointment)
                    2 -> editAppointment(appointment)
                    3 -> cancelAppointment(appointment)
                }
            }
            .setNegativeButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun completeAppointment(appointment: Appointment) {
        // Aquí puedes implementar la lógica para completar la cita
        appointment.status = AppointmentStatus.COMPLETED
        adapter.updateAppointments(appointments)
    }

    private fun postponeAppointment(appointment: Appointment) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_appointment_postpone, null)
        val calendarView = dialogView.findViewById<MaterialCalendarView>(R.id.calendarViewPostpone)
        // Oculta el topbar (título y flechas)
        val topbar = calendarView.getChildAt(0)
        topbar?.visibility = View.GONE
        val tvPendingCount = dialogView.findViewById<TextView>(R.id.tvPendingCount)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmPostpone)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelPostpone)

        // Configurar calendario en español y colores
        val locale = Locale("es", "ES")
        // Oculta el título y las flechas grises
        calendarView.setWeekDayFormatter { dayOfWeek ->
            when (dayOfWeek.value) {
                1 -> "Dom"
                2 -> "Lun"
                3 -> "Mar"
                4 -> "Mié"
                5 -> "Jue"
                6 -> "Vie"
                7 -> "Sáb"
                else -> ""
            }
        }
        // Color de selección
        calendarView.selectionColor = resources.getColor(R.color.cyan, null)
        // Decorador para días pasados: visibles pero no seleccionables
        val today = CalendarDay.today()
        calendarView.addDecorator(object : com.prolificinteractive.materialcalendarview.DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean {
                return day.isBefore(today)
            }
            override fun decorate(view: com.prolificinteractive.materialcalendarview.DayViewFacade) {
                view.setDaysDisabled(true)
                view.addSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#BDBDBD")))
            }
        })
        // Seleccionar la fecha actual de la cita
        val cal = Calendar.getInstance().apply { time = appointment.date }
        val selectedDay = CalendarDay.from(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        calendarView.selectedDate = selectedDay

        btnConfirm.isEnabled = false
        tvPendingCount.visibility = View.GONE

        calendarView.setOnDateChangedListener { _, date, _ ->
            // Habilitar solo si la fecha cambia
            btnConfirm.isEnabled = date != selectedDay
            // Contar citas pendientes ese día
            val count = appointments.count {
                it.status == AppointmentStatus.PENDING && isSameDay(it.date, date)
            }
            if (count > 0) {
                tvPendingCount.text = "Tienes $count cita(s) pendiente(s) ese día."
                tvPendingCount.visibility = View.VISIBLE
            } else {
                tvPendingCount.visibility = View.GONE
            }
        }

        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnConfirm.setOnClickListener {
            val newDate = calendarView.selectedDate?.date
            if (newDate != null && newDate != appointment.date) {
                // Conversión correcta usando solo org.threeten.bp
                val zoneId = org.threeten.bp.ZoneId.systemDefault()
                val instant = newDate.atStartOfDay(zoneId).toInstant()
                appointment.date = java.util.Date(instant.toEpochMilli())
                appointment.status = AppointmentStatus.POSTPONED
                adapter.updateAppointments(appointments)
                Toast.makeText(requireContext(), "Cita pospuesta", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun editAppointment(appointment: Appointment) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_appointment_edit, null)
        val editName = dialogView.findViewById<android.widget.EditText>(R.id.editClientName)
        val editPhone = dialogView.findViewById<android.widget.EditText>(R.id.editClientPhone)
        val editAddress = dialogView.findViewById<android.widget.EditText>(R.id.editClientAddress)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSaveEditAppointment)

        // Rellenar campos actuales
        editName.setText(appointment.clientName)
        editPhone.setText(appointment.clientPhone)
        editAddress.setText(appointment.clientAddress)

        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnSave.setOnClickListener {
            val newName = editName.text.toString().trim()
            val newPhone = editPhone.text.toString().trim()
            val newAddress = editAddress.text.toString().trim()
            if (newName.isEmpty() || newPhone.isEmpty() || newAddress.isEmpty()) {
                Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                appointment.clientName = newName
                appointment.clientPhone = newPhone
                appointment.clientAddress = newAddress
                adapter.updateAppointments(appointments)
                dialog.dismiss()
                Toast.makeText(requireContext(), "Cita actualizada", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun cancelAppointment(appointment: Appointment) {
        AlertDialog.Builder(requireContext())
            .setMessage("¿Seguro que deseas cancelar la cita?")
            .setPositiveButton("Sí") { dialog, _ ->
                appointments.remove(appointment)
                adapter.updateAppointments(appointments)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Función para comparar solo la fecha (sin hora)
    private fun isSameDay(date1: java.util.Date, calendarDay: CalendarDay): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        return cal1.get(Calendar.YEAR) == calendarDay.year &&
                cal1.get(Calendar.MONTH) + 1 == calendarDay.month &&
                cal1.get(Calendar.DAY_OF_MONTH) == calendarDay.day
    }
} 