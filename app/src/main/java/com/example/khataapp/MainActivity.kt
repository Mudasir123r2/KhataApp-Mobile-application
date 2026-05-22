package com.example.khataapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.khataapp.navigation.NavGraph
import com.example.khataapp.navigation.Routes
import com.example.khataapp.ui.components.BottomNavBar
import com.example.khataapp.ui.theme.KhataAppTheme

private val bottomNavRoutes = setOf(
    Routes.HOME, Routes.CUSTOMERS, Routes.REPORTS, Routes.PROFILE
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KhataAppTheme {
                val navController = rememberNavController()
                val currentRoute by navController.currentBackStackEntryAsState()
                val showBottomBar = currentRoute?.destination?.route in bottomNavRoutes

                Scaffold(
                    bottomBar = { if (showBottomBar) BottomNavBar(navController) }
                ) { padding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}
