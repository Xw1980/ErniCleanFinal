package com.example.erniclean

import java.util.Date

data class Appointment(
    val id: String,
    var clientName: String,
    var clientPhone: String,
    var clientAddress: String,
    var serviceType: String,
    var date: Date,
    var status: AppointmentStatus = AppointmentStatus.PENDING,
    var extras: String? = null,
    val evidenceUrls: List<String> = emptyList()
)

enum class AppointmentStatus {
    PENDING,
    COMPLETED,
    POSTPONED
} 