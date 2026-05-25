package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StampViewModel
import com.example.ui.theme.DarkPostBlue
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.VintageCream
import com.example.ui.theme.VintageCharcoal
import androidx.compose.material.icons.filled.ArrowDropDown
import com.example.util.StampProcessor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveStampScreen(
    croppedBitmap: Bitmap,
    shape: String,
    viewModel: StampViewModel,
    onBack: () -> Unit,
    onSaveComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val albums by viewModel.albums.collectAsState()

    // Form states
    var stampName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    val faceValue = ""
    var country by remember { mutableStateOf("VIỆT NAM") }
    var note by remember { mutableStateOf("") }
    
    // Choose theme notebook (optional)
    var expandedAlbumMenu by remember { mutableStateOf(false) }
    var selectedAlbum by remember { mutableStateOf<com.example.data.model.Album?>(null) }

    var isSaving by remember { mutableStateOf(false) }

    // Automatically assign first category as default once loaded
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategory.isEmpty()) {
            selectedCategory = categories.first().name
        }
    }

    // Dynamic high-fidelity completed stamp preview based on current form inputs!
    val finalStampPreview = remember(stampName, selectedCategory, faceValue, country) {
        // Prevent empty strings causing issues in generator lines
        val displayName = stampName.ifEmpty { viewModel.translate("stamp_name") }
        val displayCountry = country.ifEmpty { "VIỆT NAM" }
        StampProcessor.createStamp(
            source = croppedBitmap,
            shape = shape,
            title = displayName,
            faceValue = faceValue,
            country = displayCountry
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VintageCream)
    ) {
        // --- TOP BAR ---
        TopAppBar(
            title = { Text(viewModel.translate("detail_stamp"), fontWeight = FontWeight.Bold, color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBack, enabled = !isSaving) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkPostBlue)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- LIVE STAMP RENDER PREVIEW (Realist-style paper look) ---
            Text(
                text = viewModel.translate("preview_design"),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DarkPostBlue,
                modifier = Modifier.align(Alignment.Start)
            )

            Box(
                modifier = Modifier
                    .size(230.dp)
                    .shadow(12.dp, shape = RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = finalStampPreview.asImageBitmap(),
                    contentDescription = "Xem trước con tem",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Divider(color = Color(0xFFDDE2EA), thickness = 1.dp)

            // --- FORM FIELDS ---

            // Stamp Name
            OutlinedTextField(
                value = stampName,
                onValueChange = { stampName = it },
                label = { Text(viewModel.translate("stamp_name")) },
                placeholder = { Text(viewModel.translate("stamp_name_hint")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stamp_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkPostBlue,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Categories Selector (Scientific Classification Row)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = viewModel.translate("classify_collection"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkPostBlue,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category.name
                        val color = remember(category.colorHex) {
                            try { Color(android.graphics.Color.parseColor(category.colorHex)) } catch (e: Exception) { DarkPostBlue }
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category.name },
                            label = { Text(category.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Choice of theme notebook (optional)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = viewModel.translate("choose_album_opt"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkPostBlue,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedAlbumMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = VintageCharcoal
                        ),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedAlbum?.title ?: viewModel.translate("choose_album_placeholder"),
                                fontSize = 14.sp
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = expandedAlbumMenu,
                        onDismissRequest = { expandedAlbumMenu = false },
                        modifier = Modifier.fillMaxWidth().background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text(viewModel.translate("none"), color = Color.Gray) },
                            onClick = {
                                selectedAlbum = null
                                expandedAlbumMenu = false
                            }
                        )
                        albums.forEach { album ->
                            DropdownMenuItem(
                                text = { Text(album.title, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedAlbum = album
                                    expandedAlbumMenu = false
                                }
                            )
                        }
                    }
                }
            }



            // Country
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("Thành phố / Quốc gia") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("country_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkPostBlue,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Ghi Chú
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ký sự, ghi chú bưu thiếp...") },
                placeholder = { Text(viewModel.translate("custom_note_hint")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("note_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkPostBlue,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(4.dp))

            // --- SAVE TRIGGER ACTION ---
            if (isSaving) {
                CircularProgressIndicator(color = TerracottaRed)
            } else {
                Button(
                    onClick = {
                        if (stampName.trim().isEmpty()) {
                            // Simple friendly validation
                            return@Button
                        }
                        isSaving = true
                        viewModel.addStamp(
                            context = context,
                            name = stampName.trim(),
                            category = selectedCategory.ifEmpty { "Mẫu khác" },
                            bitmap = croppedBitmap,
                            shape = shape,
                            note = note.trim(),
                            faceValue = faceValue.trim(),
                            country = country.trim().uppercase(),
                            albumId = selectedAlbum?.id,
                            onComplete = { success ->
                                isSaving = false
                                if (success) {
                                    onSaveComplete()
                                }
                            }
                        )
                    },
                    enabled = stampName.trim().isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_stamp_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerracottaRed,
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Lưu con tem")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(viewModel.translate("save_to_collection"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
