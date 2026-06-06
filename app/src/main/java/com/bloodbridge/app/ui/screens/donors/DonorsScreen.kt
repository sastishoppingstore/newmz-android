package com.bloodbridge.app.ui.screens.donors

import androidx.compose.foundation.clickable
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
import com.bloodbridge.app.data.api.models.Donor
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.components.*
import com.bloodbridge.app.ui.theme.*
import com.bloodbridge.app.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorsScreen(
    repository: BloodBridgeRepository,
    onNavigateToDonorProfile: (Int) -> Unit
) {
    var donors by remember { mutableStateOf<List<Donor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchCity by remember { mutableStateOf("") }
    var searchBloodGroup by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val bloodGroups = listOf("", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    var bloodGroupExpanded by remember { mutableStateOf(false) }

    fun loadDonors() {
        scope.launch {
            isLoading = true
            val result = repository.searchDonors(
                city = searchCity.ifBlank { null },
                bloodGroup = searchBloodGroup.ifBlank { null }
            )
            result.fold(
                onSuccess = { donors = it; isLoading = false },
                onFailure = { error = it.message; isLoading = false }
            )
        }
    }

    LaunchedEffect(Unit) { loadDonors() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blood Donors", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp,
                color = White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchCity,
                            onValueChange = { searchCity = it },
                            placeholder = { Text("City") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Gray) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed)
                        )

                        ExposedDropdownMenuBox(
                            expanded = bloodGroupExpanded,
                            onExpandedChange = { bloodGroupExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = searchBloodGroup.ifEmpty { "All" },
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = { Icon(Icons.Default.Bloodtype, null, tint = BloodRed) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodGroupExpanded) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed)
                            )
                            ExposedDropdownMenu(expanded = bloodGroupExpanded, onDismissRequest = { bloodGroupExpanded = false }) {
                                bloodGroups.forEach { group ->
                                    DropdownMenuItem(
                                        text = { Text(group.ifEmpty { "All" }) },
                                        onClick = { searchBloodGroup = group; bloodGroupExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { loadDonors() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Search Donors") }
                }
            }

            when {
                isLoading -> LoadingIndicator()
                error != null -> ErrorMessage(error, onRetry = { loadDonors() })
                donors.isEmpty() -> ErrorMessage("No donors found")
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(donors, key = { it.id }) { donor ->
                            DonorCard(
                                donor = donor,
                                onClick = { onNavigateToDonorProfile(donor.id) },
                                onRequestBlood = { /* donor request dialog */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonorCard(
    donor: Donor,
    onClick: () -> Unit,
    onRequestBlood: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImage(
                url = ImageUtils.getProfileUrl(donor.profile_photo),
                name = donor.name,
                size = 56
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(donor.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (donor.is_verified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, null, tint = BloodRed, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Gray, modifier = Modifier.size(14.dp))
                    Text(donor.city ?: "Unknown", color = Gray, fontSize = 13.sp)
                }
                if (donor.total_donations != null) {
                    Text("${donor.total_donations} donations", color = Green, fontSize = 13.sp)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BloodGroupBadge(donor.blood_group)
                if (donor.eligibility_status == "eligible") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Eligible", color = Green, fontSize = 11.sp)
                }
            }
        }
    }
}
