package com.bloodbridge.app.ui.screens.emergency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.bloodbridge.app.data.api.models.EmergencyRequest
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.components.*
import com.bloodbridge.app.ui.theme.*
import com.bloodbridge.app.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    repository: BloodBridgeRepository,
    onNavigateToForm: () -> Unit
) {
    var requests by remember { mutableStateOf<List<EmergencyRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("all") }
    val scope = rememberCoroutineScope()

    fun loadRequests() {
        scope.launch {
            isLoading = true
            val result = repository.getEmergencyRequests()
            result.fold(
                onSuccess = {
                    requests = if (selectedFilter == "all") it
                    else it.filter { r -> r.urgency_level == selectedFilter }
                    isLoading = false
                },
                onFailure = { error = it.message; isLoading = false }
            )
        }
    }

    LaunchedEffect(Unit) { loadRequests() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Requests", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToForm,
                containerColor = BloodRed,
                contentColor = White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Request") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BloodRed.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip("All", selectedFilter == "all", onClick = { selectedFilter = "all"; loadRequests() })
                    FilterChip("Critical", selectedFilter == "critical", onClick = { selectedFilter = "critical"; loadRequests() })
                    FilterChip("High", selectedFilter == "high", onClick = { selectedFilter = "high"; loadRequests() })
                    FilterChip("Normal", selectedFilter == "very_high", onClick = { selectedFilter = "very_high"; loadRequests() })
                }
            }

            when {
                isLoading -> LoadingIndicator()
                error != null -> ErrorMessage(error, onRetry = { loadRequests() })
                requests.isEmpty() -> ErrorMessage("No emergency requests")
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(requests, key = { it.id }) { request ->
                            EmergencyRequestCard(request)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) BloodRed else GrayLight,
        contentColor = if (selected) White else GrayDark
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun EmergencyRequestCard(request: EmergencyRequest) {
    val urgencyColors = when (request.urgency_level) {
        "critical" -> BloodRed
        "very_high" -> Orange
        else -> Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(request.patient_name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = urgencyColors.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = request.urgency_level.replace("_", " ").uppercase(),
                        color = urgencyColors,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            request.blood_group?.let {
                BloodGroupBadge(it)
                Spacer(modifier = Modifier.height(6.dp))
            }

            InfoRow("Hospital", request.hospital_name)
            InfoRow("City", request.city)
            InfoRow("Units Needed", "${request.units_needed}")
            InfoRow("Contact", request.contact_number)
            InfoRow("Attendant", request.attendant_name)

            if (!request.doctor_note.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Note: ${request.doctor_note}", color = GrayDark, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Posted by ${request.requester_name}",
                color = Gray,
                fontSize = 12.sp
            )
        }
    }
}
