package com.example.simplecalculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private var currentInput = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvDisplay = findViewById(R.id.tvDisplay)

        val buttonIds = listOf(
            R.id.btn0, R.id.btn00, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide,
            R.id.btnOpenBracket, R.id.btnCloseBracket, R.id.btnDot
        )

        for (id in buttonIds) {
            findViewById<Button>(id).setOnClickListener { view ->
                val button = view as Button
                val buttonText = button.text.toString()
                if (currentInput == "0") {
                    currentInput = buttonText
                } else {
                    currentInput += buttonText
                }
                tvDisplay.text = currentInput
            }
        }

        // Clear button (C)
        findViewById<Button>(R.id.btnC).setOnClickListener {
            currentInput = "0"
            tvDisplay.text = currentInput
        }

        // Equals button (=)
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            if (currentInput == "0") {
                currentInput = "="
            } else {
                currentInput += "="
            }
            tvDisplay.text = currentInput
        }
    }
}
