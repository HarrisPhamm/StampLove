package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stamps")
data class Stamp(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // matches the Category.name
    val filePath: String, // file path on device's internal storage
    val shape: String,    // "RECTANGLE", "SQUARE", "CIRCLE"
    val dateCreated: Long = System.currentTimeMillis(),
    val note: String = "",
    val faceValue: String = "1000đ",
    val country: String = "Việt Nam",
    val albumId: Int? = null
)
