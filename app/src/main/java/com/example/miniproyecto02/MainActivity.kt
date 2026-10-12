package com.example.miniproyecto02

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.miniproyecto02.ui.TareasScreen
import com.example.miniproyecto02.ui.theme.MiniProyecto02Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniProyecto02Theme {
                TareasScreen()
            }
        }
    }
}
