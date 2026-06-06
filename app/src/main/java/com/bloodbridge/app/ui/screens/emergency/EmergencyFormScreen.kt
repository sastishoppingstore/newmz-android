package com.bloodbridge.app.ui.screens.emergency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyFormScreen(
    repository: BloodBridgeRepository,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    var patientName by remember { mutableStateOf("") }
    var bloodGroupId by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }
    var units by remember { mutableStateOf("1") }
    var urgency by remember { mutableStateOf("high") }
    var contact by remember { mutableStateOf("") }
    var attendantName by remember { mutableStateOf("") }
    var doctorNote by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val urgencies = listOf("critical", "very_high", "high")
    val bloodGroups = listOf("1", "2", "3", "4", "5", "6", "7", "8")
    val bloodGroupLabels = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    var bgExpanded by remember { mutableStateOf(false) }
    var urgencyExpanded by remember { mutableStateOf(false) }

    fun submit() {
        if (patientName.isBlank() || hospital.isBlank() || contact.isBlank() || attendantName.isBlank()) {
            errorMsg = "Please fill all required fields"
            return
        }
        isLoading = true
        errorMsg = null
        scope.launch {
            val result = repository.submitEmergencyRequest(
                patientName = patientName.trim(),
                bloodGroup = bloodGroupId.toIntOrNull() ?: 0,
                city = city.trim(),
                hospital = hospital.trim(),
                units = units.toIntOrNull() ?: 1,
                urgency = urgency,
                contact = contact.trim(),
                attendantName = attendantName.trim(),
                doctorNote = doctorNote.trim()
            )
            isLoading = false
            result.fold(
                onSuccess = { onSubmitSuccess() },
                onFailure = { errorMsg = it.message }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Emergency Request", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BloodRed.copy(alpha = 0.05f))
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Warning, null, tint = BloodRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fill this form for urgent blood requirements. We'll notify nearby donors.", color = GrayDark, fontSize = 13.sp)
                }
            }

            OutlinedTextField(value = patientName, onValueChange = { patientName = it; errorMsg = null },
                label = { Text("Patient Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(expanded = bgExpanded, onExpandedChange = { bgExpanded = it }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(value = if (bloodGroupId.isEmpty()) "" else
                        bloodGroupLabels[bloodGroups.indexOf(bloodGroupId).coerceAtLeast(0)],
                        onValueChange = {}, readOnly = true,
                        label = { Text("Blood Group") }, leadingIcon = { Icon(Icons.Default.Bloodtype, null, tint = BloodRed) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bgExpanded) },
                        modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed))
                    ExposedDropdownMenu(expanded = bgExpanded, onDismissRequest = { bgExpanded = false }) {
                        bloodGroups.forEachIndexed { i, g ->
                            DropdownMenuItem(text = { Text(bloodGroupLabels[i]) },
                                onClick = { bloodGroupId = g; bgExpanded = false })
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = urgencyExpanded, onExpandedChange = { urgencyExpanded = it }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(value = urgency.replace("_", " ").uppercase(), onValueChange = {}, readOnly = true,
                        label = { Text("Urgency") }, leadingIcon = { Icon(Icons.Default.Speed, null, tint = BloodRed) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = urgencyExpanded) },
                        modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed))
                    ExposedDropdownMenu(expanded = urgencyExpanded, onDismissRequest = { urgencyExpanded = false }) {
                        urgencies.forEach { u ->
                            DropdownMenuItem(text = { Text(u.replace("_", " ").uppercase()) },
                                onClick = { urgency = u; urgencyExpanded = false })
                        }
                    }
                }
            }

            OutlinedTextField(value = city, onValueChange = { city = it },
                label = { Text("City") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = BloodRed) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed))

            OutlinedTextField(value = hospital, onValueChange = { hospital = it; errorMsg = null },
                label = { Text("Hospital Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.LocalHospital, null, tint = BloodRed) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = units, onValueChange = { units = it },
                    label = { Text("Units") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed))

                OutlinedTextField(value = contact, onValueChange = { contact = it; errorMsg = null },
                    label = { Text("Contact *") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = BloodRed) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed))
            }

            OutlinedTextField(value = attendantName, onValueChange = { attendantName = it; errorMsg = null },
                label = { Text("Attendant Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed))

            OutlinedTextField(value = doctorNote, onValueChange = { doctorNote = it },
                label = { Text("Doctor Note (optional)") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed))

            if (errorMsg != null) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Button(onClick = { submit() }, enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(12.dp)) {
                if (isLoading) CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                else Text("Submit Emergency Request", fontWeight = FontWeight.Bold, color = White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
