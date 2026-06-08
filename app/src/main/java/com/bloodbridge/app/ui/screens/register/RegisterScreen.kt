package com.bloodbridge.app.ui.screens.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.theme.BloodRed
import com.bloodbridge.app.ui.theme.BloodRedDark
import com.bloodbridge.app.ui.theme.Gray
import com.bloodbridge.app.ui.theme.White
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    repository: BloodBridgeRepository,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("male") }
    var dob by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    val genders = listOf("male", "female", "other")
    val scope = rememberCoroutineScope()

    fun doRegister() {
        if (name.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank()) {
            errorMessage = "Please fill all required fields"
            return
        }
        if (bloodGroup.isBlank()) {
            errorMessage = "Please select a blood group"
            return
        }
        if (city.isBlank()) {
            errorMessage = "Please enter your city"
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
            return
        }
        isLoading = true
        errorMessage = null
        scope.launch {
            val result = repository.register(
                name = name.trim(), email = email.trim(), password = password,
                confirmPassword = confirmPassword, phone = phone.trim(),
                bloodGroup = bloodGroup, city = city.trim(), cnic = cnic.trim(),
                gender = gender, dob = dob.trim(), referralCode = referralCode.trim()
            )
            isLoading = false
            result.fold(
                onSuccess = { onRegisterSuccess() },
                onFailure = { errorMessage = it.message }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Box(
            modifier = Modifier.size(60.dp).background(BloodRed, RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) { Text("MZ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White) }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Create Account", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BloodRedDark)
        Text("Join us to save lives", fontSize = 13.sp, color = Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it; errorMessage = null },
            label = { Text("Full Name *") }, leadingIcon = { Icon(Icons.Default.Person, null, tint = BloodRed) },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = Gray.copy(0.5f)))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = email, onValueChange = { email = it; errorMessage = null },
            label = { Text("Email *") }, leadingIcon = { Icon(Icons.Default.Email, null, tint = BloodRed) },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = Gray.copy(0.5f)))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = phone, onValueChange = { phone = it },
            label = { Text("Phone *") }, leadingIcon = { Icon(Icons.Default.Phone, null, tint = BloodRed) },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = Gray.copy(0.5f)))
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ExposedDropdownMenuBox(expanded = bloodGroupExpanded, onExpandedChange = { bloodGroupExpanded = it },
                modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = bloodGroup, onValueChange = {},
                    readOnly = true, label = { Text("Blood Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodGroupExpanded) },
                    modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = Gray.copy(0.5f)))
                ExposedDropdownMenu(expanded = bloodGroupExpanded, onDismissRequest = { bloodGroupExpanded = false }) {
                    bloodGroups.forEach { group ->
                        DropdownMenuItem(text = { Text(group) }, onClick = { bloodGroup = group; bloodGroupExpanded = false })
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = it },
                modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = gender.replaceFirstChar { it.uppercase() }, onValueChange = {},
                    readOnly = true, label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = Gray.copy(0.5f)))
                ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                    genders.forEach { g ->
                        DropdownMenuItem(text = { Text(g.replaceFirstChar { it.uppercase() }) },
                            onClick = { gender = g; genderExpanded = false })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = city, onValueChange = { city = it },
            label = { Text("City") }, leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = BloodRed) },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = Gray.copy(0.5f)))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = cnic, onValueChange = { cnic = it },
            label = { Text("CNIC (optional)") }, leadingIcon = { Icon(Icons.Default.Badge, null, tint = BloodRed) },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = Gray.copy(0.5f)))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = dob, onValueChange = { dob = it },
            label = { Text("Date of Birth (YYYY-MM-DD)") }, leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = BloodRed) },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = Gray.copy(0.5f)))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = password, onValueChange = { password = it; errorMessage = null },
            label = { Text("Password *") }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = BloodRed) },
            trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) {
                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Gray)
            }},
            singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = Gray.copy(0.5f)))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it; errorMessage = null },
            label = { Text("Confirm Password *") }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = BloodRed) },
            singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = Gray.copy(0.5f)))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = referralCode, onValueChange = { referralCode = it },
            label = { Text("Referral Code (optional)") }, leadingIcon = { Icon(Icons.Default.CardGiftcard, null, tint = BloodRed) },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed, unfocusedBorderColor = Gray.copy(0.5f)))

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { doRegister() }, enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BloodRed), shape = RoundedCornerShape(12.dp)) {
            if (isLoading) CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            else Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Text("Already have an account? ", color = Gray)
            Text("Login", color = BloodRed, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToLogin() })
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
