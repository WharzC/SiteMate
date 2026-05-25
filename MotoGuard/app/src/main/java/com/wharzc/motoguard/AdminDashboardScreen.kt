package com.wharzc.motoguard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*   // <-- only this import needed now

@Composable
fun AdminDashboardScreen(onLogout: () -> Unit) {
    val db = FirebaseDatabase.getInstance()
    val locLongRef = db.getReference("loc/long")
    val locLatRef = db.getReference("loc/lat")
    val smsRef = db.getReference("SMS")
    val callRef = db.getReference("Call")
    val numberRef = db.getReference("number")

    var longValue by remember { mutableStateOf<DataSnapshot?>(null) }
    var latValue by remember { mutableStateOf<DataSnapshot?>(null) }
    var smsBoolean by remember { mutableStateOf<DataSnapshot?>(null) }
    var callBoolean by remember { mutableStateOf<DataSnapshot?>(null) }
    var phoneNumber by remember { mutableStateOf<DataSnapshot?>(null) }

    DisposableEffect(Unit) {
        val longListener = locLongRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { longValue = snapshot }
            override fun onCancelled(error: DatabaseError) {}
        })
        val latListener = locLatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { latValue = snapshot }
            override fun onCancelled(error: DatabaseError) {}
        })
        val smsListener = smsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { smsBoolean = snapshot }
            override fun onCancelled(error: DatabaseError) {}
        })
        val callListener = callRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { callBoolean = snapshot }
            override fun onCancelled(error: DatabaseError) {}
        })
        val numberListener = numberRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { phoneNumber = snapshot }
            override fun onCancelled(error: DatabaseError) {}
        })

        onDispose {
            locLongRef.removeEventListener(longListener)
            locLatRef.removeEventListener(latListener)
            smsRef.removeEventListener(smsListener)
            callRef.removeEventListener(callListener)
            numberRef.removeEventListener(numberListener)
        }
    }

    fun getDisplayString(snapshot: DataSnapshot?): String {
        val v = snapshot?.value
        return when (v) {
            is Long -> v.toString()
            is Double -> v.toString()
            is String -> v
            else -> "—"
        }
    }

    fun getBoolean(snapshot: DataSnapshot?): Boolean {
        return snapshot?.value as? Boolean ?: false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Longitude & Latitude ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Longitude", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = getDisplayString(longValue),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Latitude", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = getDisplayString(latValue),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }

        // --- SMS & Call boxes ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(
                        color = if (getBoolean(smsBoolean)) Color(0xFF4CAF50) else Color(0xFFF44336),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Send SMS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { smsRef.setValue(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Reset", color = Color.Black)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(
                        color = if (getBoolean(callBoolean)) Color(0xFF4CAF50) else Color(0xFFF44336),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Call Number", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { callRef.setValue(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Reset", color = Color.Black)
                    }
                }
            }
        }

        // --- Phone Number ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Phone Number", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = getDisplayString(phoneNumber),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }
        }

        // Pushes all content up and leaves the logout button at the bottom
        Spacer(modifier = Modifier.weight(1f))

        // Logout button
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}