package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.StampViewModel
import com.example.ui.theme.DarkPostBlue
import com.example.ui.theme.VintageCharcoal
import com.example.ui.theme.SoftPostBlue

@Composable
fun MainAppLayout(
    viewModel: StampViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(0) } // 0: CamCreator, 1: Collection, 2: Category
    var showSettingsDialog by remember { mutableStateOf(false) }
    val currentLang by viewModel.language.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    // Creation wizard states
    var rawPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var croppedPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var activeShape by remember { mutableStateOf("RECTANGLE") }
    var wizardStep by remember { mutableStateOf(0) } // 0: None, 1: Cropper, 2: Details Form

    // Intercept hardware Android back clicks to easily navigate back in our steps
    BackHandler(enabled = wizardStep > 0) {
        if (wizardStep == 2) {
            // Back from Details back to Cropper
            wizardStep = 1
        } else if (wizardStep == 1) {
            // Cancel Cropper back to viewfinder
            rawPhotoBitmap = null
            croppedPhotoBitmap = null
            wizardStep = 0
        }
    }

    if (wizardStep > 0 && rawPhotoBitmap != null) {
        when (wizardStep) {
            1 -> {
                StampCropperScreen(
                    sourceBitmap = rawPhotoBitmap!!,
                    initialShape = activeShape,
                    onBack = {
                        rawPhotoBitmap = null
                        wizardStep = 0
                    },
                    onCropped = { cropped, shape ->
                        croppedPhotoBitmap = cropped
                        activeShape = shape
                        wizardStep = 2
                    }
                )
            }
            2 -> {
                if (croppedPhotoBitmap != null) {
                    SaveStampScreen(
                        croppedBitmap = croppedPhotoBitmap!!,
                        shape = activeShape,
                        viewModel = viewModel,
                        onBack = {
                            wizardStep = 1
                        },
                        onSaveComplete = {
                            // Finished saving! Reset states and show gallery
                            rawPhotoBitmap = null
                            croppedPhotoBitmap = null
                            wizardStep = 0
                            currentTab = 1 // Switch automatically to "Bộ sưu tập"
                        }
                    )
                }
            }
        }
    } else {
        // Standard Tab View
        val context = androidx.compose.ui.platform.LocalContext.current
        val appShareText = if (currentLang == "vi") 
            "Hãy tải ngay Stamp Love - ứng dụng thiết kế và sưu tầm hình ảnh thành những con tem bưu chính tuyệt đẹp!" 
            else 
            "Download Stamp Love - the ultimate application to collection and design postage stamps!"

        Scaffold(
            topBar = {
                Surface(
                    color = Color.White,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Stamp Love",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = VintageCharcoal,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = { showSettingsDialog = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Cài đặt", tint = SoftPostBlue)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        label = { Text(viewModel.translate("tab_create")) },
                        icon = { Icon(Icons.Default.Camera, contentDescription = "Trình Tạo Tem") },
                        modifier = Modifier.testTag("nav_tab_create")
                    )
 
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        label = { Text(viewModel.translate("tab_collection")) },
                        icon = { Icon(Icons.Default.Collections, contentDescription = "Bộ Sưu Tập") },
                        modifier = Modifier.testTag("nav_tab_collection")
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentTab) { tab ->
                    when (tab) {
                        0 -> {
                            CamCreatorTab(
                                onImageSelected = { bitmap, shape ->
                                    rawPhotoBitmap = bitmap
                                    activeShape = shape
                                    wizardStep = 1
                                }
                            )
                        }
                        1 -> {
                            CollectionTab(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS & SHEET HELPERS ---
    if (showSettingsDialog) {
        Dialog(onDismissRequest = { showSettingsDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = viewModel.translate("dialog_settings"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DarkPostBlue
                    )
                    
                    Divider(color = Color(0xFFF3F4F9))
                    
                    // Language option
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = viewModel.translate("lang_option"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ElevatedFilterChip(
                                selected = currentLang == "vi",
                                onClick = { viewModel.setLanguage("vi") },
                                label = { Text(viewModel.translate("lang_vi"), fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            ElevatedFilterChip(
                                selected = currentLang == "en",
                                onClick = { viewModel.setLanguage("en") },
                                label = { Text(viewModel.translate("lang_en"), fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Theme option
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = viewModel.translate("theme_option"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ElevatedFilterChip(
                                selected = !isDark,
                                onClick = { if (isDark) viewModel.toggleTheme() },
                                label = { Text(viewModel.translate("theme_light"), fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            ElevatedFilterChip(
                                selected = isDark,
                                onClick = { if (!isDark) viewModel.toggleTheme() },
                                label = { Text(viewModel.translate("theme_dark"), fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Button(
                        onClick = { showSettingsDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkPostBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = viewModel.translate("cancel").replace("Hủy", "Đóng").replace("Cancel", "Close"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
