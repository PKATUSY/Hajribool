package com.pkstudy.hajiribook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pkstudy.hajiribook.ui.navigation.HajiriNavGraph
import com.pkstudy.hajiribook.ui.theme.HajiriBookTheme
import com.pkstudy.hajiribook.ui.theme.LightBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HajiriApp

        setContent {
            HajiriBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LightBackground
                ) {
                    val navController = rememberNavController()
                    HajiriNavGraph(navController = navController, app = app)
                }
            }
        }
    }
}
