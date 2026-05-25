package com.wharzc.motoguard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import android.graphics.BitmapFactory

@Composable
fun LiveTrackingScreen(
    tripViewModel: TripViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTripHistory: () -> Unit
) {
    val primaryDarkBlue = Color(0xFF10213E)
    val actionBlue = Color(0xFF1976D2)
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    var motorcycleLocation by remember { mutableStateOf(LatLng(13.4115, 121.1803)) }
    var currentSpeedKmh by remember { mutableStateOf(0.0) }
    var recenterTrigger by remember { mutableStateOf(0) }

    // Firebase references
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val rtdb = FirebaseDatabase.getInstance().reference
    val currentUser = auth.currentUser

    // Motorcycle profile
    var motorcycleName by remember { mutableStateOf("My Motorcycle") }
    var motorcycleModel by remember { mutableStateOf("Honda Click 125i") }
    var phoneNumber by remember { mutableStateOf("") }

    // Geofence data
    var geofences by remember { mutableStateOf<List<Geofence>>(emptyList()) }
    var insideGeofence by remember { mutableStateOf<MutableMap<String, Boolean>>(mutableMapOf()) }
    var showExitDialog by remember { mutableStateOf(false) }
    var exitedGeofenceName by remember { mutableStateOf("") }
    var showAlertChoiceDialog by remember { mutableStateOf(false) }

    // Movement detection
    var wasStationary by remember { mutableStateOf(true) }
    var previousSpeed by remember { mutableStateOf(0.0) }

    val GEOFENCE_RADIUS_METERS = 100.0

    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    fun getUserRef() = currentUser?.uid?.let { rtdb.child("users").child(it) }

    // Load profile and initialise RTDB alerts
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            try {
                val doc = db.collection("users").document(currentUser.uid).get().await()
                if (doc.exists()) {
                    motorcycleName = doc.getString("motorcycleName") ?: "My Motorcycle"
                    motorcycleModel = doc.getString("motorcycleModel") ?: "Honda Click 125i"
                    phoneNumber = doc.getString("phoneNumber") ?: ""
                }
                val userRef = getUserRef()
                userRef?.child("alerts/SMS")?.setValue(false)
                userRef?.child("alerts/Call")?.setValue(false)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Load geofences
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            try {
                val snapshot = db.collection("users")
                    .document(currentUser.uid)
                    .collection("geofences")
                    .get()
                    .await()
                geofences = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val lat = doc.getDouble("latitude") ?: return@mapNotNull null
                    val lng = doc.getDouble("longitude") ?: return@mapNotNull null
                    Geofence(doc.id, name, lat, lng)
                }
                insideGeofence = geofences.associate { it.id to false }.toMutableMap()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Location permission handling
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateIntervalMillis(1000L)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val lastLocation = locationResult.lastLocation
                    if (lastLocation != null) {
                        val newLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                        val speedKmh = lastLocation.speed * 3.6

                        motorcycleLocation = newLatLng
                        currentSpeedKmh = speedKmh.toDouble()
                        tripViewModel.evaluateTelemetryRules(newLatLng, speedKmh.toDouble())

                        // ---- Write location to RTDB ----
                        rtdb.child("loc/lat").setValue(lastLocation.latitude)
                        rtdb.child("loc/long").setValue(lastLocation.longitude)
                        getUserRef()?.child("location")?.apply {
                            child("lat").setValue(lastLocation.latitude)
                            child("long").setValue(lastLocation.longitude)
                        }

                        // ---- Movement detection notification ----
                        if (!wasStationary && speedKmh > 5 && previousSpeed <= 5) {
                            scope.launch {
                                NotificationRepository.addNotification(
                                    context = context,
                                    title = "Movement Detected",
                                    message = "Motorcycle moved without ignition.",
                                    type = "movement"
                                )
                            }
                        }
                        wasStationary = speedKmh < 1
                        previousSpeed = speedKmh

                        // ---- Geofence crossing detection ----
                        if (geofences.isNotEmpty() && phoneNumber.isNotEmpty()) {
                            val currentInside = mutableMapOf<String, Boolean>()
                            for (geofence in geofences) {
                                val distance = haversineDistance(
                                    newLatLng.latitude, newLatLng.longitude,
                                    geofence.latitude, geofence.longitude
                                )
                                val inside = distance <= GEOFENCE_RADIUS_METERS
                                currentInside[geofence.id] = inside

                                val previous = insideGeofence[geofence.id] ?: false
                                if (previous && !inside) {
                                    exitedGeofenceName = geofence.name
                                    showExitDialog = true

                                    val alertsRef = getUserRef()?.child("alerts")
                                    alertsRef?.child("SMS")?.setValue(true)
                                    alertsRef?.child("Call")?.setValue(true)
                                    alertsRef?.child("number")?.setValue(phoneNumber)

                                    // Send notification
                                    scope.launch {
                                        NotificationRepository.addNotification(
                                            context = context,
                                            title = "Geofence Alert",
                                            message = "You have exited geofence: ${geofence.name}",
                                            type = "geofence"
                                        )
                                    }
                                }
                            }
                            insideGeofence = currentInside
                        }
                    }
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } catch (e: SecurityException) { e.printStackTrace() }

            onDispose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } else {
            onDispose {}
        }
    }

    // ------------------------------ UI (unchanged) ------------------------------
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FB))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryDarkBlue)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp).clickable { onNavigateBack() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Live Location Tracking", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { recenterTrigger++ }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.MyLocation, "Recenter", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            // Map
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                MapLibreCustomView(
                    targetLocation = motorcycleLocation,
                    recenterTrigger = recenterTrigger,
                    geofences = geofences,
                    modifier = Modifier.fillMaxSize()
                )
                Card(
                    modifier = Modifier.padding(16.dp).align(Alignment.TopCenter),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryDarkBlue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color.Green, shape = RoundedCornerShape(5.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Device Online • Live Streaming", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Motorcycle info card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = motorcycleName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = primaryDarkBlue)
                        Text(text = "$motorcycleModel • MotoGuard Hardware", fontSize = 14.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(modifier = Modifier.weight(1f)) {
                            InfoItem(icon = Icons.Filled.Speed, label = "Speed", value = "${String.format("%.1f", currentSpeedKmh)} km/h")
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            InfoItem(
                                icon = Icons.Filled.LocationOn,
                                label = "Coordinates",
                                value = String.format("%.4f, %.4f", motorcycleLocation.latitude, motorcycleLocation.longitude)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onNavigateToTripHistory,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryDarkBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.History, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trip History", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showAlertChoiceDialog = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = actionBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send SMS / Call", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Geofence crossing popup
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Geofence Alert", fontWeight = FontWeight.Bold, color = primaryDarkBlue) },
            text = { Text("You have crossed the geofence: $exitedGeofenceName") },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("OK", color = primaryDarkBlue, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

    // SMS / Call choice dialog
    if (showAlertChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showAlertChoiceDialog = false },
            title = { Text("Send Alert", fontWeight = FontWeight.Bold, color = primaryDarkBlue) },
            text = { Text("Choose how you want to be notified:") },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(
                        onClick = {
                            val alertsRef = getUserRef()?.child("alerts")
                            alertsRef?.child("SMS")?.setValue(true)
                            alertsRef?.child("Call")?.setValue(false)
                            alertsRef?.child("number")?.setValue(phoneNumber)
                            rtdb.child("SMS").setValue(true)
                            rtdb.child("Call").setValue(false)
                            rtdb.child("number").setValue(phoneNumber)
                            scope.launch {
                                NotificationRepository.addNotification(
                                    context,
                                    "Alert Triggered",
                                    "You sent an alert via SMS.",
                                    "alert"
                                )
                            }
                            showAlertChoiceDialog = false
                        }
                    ) {
                        Text("SMS", color = primaryDarkBlue, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = {
                            val alertsRef = getUserRef()?.child("alerts")
                            alertsRef?.child("Call")?.setValue(true)
                            alertsRef?.child("SMS")?.setValue(false)
                            alertsRef?.child("number")?.setValue(phoneNumber)
                            rtdb.child("Call").setValue(true)
                            rtdb.child("SMS").setValue(false)
                            rtdb.child("number").setValue(phoneNumber)
                            scope.launch {
                                NotificationRepository.addNotification(
                                    context,
                                    "Alert Triggered",
                                    "You sent an alert via Call.",
                                    "alert"
                                )
                            }
                            showAlertChoiceDialog = false
                        }
                    ) {
                        Text("Call", color = primaryDarkBlue, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlertChoiceDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF10213E), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun MapLibreCustomView(
    targetLocation: LatLng,
    recenterTrigger: Int,
    geofences: List<Geofence>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapTilerKey = "EhDTfYOHK2xpfkO9v2N6"
    val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=$mapTilerKey"

    var maplibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    val mapView = remember { MapView(context) }
    var markers by remember { mutableStateOf<List<Marker>>(emptyList()) }

    val customIcon = remember {
        runCatching {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.red_marker)
            if (bitmap != null) IconFactory.getInstance(context).fromBitmap(bitmap) else null
        }.getOrNull()
    }

    fun updateMarkers(map: MapLibreMap) {
        markers.forEach { it.remove() }
        val newMarkers = geofences.map { geofence ->
            val markerOptions = MarkerOptions()
                .position(LatLng(geofence.latitude, geofence.longitude))
                .title(geofence.name)
            customIcon?.let { markerOptions.icon(it) }
            try {
                map.addMarker(markerOptions)
            } catch (e: Exception) {
                map.addMarker(MarkerOptions()
                    .position(LatLng(geofence.latitude, geofence.longitude))
                    .title(geofence.name))
            }
        }
        markers = newMarkers
    }

    LaunchedEffect(geofences, maplibreMapInstance) {
        val map = maplibreMapInstance
        if (map != null) {
            if (geofences.isNotEmpty()) updateMarkers(map)
            else {
                markers.forEach { it.remove() }
                markers = emptyList()
            }
        }
    }

    LaunchedEffect(targetLocation, recenterTrigger, maplibreMapInstance) {
        maplibreMapInstance?.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder().target(targetLocation).zoom(15.0).build()
            )
        )
    }

    AndroidView(
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    map.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
                        maplibreMapInstance = map
                        if (geofences.isNotEmpty()) updateMarkers(map)

                        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (hasFine || hasCoarse) {
                            try {
                                val locationComponent = map.locationComponent
                                val activationOptions = LocationComponentActivationOptions
                                    .builder(context, style)
                                    .useDefaultLocationEngine(true)
                                    .build()
                                locationComponent.activateLocationComponent(activationOptions)
                                locationComponent.isLocationComponentEnabled = true
                                locationComponent.cameraMode = CameraMode.NONE
                                locationComponent.renderMode = RenderMode.COMPASS
                            } catch (e: SecurityException) { e.printStackTrace() }
                        }
                    }
                }
            }
        },
        modifier = modifier
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }
}