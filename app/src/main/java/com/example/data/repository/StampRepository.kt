package com.example.data.repository

import com.example.data.db.CategoryDao
import com.example.data.db.StampDao
import com.example.data.db.AlbumDao
import com.example.data.model.Category
import com.example.data.model.Stamp
import com.example.data.model.Album
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class StampRepository(
    private val stampDao: StampDao,
    private val categoryDao: CategoryDao,
    private val albumDao: AlbumDao
) {
    val allStamps: Flow<List<Stamp>> = stampDao.getAllStampsFlow()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategoriesFlow()
    val allAlbums: Flow<List<Album>> = albumDao.getAllAlbumsFlow()

    suspend fun insertAlbum(album: Album): Long {
        return albumDao.insertAlbum(album)
    }

    suspend fun deleteAlbum(album: Album) {
        albumDao.deleteAlbum(album)
    }

    fun getStampsByCategory(categoryName: String): Flow<List<Stamp>> {
        return stampDao.getStampsByCategoryFlow(categoryName)
    }

    fun searchStamps(query: String): Flow<List<Stamp>> {
        return stampDao.searchStampsFlow("%$query%")
    }

    suspend fun insertStamp(stamp: Stamp) {
        stampDao.insertStamp(stamp)
    }

    suspend fun updateStamp(stamp: Stamp) {
        stampDao.updateStamp(stamp)
    }

    suspend fun deleteStamp(stamp: Stamp) {
        stampDao.deleteStamp(stamp)
    }

    suspend fun getStampById(id: Int): Stamp? {
        return stampDao.getStampById(id)
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun seedCategoriesIfEmpty() {
        val current = categoryDao.getAllCategories()
        if (current.isEmpty()) {
            val defaults = listOf(
                Category(name = "Tự nhiên & Phong cảnh", colorHex = "#008080", iconName = "Nature"),
                Category(name = "Động vật hoang dã", colorHex = "#D2691E", iconName = "Pets"),
                Category(name = "Lịch sử & Quốc gia", colorHex = "#A52A2A", iconName = "Public"),
                Category(name = "Hội họa & Nghệ thuật", colorHex = "#8A2BE2", iconName = "Brush"),
                Category(name = "Du lịch & Danh lam", colorHex = "#3B5998", iconName = "FlightTakeoff"),
                Category(name = "Kỷ niệm cá nhân", colorHex = "#FF69B4", iconName = "Favorite"),
                Category(name = "Mẫu khác", colorHex = "#708090", iconName = "Category")
            )
            categoryDao.insertInitialCategories(defaults)
        }
    }
}
