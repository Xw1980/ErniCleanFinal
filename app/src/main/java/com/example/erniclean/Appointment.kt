package com.example.erniclean

import java.util.Date

data class Appointment(
    val id: String,
    val clientName: String,
    val clientPhone: String,
    val clientAddress: String,
    val serviceType: String,
    var date: Date,
    var status: AppointmentStatus = AppointmentStatus.PENDING,
    val extras: String? = null,
    val evidenceUrls: List<String> = emptyList()
)

enum class AppointmentStatus {
    PENDING,
    COMPLETED,
    POSTPONED
} 