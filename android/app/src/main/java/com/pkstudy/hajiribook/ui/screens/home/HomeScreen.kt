package com.pkstudy.hajiribook.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pkstudy.hajiribook.ui.components.HajiriBottomNav
import com.pkstudy.hajiribook.ui.components.HajiriTopBar
import com.pkstudy.hajiribook.ui.components.QuickActionButton
import com.pkstudy.hajiribook.ui.components.StatCard
import com.pkstudy.hajiribook.ui.theme.LightBackground
import com.pkstudy.hajiribook.ui.theme.PrimaryBlue
import com.pkstudy.hajiribook.ui.theme.PrimaryGreen
import com.pkstudy.hajiribook.ui.theme.SecondaryBlue
import com.pkstudy.hajiribook.ui.theme.StatAbsentBg
import com.pkstudy.hajiribook.ui.theme.StatPercentBg
import com.pkstudy.hajiribook.ui.theme.StatPresentBg
import com.pkstudy.hajiribook.ui.theme.StatTotalBg
import com.pkstudy.hajiribook.ui.theme.StatusError
import com.pkstudy.hajiribook.ui.theme.StatusSuccess
import com.pkstudy.hajiribook.ui.theme.TextPrimary
import com.pkstudy.hajiribook.ui.theme.TextSecondary
import com.pkstudy.hajiribook.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val user by viewModel.user.collectAsState()

    Scaffold(
        topBar = {
            HajiriTopBar(
                title = "Hajiri Book",
                onMenuClick = onOpenDrawer,
                onNotificationClick = { onNavigate("notifications") },
                onProfileClick = { onNavigate("profile") }
            )
        },
        bottomBar = {
            HajiriBottomNav(
                currentRoute = "home",
                onNavigateToRoute = onNavigate
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Welcome Section
            Text(
                text = "Welcome, ${user?.fullName ?: "Madhurendra Kumar"} 👋",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Role : ${user?.role ?: "Teacher"} | Class : ${user?.assignedClasses ?: "6th - 10th"}",
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Stats Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    count = stats.totalStudents.toString(),
                    label = "Total",
                    bgColor = StatTotalBg,
                    textColor = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    count = stats.presentStudents.toString(),
                    label = "Present",
                    bgColor = StatPresentBg,
                    textColor = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    count = stats.absentStudents.toString(),
                    label = "Absent",
                    bgColor = StatAbsentBg,
                    textColor = StatusError,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    count = "${stats.attendancePercentage}%",
                    label = "Attendance",
                    bgColor = StatPercentBg,
                    textColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Face Attendance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Face Attendance",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Identify & Mark Automatically",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onNavigate("face_scan") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text(
                                text = "Start Scanning",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions Grid (8 items in 2 rows of 4)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Actions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Add,
                            label = "Add +",
                            onClick = { onNavigate("add_student") }
                        )
                        QuickActionButton(
                            icon = Icons.Default.HowToReg,
                            label = "Face\nRegistration",
                            onClick = { onNavigate("face_register") }
                        )
                        QuickActionButton(
                            icon = Icons.Default.People,
                            label = "Student\nList",
                            onClick = { onNavigate("students") }
                        )
                        QuickActionButton(
                            icon = Icons.Default.Class,
                            label = "Class\nManagement",
                            onClick = { onNavigate("classes") }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.HowToReg,
                            label = "Attendance",
                            onClick = { onNavigate("attendance") }
                        )
                        QuickActionButton(
                            icon = Icons.Default.Assessment,
                            label = "Reports",
                            onClick = { onNavigate("reports") }
                        )
                        QuickActionButton(
                            icon = Icons.Default.Sync,
                            label = "Sync",
                            onClick = { onNavigate("sync") }
                        )
                        QuickActionButton(
                            icon = Icons.Default.Backup,
                            label = "Backup",
                            onClick = { onNavigate("settings") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
