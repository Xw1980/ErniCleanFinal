package com.example.erniclean

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.Gravity
import android.view.ViewGroup
import android.graphics.Color

class AllAppointmentsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = ConstraintLayout(this)
        layout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layout.setBackgroundColor(Color.WHITE)
        val textView = TextView(this)
        textView.text = "Todas las Citas"
        textView.textSize = 22f
        textView.setTextColor(Color.parseColor("#8832cfd1")) // cyan opaco
        textView.gravity = Gravity.CENTER
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        textView.layoutParams = params
        layout.addView(textView)
        setContentView(layout)
    }
} 