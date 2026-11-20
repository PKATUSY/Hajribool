package com.pkstudy.hajiribook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pkstudy.hajiribook.ui.theme.PrimaryBlue
import com.pkstudy.hajiribook.ui.theme.PrimaryGreen
import com.pkstudy.hajiribook.ui.theme.StatusError

data class DrawerMenuItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false
)

@Composable
fun HajiriDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val items = listOf(
        DrawerMenuItem("home", "Home", Icons.Default.Home),
        DrawerMenuItem("add_student", "Add +", Icons.Default.Add),
        DrawerMenuItem("students", "Students", Icons.Default.People),
        DrawerMenuItem("attendance", "Attendance", Icons.Default.EventNote),
        DrawerMenuItem("reports", "Reports", Icons.Default.Assessment),
        DrawerMenuItem("history", "History", Icons.Default.History),
        DrawerMenuItem("sync", "Sync Data", Icons.Default.Sync),
        DrawerMenuItem("settings", "Settings", Icons.Default.Settings),
        DrawerMenuItem("help", "Help & Support", Icons.AutoMirrored.Filled.Help),
        DrawerMenuItem("logout", "Logout", Icons.AutoMirrored.Filled.ExitToApp, isDestructive = true)
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(PrimaryBlue)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // Drawer Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = "Hajiri Book Logo",
                tint = PrimaryGreen,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Hajiri Book",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "SMART ATTENDANCE SYSTEM",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Items
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            val itemBg = if (isSelected) PrimaryGreen else Color.Transparent
            val contentColor = if (item.isDestructive) StatusError else Color.White

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(itemBg)
                    .clickable {
                        if (item.route == "logout") {
                            onLogout()
                        } else {
                            onNavigate(item.route)
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = item.title,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
