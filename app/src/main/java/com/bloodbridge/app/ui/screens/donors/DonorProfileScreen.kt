package com.bloodbridge.app.ui.screens.donors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodbridge.app.data.api.models.Donor
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.components.*
import com.bloodbridge.app.ui.theme.*
import com.bloodbridge.app.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorProfileScreen(
    donorId: Int,
    repository: BloodBridgeRepository,
    onBack: () -> Unit
) {
    var showRequestDialog by remember { mutableStateOf(false) }
    var patientName by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donor Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileImage(url = null, name = "Donor", size = 80)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showRequestDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Bloodtype, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request Blood", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = { Text("Request Blood Donation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = patientName, onValueChange = { patientName = it },
                        label = { Text("Patient Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp))
                    OutlinedTextField(value = hospital, onValueChange = { hospital = it },
                        label = { Text("Hospital") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp))
                    OutlinedTextField(value = contact, onValueChange = { contact = it },
                        label = { Text("Contact Number") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            repository.requestDonor(donorId, patientName, hospital, contact)
                            isLoading = false
                            showRequestDialog = false
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
                ) { Text("Send Request") }
            },
            dismissButton = { TextButton(onClick = { showRequestDialog = false }) { Text("Cancel") } }
        )
    }
}
