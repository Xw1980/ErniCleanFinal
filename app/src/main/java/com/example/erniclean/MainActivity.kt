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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_calendar, container, false)
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)
        val recyclerView = view.findViewById<RecyclerView>(R.id.mainAppointmentsRecyclerView)
        val title = view.findViewById<TextView>(R.id.titleMain)
        title.text = "Calendario y Citas"
        // Simulación de citas
        val appointments = listOf(
            Appointment("1", "Juan Pérez", "555-0123", "Calle Principal 123", "Pulido de Marmol", java.util.Date()),
            Appointment("2", "María García", "555-0124", "Avenida Central 456", "Limpieza General", java.util.Date())
        )
        val adapter = AppointmentsAdapter(
            appointments,
            onItemClick = {},
            onCompleteClick = {},
            onPostponeClick = {}
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        return view
    }
} 