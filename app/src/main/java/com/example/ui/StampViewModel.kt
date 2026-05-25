package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Category
import com.example.data.model.Stamp
import com.example.data.model.Album
import com.example.data.repository.StampRepository
import com.example.util.StampProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class StampViewModel(private val repository: StampRepository) : ViewModel() {

    // Language and Theme customizable states
    private val _language = MutableStateFlow("vi")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Dynamic Multi-lingual Localization support dictionary
    fun translate(key: String): String {
        val isVi = language.value == "vi"
        return when (key) {
            "app_title" -> if (isVi) "Stamp Love" else "Stamp Love"
            "tab_collection" -> if (isVi) "Bộ sưu tập" else "Collection"
            "tab_create" -> if (isVi) "Tạo Tem" else "Create"
            "tab_categories" -> if (isVi) "Danh Mục" else "Categories"
            "search_placeholder" -> if (isVi) "Tìm kiếm tên, quốc gia, mệnh giá..." else "Search name, country, face value..."
            "scientific_classification" -> if (isVi) "Phân Loại Khoa Học" else "Scientific Classification"
            "stamps_all" -> if (isVi) "Tất cả" else "All"
            "albums_title" -> if (isVi) "Sổ Tay Chủ Đề" else "Theme Notebooks"
            "stamps_title" -> if (isVi) "Tem Thư Sưu Tập" else "Postage Stamps"
            "btn_create_album" -> if (isVi) "Sổ Tay Chủ Đề" else "New Notebook"
            "add_album" -> if (isVi) "Tạo Sổ Tay Mới" else "Create New Notebook"
            "album_name" -> if (isVi) "Tên chủ đề sổ tay *" else "Notebook theme name *"
            "album_desc" -> if (isVi) "Mô tả của sổ tay" else "Notebook description"
            "album_color" -> if (isVi) "Chọn tông màu bìa *" else "Choose cover color *"
            "choose_album_opt" -> if (isVi) "Thêm vào cuốn sổ tay" else "Add to theme notebook"
            "choose_album_placeholder" -> if (isVi) "Chọn sổ tay (Không bắt buộc)..." else "Select notebook (Optional)..."
            "none" -> if (isVi) "Không lưu vào sổ tay" else "No notebook (Individual)"
            "share" -> if (isVi) "Chia sẻ" else "Share"
            "share_app" -> if (isVi) "Chia sẻ qua ứng dụng" else "Share via app"
            "quick_share" -> if (isVi) "Chia sẻ cực nhanh (Quick Share)" else "Quick Share (Bluetooth/Wi-Fi)"
            "dialog_settings" -> if (isVi) "Tùy Chọn Hệ Thống" else "System settings"
            "lang_option" -> if (isVi) "Ngôn ngữ hiển thị" else "Display Language"
            "theme_option" -> if (isVi) "Giao diện chủ đề" else "Application Theme"
            "lang_vi" -> if (isVi) "Tiếng Việt (VI)" else "Vietnamese (VI)"
            "lang_en" -> if (isVi) "English (EN)" else "English (EN)"
            "theme_light" -> if (isVi) "Sáng Thanh Lịch" else "Elegant Light"
            "theme_dark" -> if (isVi) "Tối Hiện Đại" else "Modern Dark"
            "delete" -> if (isVi) "Xóa" else "Delete"
            "cancel" -> if (isVi) "Hủy" else "Cancel"
            "create" -> if (isVi) "Tạo mới" else "Create"
            "detail_stamp" -> if (isVi) "Chi Tiết Con Tem" else "Stamp Details"
            "preview_design" -> if (isVi) "Xem Trước Thiết Kế" else "Preview Design"
            "stamp_name" -> if (isVi) "Tên con tem *" else "Stamp name *"
            "stamp_name_hint" -> if (isVi) "Ví dụ: Hoa Sen Nam Bộ, Hồ Gươm..." else "Example: Lotus, Hoan Kiem Lake..."
            "classify_collection" -> if (isVi) "Phân loại vào Bộ sưu tập *" else "Classification *"
            "custom_note_hint" -> if (isVi) "Ký sự, ghi chú bưu thiếp..." else "Chronicle, postcard memo..."
            "save_to_collection" -> if (isVi) "Lưu Vào Bộ Sưu Tập" else "Save to Collection"
            "empty_gallery" -> if (isVi) "Hộp sưu tập rỗng!\nHãy tạo con tem đầu tiên của bạn." else "Empty collection!\nCreate your first postage stamp."
            "empty_album" -> if (isVi) "Chưa có con tem nào trong cuốn sổ tay này!\nHãy gán tem khi chụp." else "No stamps in this notebook!\nAssign stamps when creating them."
            "share_info" -> if (isVi) "Xem con tem tuyệt đẹp tôi tự tạo và sưu tầm bằng app Stampify: " else "Check out this beautiful postage stamp I created and collected via Stampify: "
            "success" -> if (isVi) "Thành công" else "Success"
            "create_stamp_inside_album" -> if (isVi) "Tạo thêm tem cho sổ tay này" else "Create new stamp inside this notebook"
            "stamps_count" -> if (isVi) "tem" else "stamps"
            else -> key
        }
    }

    // Filter and search parameters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Combined stream of stamps based on search & category filters
    val filteredStamps: StateFlow<List<Stamp>> = combine(
        repository.allStamps,
        _searchQuery,
        _selectedCategory
    ) { stamps, query, category ->
        var result = stamps
        if (category != null) {
            result = result.filter { it.category == category }
        }
        if (query.isNotEmpty()) {
            result = result.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.note.contains(query, ignoreCase = true) ||
                it.country.contains(query, ignoreCase = true) ||
                it.faceValue.contains(query, ignoreCase = true)
            }
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Exposed lists
    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val albums: StateFlow<List<Album>> = repository.allAlbums
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Initialize default categories
        viewModelScope.launch {
            repository.seedCategoriesIfEmpty()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryName: String?) {
        _selectedCategory.value = categoryName
    }

    // Creating Stamp
    fun addStamp(
        context: Context,
        name: String,
        category: String,
        bitmap: Bitmap,
        shape: String,
        note: String,
        faceValue: String,
        country: String,
        albumId: Int? = null,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    // 1. Process with custom StampProcessor
                    val processedStamp = StampProcessor.createStamp(
                        source = bitmap,
                        shape = shape,
                        title = name,
                        faceValue = faceValue,
                        country = country
                    )

                    // 2. Save physical image file in app internal storage
                    val stampDir = File(context.filesDir, "stamps")
                    if (!stampDir.exists()) stampDir.mkdirs()

                    val fileName = "stamp_${System.currentTimeMillis()}.png"
                    val file = File(stampDir, fileName)

                    FileOutputStream(file).use { out ->
                        processedStamp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }

                    // 3. Insert metadata to database
                    val stamp = Stamp(
                        name = name,
                        category = category,
                        filePath = file.absolutePath,
                        shape = shape,
                        note = note,
                        faceValue = faceValue,
                        country = country,
                        albumId = albumId
                    )
                    repository.insertStamp(stamp)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
            onComplete(success)
        }
    }

    fun addAlbum(title: String, description: String, coverColorHex: String) {
        viewModelScope.launch {
            repository.insertAlbum(
                Album(title = title, description = description, coverColorHex = coverColorHex)
            )
        }
    }

    fun deleteAlbum(album: Album) {
        viewModelScope.launch {
            repository.deleteAlbum(album)
        }
    }

    fun deleteStamp(stamp: Stamp) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val file = File(stamp.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                repository.deleteStamp(stamp)
            }
        }
    }

    fun addCustomCategory(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            repository.insertCategory(
                Category(name = name, colorHex = colorHex, iconName = iconName)
            )
        }
    }

    // Helper to decode a Uri safely from the gallery
    suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

class StampViewModelFactory(private val repository: StampRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StampViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StampViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
