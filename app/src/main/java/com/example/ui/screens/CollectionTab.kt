package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.core.ShareUtils
import com.example.data.model.Album
import com.example.data.model.Stamp
import com.example.ui.StampViewModel
import com.example.ui.theme.DarkPostBlue
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.VintageCream
import com.example.ui.theme.VintageCharcoal
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionTab(
    viewModel: StampViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stamps by viewModel.filteredStamps.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val albums by viewModel.albums.collectAsState()

    // Sub-tab toggling: 0 is Postage Stamps (Tất cả tem), 1 is Theme Notebooks (Sổ tay chủ đề)
    var currentSubTab by remember { mutableStateOf(0) }

    // Dialog triggering states
    var showDetailStamp by remember { mutableStateOf<Stamp?>(null) }
    var showDetailAlbum by remember { mutableStateOf<Album?>(null) }
    var showCreateAlbumDialog by remember { mutableStateOf(false) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VintageCream)
            .padding(horizontal = 16.dp)
    ) {
        // --- SEARCH BAR ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("search_bar"),
            placeholder = { Text(viewModel.translate("search_placeholder"), color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = DarkPostBlue) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkPostBlue,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // --- SUB-TAB SELECTION SWIPER ROWS ---
        TabRow(
            selectedTabIndex = currentSubTab,
            containerColor = Color.Transparent,
            contentColor = DarkPostBlue,
            divider = {},
            indicator = { tabPositions ->
                if (tabPositions.isNotEmpty()) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentSubTab]),
                        color = DarkPostBlue
                    )
                }
            },
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Tab(
                selected = currentSubTab == 0,
                onClick = { currentSubTab = 0 },
                text = { Text(viewModel.translate("stamps_title"), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
            Tab(
                selected = currentSubTab == 1,
                onClick = { currentSubTab = 1 },
                text = { Text(viewModel.translate("albums_title"), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }

        // --- SUB-TAB 0: POSTAGE STAMPS ---
        if (currentSubTab == 0) {
            // Horizontal Scientific Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.translate("scientific_classification"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkPostBlue
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showCreateCategoryDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tạo danh mục",
                        tint = TerracottaRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (viewModel.language.collectAsState().value == "vi") "Tạo danh mục" else "New Category",
                        color = TerracottaRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text(viewModel.translate("stamps_all")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkPostBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                items(categories) { category ->
                    val isSelected = selectedCategory == category.name
                    val color = remember(category.colorHex) {
                        try { Color(android.graphics.Color.parseColor(category.colorHex)) } catch (e: Exception) { DarkPostBlue }
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(category.name) },
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

            // Stamp gallery
            if (stamps.isEmpty()) {
                EmptyGalleryState(viewModel, searchQuery, selectedCategory != null)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(stamps) { stamp ->
                        StampItemCard(
                            stamp = stamp,
                            onClick = { showDetailStamp = stamp }
                        )
                    }
                }
            }
        }

        // --- SUB-TAB 1: THEME NOTEBOOKS (ALBUMS) ---
        else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${albums.size} ${viewModel.translate("albums_title").lowercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Button(
                    onClick = { showCreateAlbumDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPostBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(viewModel.translate("btn_create_album"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (albums.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (viewModel.language.collectAsState().value == "vi")
                                "Chưa có sổ tay chủ đề nào!\nHãy tạo sổ tay để phân loại khoa học hơn."
                            else
                                "No notebooks created yet!\nCreate themes to build gorgeous notebooks.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(albums) { album ->
                        val albumStampsCount = stamps.count { it.albumId == album.id }
                        NotebookCard(
                            album = album,
                            stampsCount = albumStampsCount,
                            onClick = { showDetailAlbum = album },
                            onDelete = { viewModel.deleteAlbum(album) }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS HANDLING ---

    // Stamp details dialog
    showDetailStamp?.let { stamp ->
        StampDetailDialog(
            stamp = stamp,
            viewModel = viewModel,
            onDismiss = { showDetailStamp = null },
            onDelete = {
                viewModel.deleteStamp(stamp)
                showDetailStamp = null
            }
        )
    }

    // Album details dialog (Theme Notebook Opening Visualizer)
    showDetailAlbum?.let { album ->
        AlbumDetailDialog(
            album = album,
            viewModel = viewModel,
            stamps = stamps.filter { it.albumId == album.id },
            onDismiss = { showDetailAlbum = null },
            onStampClick = { stamp ->
                showDetailStamp = stamp
            }
        )
    }

    // Create Album creation Dialog
    if (showCreateAlbumDialog) {
        CreateAlbumDialog(
            viewModel = viewModel,
            onDismiss = { showCreateAlbumDialog = false },
            onSave = { title, desc, coverHex ->
                viewModel.addAlbum(title, desc, coverHex)
                showCreateAlbumDialog = false
            }
        )
    }

    // Create Category Dialog
    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            viewModel = viewModel,
            onDismiss = { showCreateCategoryDialog = false },
            onSave = { name, colorHex ->
                viewModel.addCustomCategory(name, colorHex, "Category")
                showCreateCategoryDialog = false
            }
        )
    }
}

@Composable
fun EmptyGalleryState(
    viewModel: StampViewModel,
    searchQuery: String,
    hasFilter: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterFrames,
                contentDescription = "Không có tem",
                tint = Color.LightGray,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (searchQuery.isNotEmpty() || hasFilter)
                    (if (viewModel.language.collectAsState().value == "vi") "Không tìm thấy con tem phù hợp!" else "No matching stamp found!")
                else
                    viewModel.translate("empty_gallery"),
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun StampItemCard(
    stamp: Stamp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("stamp_item_${stamp.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Postmark category tag
                Text(
                    text = stamp.category.take(15) + if (stamp.category.length > 15) ".." else "",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkPostBlue.copy(alpha = 0.6f)
                )
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = TerracottaRed.copy(alpha = 0.5f),
                    modifier = Modifier.size(10.dp)
                )
            }

            // Simulated postage stamp shadow frame
            Box(
                modifier = Modifier
                    .aspectRatio(if (stamp.shape == "RECTANGLE") 0.75f else 1.0f)
                    .fillMaxWidth()
                    .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(4.dp))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(stamp.filePath),
                    contentDescription = stamp.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stamp.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DarkPostBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stamp.country,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Visual layout for spiral coil handbook Albums
@Composable
fun NotebookCard(
    album: Album,
    stampsCount: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val coverColor = remember(album.coverColorHex) {
        try { Color(android.graphics.Color.parseColor(album.coverColorHex)) } catch (e: Exception) { DarkPostBlue }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(6.dp, shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = coverColor),
        shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // --- SPIRAL BINDING SHAPE (metallic coils on the left edge) ---
            Column(
                modifier = Modifier
                    .width(18.dp)
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.15f))
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(7) {
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(6.dp)
                            .background(
                                color = Color(0xFFCCCCCC),
                                shape = RoundedCornerShape(3.dp)
                            )
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(3.dp))
                    )
                }
            }

            // --- NOTEBOOK PAGES / LABEL SHEET ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 8.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
                    .background(
                        color = Color(0xFFFDFBF7), // Ivory paper color
                        shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                    )
                    .border(1.dp, Color(0xFFE5DDD0), RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 8.dp, bottomEnd = 8.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Title header
                        Text(
                            text = album.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = VintageCharcoal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (album.description.isNotEmpty()) {
                            Text(
                                text = album.description,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Count and actions row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = coverColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$stampsCount ${if (coverColor.hashCode() % 2 == 0) "tem" else "stamps"}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = coverColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa sổ tay",
                                tint = Color.Red.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// STAMP DETAIL DIALOG WITH ADVANCED SHARING (APP SHARE VS QUICK SHARE)
@Composable
fun StampDetailDialog(
    stamp: Stamp,
    viewModel: StampViewModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = if (viewModel.language.value == "vi") "Xác nhận xóa" else "Confirm Delete",
                    fontWeight = FontWeight.Bold,
                    color = DarkPostBlue
                )
            },
            text = {
                Text(
                    text = if (viewModel.language.value == "vi") "Bạn có chắc chắn muốn xóa con tem \"${stamp.name}\" khỏi bộ sưu tập? Hành động này không thể hoàn tác." else "Are you sure you want to delete \"${stamp.name}\" from your collection? This action cannot be undone.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(viewModel.translate("delete"))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text(viewModel.translate("cancel"))
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("stamp_detail_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large stamp view with premium stamp shadow feel
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .shadow(8.dp, shape = RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(stamp.filePath),
                        contentDescription = stamp.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stamp.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkPostBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Detail attributes
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFCFBF7), shape = RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Phân loại:", color = Color.Gray, fontSize = 12.sp)
                        Text(stamp.category, fontWeight = FontWeight.Medium, color = DarkPostBlue, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Quốc gia:", color = Color.Gray, fontSize = 12.sp)
                        Text(stamp.country, fontWeight = FontWeight.Medium, color = DarkPostBlue, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ngày sưu tầm:", color = Color.Gray, fontSize = 12.sp)
                        Text(dateFormatter.format(Date(stamp.dateCreated)), color = Color.Gray, fontSize = 12.sp)
                    }
                    if (stamp.note.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFDDE2EA))
                        Text("Ghi chú:", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = stamp.note,
                            color = VintageCharcoal,
                            fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons (Share & Delete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                ShareUtils.shareStampImage(context, File(stamp.filePath), stamp.name)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_stamp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkPostBlue)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Chia sẻ")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(viewModel.translate("share"))
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("delete_stamp_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(viewModel.translate("delete"))
                    }
                }
            }
        }
    }
}

// CREATED NOTEBOOK DETAILS DIALOG (Visualizes opening of custom notebook binder)
@Composable
fun AlbumDetailDialog(
    album: Album,
    viewModel: StampViewModel,
    stamps: List<Stamp>,
    onDismiss: () -> Unit,
    onStampClick: (Stamp) -> Unit
) {
    val coverColor = remember(album.coverColorHex) {
        try { Color(android.graphics.Color.parseColor(album.coverColorHex)) } catch (e: Exception) { DarkPostBlue }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)) // Ivory paper page style
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top header colored similar to the binder cover
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(coverColor)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = album.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${stamps.size} ${viewModel.translate("stamps_count")}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color.White)
                        }
                    }
                }

                // Binder spiral separator line indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.Black.copy(alpha = 0.1f))
                )

                // List of stamps assigned
                if (stamps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.translate("empty_album"),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(stamps) { stamp ->
                            // Custom minimized card representation for Notebook member stamps
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onStampClick(stamp) },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(0.5.dp, Color(0xFFE5DDD0)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = File(stamp.filePath),
                                            contentDescription = stamp.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stamp.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = DarkPostBlue
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

// DIALOG TO CREATE NEW CUSTOM THEME NOTEBOOK
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlbumDialog(
    viewModel: StampViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Choose cover color hex
    val coverColors = listOf(
        "#0061A4" to "Xanh bưu chính",
        "#BA1A1A" to "Yêu thương",
        "#1B5E20" to "Thiên nhiên",
        "#E65100" to "Mùa thu vàng",
        "#4A148C" to "Hoàng gia",
        "#3E2723" to "Mộc cổ kính"
    )
    var selectedColorHex by remember { mutableStateOf(coverColors.first().first) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = viewModel.translate("add_album"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkPostBlue
                )

                // Album Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(viewModel.translate("album_name")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(viewModel.translate("album_desc")) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )

                // Cover Colors Row
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = viewModel.translate("album_color"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        coverColors.forEach { (hexCode, label) ->
                            val color = remember(hexCode) { Color(android.graphics.Color.parseColor(hexCode)) }
                            val isSel = selectedColorHex == hexCode
                            
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColorHex = hexCode }
                                    .border(
                                        width = if (isSel) 3.dp else 0.dp,
                                        color = if (isSel) Color(0xFFCCCCCC) else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(viewModel.translate("cancel"))
                    }

                    Button(
                        onClick = {
                            if (title.trim().isNotEmpty()) {
                                onSave(title.trim(), description.trim(), selectedColorHex)
                            }
                        },
                        enabled = title.trim().isNotEmpty(),
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkPostBlue)
                    ) {
                        Text(viewModel.translate("create"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// DIALOG TO CREATE NEW CUSTOM CATEGORY
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCategoryDialog(
    viewModel: StampViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    // Choose cover/wax tag color hex of categories
    val waxPalette = listOf(
        "#0F4C5C" to "Xanh thông",
        "#5C0632" to "Mận chín",
        "#E36414" to "Cam cháy",
        "#003049" to "Xanh hải quân",
        "#C1121F" to "Đỏ gạch",
        "#780000" to "Nâu đỏ cổ điển",
        "#6B705C" to "Xanh ô liu",
        "#4A4E69" to "Xám tím hoài niệm"
    )
    var selectedColorHex by remember { mutableStateOf(waxPalette.first().first) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (viewModel.language.collectAsState().value == "vi") "Tạo Danh Mục Mới" else "Create New Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkPostBlue
                )

                // Category Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (viewModel.language.collectAsState().value == "vi") "Tên danh mục *" else "Category name *") },
                    placeholder = { Text(if (viewModel.language.collectAsState().value == "vi") "Ví dụ: Động vật, Hoa lá..." else "Example: Animals, Flora...") },
                    modifier = Modifier.fillMaxWidth().testTag("new_category_dialog_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Colors Palette Row
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (viewModel.language.collectAsState().value == "vi") "Chọn tông màu chủ đề *" else "Choose theme color *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        waxPalette.forEach { (hexCode, label) ->
                            val color = remember(hexCode) { Color(android.graphics.Color.parseColor(hexCode)) }
                            val isSel = selectedColorHex == hexCode
                            
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColorHex = hexCode }
                                    .border(
                                        width = if (isSel) 3.dp else 0.dp,
                                        color = if (isSel) Color(0xFFCCCCCC) else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(viewModel.translate("cancel"))
                    }

                    Button(
                        onClick = {
                            if (name.trim().isNotEmpty()) {
                                onSave(name.trim(), selectedColorHex)
                            }
                        },
                        enabled = name.trim().isNotEmpty(),
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkPostBlue)
                    ) {
                        Text(viewModel.translate("create"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
