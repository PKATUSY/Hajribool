package com.pkstudy.hajiribook.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pkstudy.hajiribook.ui.components.HajiriTopBar
import com.pkstudy.hajiribook.ui.theme.LightBackground
import com.pkstudy.hajiribook.ui.theme.PrimaryBlue
import com.pkstudy.hajiribook.ui.theme.SecondaryBlue
import com.pkstudy.hajiribook.ui.theme.TextPrimary
import com.pkstudy.hajiribook.ui.theme.TextSecondary

data class SettingItem(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val route: String
)

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateTo: (String) -> Unit
) {
    val settingsList = listOf(
        SettingItem("School Information", Icons.Default.School, SecondaryBlue, "school_info"),
        SettingItem("User & Role Management", Icons.Default.ManageAccounts, SecondaryBlue, "user_roles"),
        SettingItem("Face Recognition Settings", Icons.Default.Face, SecondaryBlue, "face_settings"),
        SettingItem("Attendance Settings", Icons.Default.HowToReg, Color(0xFFF59E0B), "attendance_settings"),
        SettingItem("Sync & Server (MySQL)", Icons.Default.Sync, SecondaryBlue, "sync"),
        SettingItem("Local Backup & Restore", Icons.Default.Backup, Color(0xFF0284C7), "backup"),
        SettingItem("App Preferences", Icons.Default.Settings, SecondaryBlue, "preferences"),
        SettingItem("Voice Announcement", Icons.Default.Campaign, Color(0xFF16A05A), "voice_settings"),
        SettingItem("Security (PIN / Password)", Icons.Default.Lock, Color(0xFF6B7280), "security"),
        SettingItem("About App", Icons.Default.Info, Color(0xFFF59E0B), "about")
    )

    Scaffold(
        topBar = {
            HajiriTopBar(
                title = "Settings",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            settingsList.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateTo(item.route) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = item.iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
