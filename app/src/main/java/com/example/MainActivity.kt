package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.localization.AppLanguage
import com.example.ui.MainViewModel
import com.example.ui.components.AlarmActiveOverlay
import com.example.ui.navigation.NavDestination
import com.example.ui.screens.*
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val currentLanguage by mainViewModel.currentLanguage.collectAsState()
            val strings by mainViewModel.strings.collectAsState()
            val isAlarmActive by mainViewModel.antiTheftEngine.isAlarmActive.collectAsState()
            val activeReason by mainViewModel.antiTheftEngine.activeReason.collectAsState()
            val isDarkPref by mainViewModel.isDarkMode.collectAsState()

            val useDarkTheme = isDarkPref ?: isSystemInDarkTheme()

            // Request permissions on startup
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { /* Permissions updated */ }

            LaunchedEffect(Unit) {
                val permissionsToRequest = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA
                )
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }

                val needed = permissionsToRequest.filter {
                    ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                }
                if (needed.isNotEmpty()) {
                    permissionLauncher.launch(needed.toTypedArray())
                }
            }

            // RTL support for Urdu and Arabic
            val layoutDirection = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme(darkTheme = useDarkTheme) {
                    var currentDestination by remember { mutableStateOf(NavDestination.SHIELD) }
                    var isViewingIntruderLogs by remember { mutableStateOf(false) }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = WindowInsets.safeDrawing,
                            topBar = {
                                if (!isViewingIntruderLogs) {
                                    CenterAlignedTopAppBar(
                                        title = {
                                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                                Text(
                                                    text = strings.appTitle,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.background,
                                            titleContentColor = MaterialTheme.colorScheme.onBackground
                                        )
                                    )
                                }
                            },
                            bottomBar = {
                                if (!isViewingIntruderLogs) {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 8.dp,
                                        modifier = Modifier.testTag("main_bottom_nav")
                                    ) {
                                        NavDestination.values().forEach { destination ->
                                            val selected = currentDestination == destination
                                            NavigationBarItem(
                                                selected = selected,
                                                onClick = { currentDestination = destination },
                                                icon = {
                                                    Icon(
                                                        imageVector = destination.icon,
                                                        contentDescription = destination.getLabel(strings)
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = destination.getLabel(strings),
                                                        fontSize = 11.sp,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    indicatorColor = CyberCyan.copy(alpha = 0.2f),
                                                    selectedIconColor = CyberCyan,
                                                    selectedTextColor = CyberCyan
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                if (isViewingIntruderLogs) {
                                    IntruderLogsScreen(
                                        viewModel = mainViewModel,
                                        strings = strings,
                                        onBack = { isViewingIntruderLogs = false }
                                    )
                                } else {
                                    when (currentDestination) {
                                        NavDestination.SHIELD -> ShieldScreen(
                                            viewModel = mainViewModel,
                                            strings = strings,
                                            onNavigateToIntruderLogs = { isViewingIntruderLogs = true }
                                        )
                                        NavDestination.VAULT -> DeviceVaultScreen(
                                            viewModel = mainViewModel,
                                            strings = strings
                                        )
                                        NavDestination.TRACKING -> LiveTrackingScreen(
                                            viewModel = mainViewModel,
                                            strings = strings
                                        )
                                        NavDestination.COMMUNITY -> CommunityReportsScreen(
                                            viewModel = mainViewModel,
                                            strings = strings
                                        )
                                        NavDestination.SETTINGS -> SettingsScreen(
                                            viewModel = mainViewModel,
                                            strings = strings,
                                            onNavigateToIntruderLogs = { isViewingIntruderLogs = true }
                                        )
                                    }
                                }

                                // Full Screen Alarm Overlay when triggered
                                if (isAlarmActive) {
                                    AlarmActiveOverlay(
                                        triggerReason = activeReason,
                                        strings = strings,
                                        onDisarmAttempt = { pin ->
                                            mainViewModel.stopAlarmWithPin(pin)
                                        },
                                        onDismissRequest = {
                                            mainViewModel.antiTheftEngine.stopAlarm()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
