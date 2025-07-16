package com.example.erniclean

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.adapter.FragmentStateAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.Toast
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.Calendar
import java.util.Locale
import android.widget.Button
import android.app.Dialog
import android.widget.GridView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager = findViewById(R.id.viewPager)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        val fragments = listOf(
            MainCalendarFragment(),
            PendingAppointmentsFragment(),
            WebAppointmentsFragment()
        )
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> bottomNavigation.selectedItemId = R.id.nav_home
                    1 -> bottomNavigation.selectedItemId = R.id.nav_clock
                    2 -> bottomNavigation.selectedItemId = R.id.nav_calendar
                }
            }
        })
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> viewPager.currentItem = 0
                R.id.nav_clock -> viewPager.currentItem = 1
                R.id.nav_calendar -> viewPager.currentItem = 2
            }
            true
        }
    }
}

class MainCalendarFragment : Fragment() {
    private lateinit var appointments: MutableList<Appointment>
    private lateinit var adapter: AppointmentsAdapter
    private lateinit var calendarView: MaterialCalendarView
    private var selectedDay: CalendarDay? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_calendar, container, false)
        calendarView = view.findViewById(R.id.calendarView)
        // Oculta el topbar (título y flechas)
        val topbar = calendarView.getChildAt(0)
        topbar?.visibility = View.GONE
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
        val tvMonthTitle = view.findViewById<TextView>(R.id.tvMonthTitle)
        val btnPrevMonth = view.findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNextMonth = view.findViewById<ImageButton>(R.id.btnNextMonth)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewCitas)
        // Citas distribuidas en varios días del mes actual para pruebas
        val now = java.util.Calendar.getInstance()
        val year = now.get(java.util.Calendar.YEAR)
        val month = now.get(java.util.Calendar.MONTH)
        appointments = mutableListOf(
            Appointment("1", "Juan Pérez", "555-0123", "Calle Principal 123", "Pulido de Marmol", java.util.Date(year - 1900, month, 2)),
            Appointment("2", "María García", "555-0124", "Avenida Central 456", "Limpieza General", java.util.Date(year - 1900, month, 5)),
            Appointment("3", "Carlos Sánchez", "555-0125", "Plaza Mayor 789", "Desinfección", java.util.Date(year - 1900, month, 11)),
            Appointment("4", "Ana López", "555-0126", "Calle Sur 321", "Limpieza de Vidrios", java.util.Date(year - 1900, month, 15)),
            Appointment("5", "Luis Torres", "555-0127", "Boulevard Norte 654", "Pulido de Pisos", java.util.Date(year - 1900, month, 20))
        )
        appointments.sortBy { it.date }
        adapter = AppointmentsAdapter(
            appointments,
            onItemClick = { appointment -> showAppointmentDetails(appointment) },
            onCompleteClick = {},
            onPostponeClick = { appointment -> showPostponeDialog(appointment) },
            onEditClick = {} // No hace nada en este fragmento
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        // Forzar idioma español en el calendario principal
        val locale = Locale("es", "ES")
        fun updateMonthTitle() {
            val date = calendarView.currentDate.date
            val monthName = org.threeten.bp.Month.of(date.monthValue).getDisplayName(org.threeten.bp.format.TextStyle.FULL, locale)
            tvMonthTitle.text = "${monthName.replaceFirstChar { it.uppercase(locale) }} ${date.year}"
        }
        updateMonthTitle()
        tvMonthTitle.setOnClickListener {
            showMonthSelectorDialog(tvMonthTitle, ::updateMonthTitle)
        }
        btnPrevMonth.setOnClickListener {
            val date = calendarView.currentDate.date.minusMonths(1)
            calendarView.currentDate = CalendarDay.from(date)
            updateMonthTitle()
        }
        btnNextMonth.setOnClickListener {
            val date = calendarView.currentDate.date.plusMonths(1)
            calendarView.currentDate = CalendarDay.from(date)
            updateMonthTitle()
        }
        calendarView.setOnMonthChangedListener { _, _ ->
            updateMonthTitle()
            calendarView.invalidateDecorators()
        }
        // Seleccionar el día actual por defecto y mostrar citas de ese día
        selectedDay = CalendarDay.today()
        setupCalendarDecorators()
        return view
    }

    private fun setupCalendarDecorators() {
        val today = CalendarDay.today()
        if (selectedDay == null) selectedDay = today
        calendarView.selectedDate = selectedDay
        val pendingDates = appointments
            .filter { it.status == AppointmentStatus.PENDING }
            .map { appt ->
                val localDate = org.threeten.bp.LocalDate.of(
                    appt.date.year + 1900,
                    appt.date.month + 1,
                    appt.date.date
                )
                CalendarDay.from(localDate)
            }
        calendarView.removeDecorators()
        // Decorador para el día actual (círculo grande cyan)
        calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean {
                return day == today
            }
            override fun decorate(view: DayViewFacade) {
                val radius = 32f
                val drawable = GradientDrawable()
                drawable.shape = GradientDrawable.OVAL
                drawable.setColor(Color.parseColor("#00BCD4"))
                drawable.setSize(radius.toInt(), radius.toInt())
                view.setBackgroundDrawable(drawable)
            }
        })
        // Decorador para dots cyan en días con citas pendientes
        calendarView.addDecorator(EventDecorator(Color.parseColor("#00BCD4"), pendingDates))
        // Decorador para opacar días de meses pasados y días de otros meses en la cuadrícula (texto gris claro)
        calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean {
                val currentMonth = calendarView.currentDate.month
                val currentYear = calendarView.currentDate.year
                // Días de meses pasados o días de otros meses visibles en la cuadrícula
                return (day.year < currentYear) || (day.year == currentYear && day.month < currentMonth) || (day.month != currentMonth)
            }
            override fun decorate(view: DayViewFacade) {
                // Cambia solo el color del texto a gris claro
                view.addSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#BDBDBD")))
            }
        })
        calendarView.setOnDateChangedListener { _, date, _ ->
            selectedDay = date
            updateAppointmentsForSelectedDay()
            setupCalendarDecorators()
        }
        calendarView.setOnMonthChangedListener { _, _ ->
            calendarView.invalidateDecorators()
        }
        updateAppointmentsForSelectedDay()
    }

    private fun updateAppointmentsForSelectedDay() {
        val day = selectedDay
        if (day == null) {
            adapter.updateAppointments(emptyList())
            return
        }
        val filtered = appointments.filter {
            it.date.isSameDay(day)
        }.sortedBy { it.date }
        adapter.updateAppointments(filtered)
    }

    private fun showAppointmentDetails(appointment: Appointment) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_appointment_details, null)
        dialogView.findViewById<TextView>(R.id.detailClientName).text = appointment.clientName
        dialogView.findViewById<TextView>(R.id.detailClientPhone).text = appointment.clientPhone
        dialogView.findViewById<TextView>(R.id.detailClientAddress).text = appointment.clientAddress
        dialogView.findViewById<TextView>(R.id.detailServiceType).text = appointment.serviceType

        android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("CERRAR") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showPostponeDialog(appointment: Appointment) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_appointment_postpone, null)
        val calendarDialog = dialogView.findViewById<MaterialCalendarView>(R.id.calendarViewPostpone)
        val tvPendingCount = dialogView.findViewById<TextView>(R.id.tvPendingCount)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmPostpone)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelPostpone)

        val locale = Locale("es", "ES")
        calendarDialog.setTitleFormatter { day ->
            val localDate = day.date
            val monthName = org.threeten.bp.Month.of(localDate.monthValue).getDisplayName(org.threeten.bp.format.TextStyle.FULL, locale)
            "${monthName.replaceFirstChar { it.uppercase(locale) }} ${localDate.year}"
        }
        calendarDialog.setWeekDayFormatter { dayOfWeek ->
            val symbols = java.text.DateFormatSymbols(locale)
            val shortWeekdays = symbols.shortWeekdays
            shortWeekdays[dayOfWeek.value % 7 + 1].uppercase(locale)
        }
        calendarDialog.selectionColor = resources.getColor(R.color.cyan, null)
        // Solo permitir seleccionar fechas futuras
        calendarDialog.state().edit().setMinimumDate(CalendarDay.today()).commit()
        val cal = Calendar.getInstance().apply { time = appointment.date }
        val selectedDay = CalendarDay.from(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        calendarDialog.selectedDate = selectedDay

        btnConfirm.isEnabled = false
        tvPendingCount.visibility = View.GONE

        calendarDialog.setOnDateChangedListener { _, date, _ ->
            btnConfirm.isEnabled = date != selectedDay
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

        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)

        btnConfirm.setOnClickListener {
            val newDate = calendarDialog.selectedDate?.date
            if (newDate != null && newDate != appointment.date) {
                val zoneId = org.threeten.bp.ZoneId.systemDefault()
                val instant = newDate.atStartOfDay(zoneId).toInstant()
                appointment.date = java.util.Date(instant.toEpochMilli())
                appointment.status = AppointmentStatus.PENDING
                appointments.sortBy { it.date }
                setupCalendarDecorators()
                Toast.makeText(requireContext(), "Cita pospuesta", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showMonthSelectorDialog(tvMonthTitle: TextView, updateMonthTitle: () -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_selector, null)
        val gridMonths = dialogView.findViewById<GridView>(R.id.gridMonths)
        val months = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, months)
        gridMonths.adapter = adapter
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        gridMonths.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val currentYear = calendarView.currentDate.year
            val newMonth = position + 1
            val newDate = org.threeten.bp.LocalDate.of(currentYear, newMonth, 1)
            calendarView.currentDate = CalendarDay.from(newDate)
            updateMonthTitle()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun isSameDay(date1: java.util.Date, calendarDay: CalendarDay): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        return cal1.get(Calendar.YEAR) == calendarDay.year &&
                cal1.get(Calendar.MONTH) + 1 == calendarDay.month &&
                cal1.get(Calendar.DAY_OF_MONTH) == calendarDay.day
    }
}

// Función de extensión para comparar solo la fecha (sin hora)
fun java.util.Date.isSameDay(day: CalendarDay): Boolean {
    val cal1 = java.util.Calendar.getInstance().apply { time = this@isSameDay }
    val cal2 = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.YEAR, day.year)
        set(java.util.Calendar.MONTH, day.month - 1)
        set(java.util.Calendar.DAY_OF_MONTH, day.day)
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
}

fun java.util.Calendar.setToStartOfDay() {
    set(java.util.Calendar.HOUR_OF_DAY, 0)
    set(java.util.Calendar.MINUTE, 0)
    set(java.util.Calendar.SECOND, 0)
    set(java.util.Calendar.MILLISECOND, 0)
}

class EventDecorator(private val color: Int, private val dates: Collection<CalendarDay>) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(com.prolificinteractive.materialcalendarview.spans.DotSpan(8f, color))
    }
} 