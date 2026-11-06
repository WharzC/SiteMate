package com.wharzc.prime.farmer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wharzc.prime.viewmodel.SensorViewModel

@Composable
fun MonitoringScreen(viewModel: SensorViewModel) {
    val data by viewModel.sensorData.collectAsState()

    val bgColor = Color(0xFF195532)
    val cardColor = Color(0xFF2A5339)
    val neonGreen = Color(0xFF00FFCC)
    val neonBlue = Color(0xFF42A5F5)
    val neonOrange = Color(0xFFFFA726)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp) // Bottom padding avoids navigation bar overlap
    ) {
        // NPK Section
        item {
            SectionTitle("Soil Nutrients (NPK)")
            Card(colors = CardDefaults.cardColors(containerColor = cardColor), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CircularDial("Nitrogen", data.npk.nitrogen.toFloat(), 100f, "mg/kg", neonGreen)
                    CircularDial("Phosphorus", data.npk.phosphorus.toFloat(), 100f, "mg/kg", neonOrange)
                    CircularDial("Potassium", data.npk.potassium.toFloat(), 100f, "mg/kg", neonBlue)
                }
            }
        }

        // Water Quality Section
        item {
            SectionTitle("Water Quality")
            Card(colors = CardDefaults.cardColors(containerColor = cardColor), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        CircularDial("Temp", data.waterQuality.temperature.toFloat(), 50f, "°C", Color(0xFFEF5350))
                        CircularDial("Turbidity", data.waterQuality.turbidity.toFloat(), 100f, "NTU", neonBlue)
                    }

                    // pH Linear Gauge (Using the updated Material 3 lambda syntax)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("pH Level: ${data.waterQuality.pH}", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (data.waterQuality.pH / 14.0).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(12.dp),
                            color = neonGreen,
                            trackColor = Color.DarkGray,
                            strokeCap = StrokeCap.Round
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("0 (Acidic)", color = Color.Gray, fontSize = 10.sp)
                            Text("7 (Neutral)", color = Color.Gray, fontSize = 10.sp)
                            Text("14 (Alkaline)", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Water Level Section
        item {
            SectionTitle("Irrigation Water Level")
            Card(colors = CardDefaults.cardColors(containerColor = cardColor), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WaterTankIndicator(distanceCm = data.waterLevel.distance.toFloat(), maxDepthCm = 100f)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Status", color = Color.LightGray, fontSize = 14.sp)
                        Text(data.waterLevel.status, color = neonBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Distance", color = Color.LightGray, fontSize = 14.sp)
                        Text("${data.waterLevel.distance} cm", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun CircularDial(title: String, value: Float, max: Float, unit: String, color: Color) {
    val progress = (value / max).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.DarkGray,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = 135f,
                    sweepAngle = 270f * progress,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = value.toInt().toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = unit, color = Color.Gray, fontSize = 10.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun WaterTankIndicator(distanceCm: Float, maxDepthCm: Float) {
    val fillPercentage = (1f - (distanceCm / maxDepthCm)).coerceIn(0f, 1f)

    Box(
        modifier = Modifier.width(60.dp).height(120.dp).background(Color.DarkGray, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(fillPercentage)
                .background(Color(0xFF42A5F5), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
        )
        Text(
            "${(fillPercentage * 100).toInt()}%",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}