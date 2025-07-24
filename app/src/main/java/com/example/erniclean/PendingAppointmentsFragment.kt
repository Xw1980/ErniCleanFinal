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
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import android.widget.LinearLayout
import android.widget.ImageButton
import android.widget.GridView
import android.graphics.Color
import android.view.Gravity
import android.widget.ArrayAdapter
import android.content.Intent

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
            appointments,
            onItemClick = { /* No hacer nada al clickear la tarjeta */ },
            onCompleteClick = { appointment -> completeAppointment(appointment) },
            onPostponeClick = { appointment -> postponeAppointment(appointment) },
            onEditClick = { appointment -> editAppointment(appointment) },
            showAddEvidenceButton = false,
            showEditOption = true
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
        // Elimina cualquier línea que oculte el topbar del calendario en postponeAppointment
        val tvPendingCount = dialogView.findViewById<TextView>(R.id.tvPendingCount)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmPostpone)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelPostpone)

        // Configurar calendario en español y colores
        val locale = Locale("es", "ES")
        // Limpia todos los decoradores previos para evitar bugs visuales
        calendarView.removeDecorators()
        // Días de la semana en español
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
        val todayDate = org.threeten.bp.LocalDate.now()
        calendarView.addDecorator(object : com.prolificinteractive.materialcalendarview.DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean {
                val localDate = day.date
                // Solo deshabilita días pasados si el mes y año son iguales o anteriores al actual
                return localDate.isBefore(todayDate)
            }
            override fun decorate(view: com.prolificinteractive.materialcalendarview.DayViewFacade) {
                view.setDaysDisabled(true)
                view.addSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#BDBDBD")))
            }
        })
        // Decorador para opacar días de otros meses en la cuadrícula (texto gris claro)
        calendarView.addDecorator(object : com.prolificinteractive.materialcalendarview.DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean {
                val currentMonth = calendarView.currentDate.month
                val currentYear = calendarView.currentDate.year
                return (day.year < currentYear) || (day.year == currentYear && day.month < currentMonth) || (day.month != currentMonth)
            }
            override fun decorate(view: com.prolificinteractive.materialcalendarview.DayViewFacade) {
                view.addSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#BDBDBD")))
            }
        })
        // Decorador para dots cyan en días con citas pendientes (dot más pequeño)
        val pendingDates = appointments
            .filter { it.status == AppointmentStatus.PENDING }
            .map { appt ->
                val localDate = org.threeten.bp.LocalDate.of(
                    appt.date.year + 1900,
                    appt.date.month + 1,
                    appt.date.date
                )
                com.prolificinteractive.materialcalendarview.CalendarDay.from(localDate)
            }
        calendarView.addDecorator(EventDecorator(android.graphics.Color.parseColor("#00BCD4"), pendingDates, 2f))
        calendarView.invalidateDecorators()
        // No establecer mínimo de fecha en el estado del calendario para permitir navegar entre meses
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

        fun refreshDecorators() {
            calendarView.removeDecorators()
            // Días de la semana en español
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
            calendarView.selectionColor = resources.getColor(R.color.cyan, null)
            val todayDate = org.threeten.bp.LocalDate.now()
            calendarView.addDecorator(object : com.prolificinteractive.materialcalendarview.DayViewDecorator {
                override fun shouldDecorate(day: CalendarDay): Boolean {
                    val localDate = day.date
                    return localDate.isBefore(todayDate)
                }
                override fun decorate(view: com.prolificinteractive.materialcalendarview.DayViewFacade) {
                    view.setDaysDisabled(true)
                    view.addSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#BDBDBD")))
                }
            })
            calendarView.addDecorator(object : com.prolificinteractive.materialcalendarview.DayViewDecorator {
                override fun shouldDecorate(day: CalendarDay): Boolean {
                    val currentMonth = calendarView.currentDate.month
                    val currentYear = calendarView.currentDate.year
                    return (day.year < currentYear) || (day.year == currentYear && day.month < currentMonth) || (day.month != currentMonth)
                }
                override fun decorate(view: com.prolificinteractive.materialcalendarview.DayViewFacade) {
                    view.addSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#BDBDBD")))
                }
            })
            val pendingDates = appointments
                .filter { it.status == AppointmentStatus.PENDING }
                .map { appt ->
                    val localDate = org.threeten.bp.LocalDate.of(
                        appt.date.year + 1900,
                        appt.date.month + 1,
                        appt.date.date
                    )
                    com.prolificinteractive.materialcalendarview.CalendarDay.from(localDate)
                }
            calendarView.addDecorator(EventDecorator(android.graphics.Color.parseColor("#00BCD4"), pendingDates, 2f))
            calendarView.invalidateDecorators()
        }
        refreshDecorators()
        calendarView.setOnMonthChangedListener { _, _ ->
            refreshDecorators()
        }

        // Oculta el topbar del calendario (mes en inglés y flechas grises)
        val topbar = calendarView.getChildAt(0)
        topbar?.visibility = View.GONE

        // Agrega un LinearLayout horizontal con flechas y título personalizado
        val customMonthBar = LinearLayout(requireContext())
        customMonthBar.id = View.generateViewId()
        customMonthBar.orientation = LinearLayout.HORIZONTAL
        customMonthBar.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        customMonthBar.gravity = Gravity.CENTER

        val btnPrev = ImageButton(requireContext())
        btnPrev.setImageResource(R.drawable.ic_chevron_left)
        btnPrev.background = null
        btnPrev.setColorFilter(resources.getColor(R.color.cyan, null))
        val btnNext = ImageButton(requireContext())
        btnNext.setImageResource(R.drawable.ic_chevron_right)
        btnNext.background = null
        btnNext.setColorFilter(resources.getColor(R.color.cyan, null))

        val tvMonthTitle = TextView(requireContext())
        tvMonthTitle.id = View.generateViewId()
        tvMonthTitle.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tvMonthTitle.gravity = Gravity.CENTER
        tvMonthTitle.textSize = 20f
        tvMonthTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        tvMonthTitle.setTextColor(resources.getColor(R.color.cyan, null))

        customMonthBar.addView(btnPrev)
        customMonthBar.addView(tvMonthTitle)
        customMonthBar.addView(btnNext)

        // Buscar el LinearLayout raíz recorriendo los hijos del MaterialCardView
        var rootLayout: LinearLayout? = null
        for (i in 0 until (dialogView as ViewGroup).childCount) {
            val child = dialogView.getChildAt(i)
            if (child is LinearLayout) {
                rootLayout = child
                break
            }
        }
        val calendarIndex = rootLayout?.let { (0 until it.childCount).firstOrNull { idx -> it.getChildAt(idx).id == R.id.calendarViewPostpone } } ?: -1
        if (rootLayout != null && calendarIndex != -1) {
            rootLayout.addView(customMonthBar, calendarIndex)
        } else if (rootLayout != null) {
            rootLayout.addView(customMonthBar)
        }

        // Mostrar el mes en español
        fun updateMonthTitleES() {
            val locale = Locale("es", "ES")
            val date = calendarView.currentDate.date
            val monthName = org.threeten.bp.Month.of(date.monthValue).getDisplayName(org.threeten.bp.format.TextStyle.FULL, locale)
            tvMonthTitle.text = "${monthName.replaceFirstChar { it.uppercase(locale) }} ${date.year}"
        }
        updateMonthTitleES()
        // Flechas para cambiar de mes
        btnPrev.setOnClickListener {
            val date = calendarView.currentDate.date.minusMonths(1)
            calendarView.currentDate = com.prolificinteractive.materialcalendarview.CalendarDay.from(date)
            updateMonthTitleES()
        }
        btnNext.setOnClickListener {
            val date = calendarView.currentDate.date.plusMonths(1)
            calendarView.currentDate = com.prolificinteractive.materialcalendarview.CalendarDay.from(date)
            updateMonthTitleES()
        }
        // Al hacer click en el título, abrir el selector moderno
        tvMonthTitle.setOnClickListener {
            showMonthYearSelectorDialog(calendarView)
        }
        // Actualizar el título al cambiar de mes
        calendarView.setOnMonthChangedListener { _, _ ->
            updateMonthTitleES()
            refreshDecorators()
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
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_appointment, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvEditDialogTitle)
        val tvDay = dialogView.findViewById<TextView>(R.id.tvEditDialogDay)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etEditName)
        val etPhone = dialogView.findViewById<android.widget.EditText>(R.id.etEditPhone)
        val etAddress = dialogView.findViewById<android.widget.EditText>(R.id.etEditAddress)
        val etService = dialogView.findViewById<android.widget.EditText>(R.id.etEditService)
        val etExtras = dialogView.findViewById<android.widget.EditText>(R.id.etEditExtras)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnEditConfirm)
        val btnNo = dialogView.findViewById<Button>(R.id.btnEditNo)

        // Título y día
        tvTitle.text = "CITAS"
        val cal = java.util.Calendar.getInstance().apply { time = appointment.date }
        val dayOfWeek = cal.getDisplayName(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.LONG, java.util.Locale("es"))?.uppercase() ?: ""
        val dayNumber = cal.get(java.util.Calendar.DAY_OF_MONTH)
        tvDay.text = "$dayOfWeek $dayNumber"

        // Rellenar campos actuales
        etName.setText(appointment.clientName)
        etPhone.setText(appointment.clientPhone)
        etAddress.setText(appointment.clientAddress)
        etService.setText(appointment.serviceType)
        etExtras.setText(appointment.extras ?: "")

        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnConfirm.setOnClickListener {
            appointment.clientName = etName.text.toString().trim()
            appointment.clientPhone = etPhone.text.toString().trim()
            appointment.clientAddress = etAddress.text.toString().trim()
            appointment.serviceType = etService.text.toString().trim()
            // Para extras, si la data class Appointment tiene val, hay que crear una copia
            try {
                appointment.extras = etExtras.text.toString().trim()
            } catch (_: Exception) {}
            adapter.updateAppointments(appointments)
            dialog.dismiss()
            Toast.makeText(requireContext(), "Cita actualizada", Toast.LENGTH_SHORT).show()
        }
        btnNo.setOnClickListener { dialog.dismiss() }
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

    // Selector moderno de mes y año para el calendario de posponer cita
    private fun showMonthYearSelectorDialog(calendarView: com.prolificinteractive.materialcalendarview.MaterialCalendarView) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_selector, null)
        val header = dialogView.findViewById<LinearLayout>(R.id.headerYearSelector)
        val btnPrevYearRange = dialogView.findViewById<ImageButton>(R.id.btnPrevYearRange)
        val btnNextYearRange = dialogView.findViewById<ImageButton>(R.id.btnNextYearRange)
        val tvYearRange = dialogView.findViewById<TextView>(R.id.tvYearRange)
        val gridYears = dialogView.findViewById<GridView>(R.id.gridYears)
        val gridMonths = dialogView.findViewById<GridView>(R.id.gridMonths)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)

        val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        val currentYear = calendarView.currentDate.year
        val currentMonth = calendarView.currentDate.month
        var yearRangeStart = (currentYear / 12) * 12
        var selectedYear = currentYear

        fun updateYearGrid() {
            val years = (yearRangeStart until yearRangeStart + 12).toList()
            tvYearRange.text = "${yearRangeStart}-${yearRangeStart + 11}"
            gridYears.visibility = View.VISIBLE
            gridMonths.visibility = View.GONE
            val adapter = object : ArrayAdapter<Int>(requireContext(), android.R.layout.simple_list_item_1, years) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent) as TextView
                    view.text = years[position].toString()
                    view.gravity = Gravity.CENTER
                    view.textSize = 18f
                    view.setPadding(0, 24, 0, 24)
                    view.setTextColor(if (years[position] == currentYear) resources.getColor(R.color.cyan, null) else Color.BLACK)
                    return view
                }
            }
            gridYears.adapter = adapter
        }

        fun showMonthGrid(year: Int) {
            selectedYear = year
            gridYears.visibility = View.GONE
            gridMonths.visibility = View.VISIBLE
            val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, months) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent) as TextView
                    view.text = months[position]
                    view.gravity = Gravity.CENTER
                    view.textSize = 18f
                    view.setPadding(0, 24, 0, 24)
                    view.setTextColor(if (selectedYear == currentYear && position + 1 == currentMonth) resources.getColor(R.color.cyan, null) else Color.BLACK)
                    return view
                }
            }
            gridMonths.adapter = adapter
        }

        updateYearGrid()

        btnPrevYearRange.setOnClickListener {
            yearRangeStart -= 12
            updateYearGrid()
        }
        btnNextYearRange.setOnClickListener {
            yearRangeStart += 12
            updateYearGrid()
        }
        gridYears.setOnItemClickListener { _, _, position, _ ->
            val year = yearRangeStart + position
            showMonthGrid(year)
        }
        gridMonths.setOnItemClickListener { _, _, position, _ ->
            val month = position + 1
            val newDate = org.threeten.bp.LocalDate.of(selectedYear, month, 1)
            calendarView.currentDate = com.prolificinteractive.materialcalendarview.CalendarDay.from(newDate)
            dialog.dismiss()
        }
        dialog.show()
    }
} 