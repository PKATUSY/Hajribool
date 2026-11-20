package com.pkstudy.hajiribook.ui.screens.addstudent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pkstudy.hajiribook.ui.components.HajiriTopBar
import com.pkstudy.hajiribook.ui.theme.LightBackground
import com.pkstudy.hajiribook.ui.theme.PrimaryBlue
import com.pkstudy.hajiribook.ui.theme.PrimaryGreen
import com.pkstudy.hajiribook.ui.theme.SecondaryBlue
import com.pkstudy.hajiribook.ui.theme.TextSecondary
import com.pkstudy.hajiribook.viewmodel.StudentViewModel

@Composable
fun AddStudentScreen(
    viewModel: StudentViewModel,
    onNavigateBack: () -> Unit,
    onStudentSaved: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("9th") }
    var selectedGender by remember { mutableStateOf("Male") }
    var dob by remember { mutableStateOf("15/08/2010") }
    var mobile by remember { mutableStateOf("") }

    var classExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    val classList = listOf("6th", "7th", "8th", "9th", "10th", "11th", "12th")
    val genderList = listOf("Male", "Female", "Other")

    Scaffold(
        topBar = {
            HajiriTopBar(
                title = "Add Student",
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photo Avatar with Camera Button
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCBD5E1))
                    .clickable { /* Trigger camera or gallery */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
                // Small green camera icon badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Add Photo",
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = SecondaryBlue)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryBlue,
                    unfocusedBorderColor = Color(0xFFD1D5DB)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Roll Number
            OutlinedTextField(
                value = rollNumber,
                onValueChange = { rollNumber = it },
                label = { Text("Roll Number") },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = SecondaryBlue)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryBlue,
                    unfocusedBorderColor = Color(0xFFD1D5DB)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Class Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = "Class ($selectedClass)",
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(Icons.Default.Class, contentDescription = null, tint = SecondaryBlue)
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { classExpanded = true }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().clickable { classExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryBlue,
                        unfocusedBorderColor = Color(0xFFD1D5DB)
                    )
                )
                DropdownMenu(
                    expanded = classExpanded,
                    onDismissRequest = { classExpanded = false }
                ) {
                    classList.forEach { cls ->
                        DropdownMenuItem(
                            text = { Text(cls) },
                            onClick = {
                                selectedClass = cls
                                classExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gender Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedGender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    leadingIcon = {
                        Icon(Icons.Default.Wc, contentDescription = null, tint = SecondaryBlue)
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { genderExpanded = true }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().clickable { genderExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryBlue,
                        unfocusedBorderColor = Color(0xFFD1D5DB)
                    )
                )
                DropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genderList.forEach { gen ->
                        DropdownMenuItem(
                            text = { Text(gen) },
                            onClick = {
                                selectedGender = gen
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date of Birth
            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SecondaryBlue)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryBlue,
                    unfocusedBorderColor = Color(0xFFD1D5DB)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mobile (Optional)
            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = { Text("Mobile (Optional)") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = SecondaryBlue)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryBlue,
                    unfocusedBorderColor = Color(0xFFD1D5DB)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Student Button
            Button(
                onClick = {
                    if (fullName.isNotBlank()) {
                        viewModel.addStudent(
                            fullName = fullName,
                            rollNumber = if (rollNumber.isNotBlank()) rollNumber else "1",
                            className = selectedClass,
                            gender = selectedGender,
                            dob = dob,
                            mobile = mobile,
                            onSuccess = onStudentSaved
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(
                    text = "✔  Save Student",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}
