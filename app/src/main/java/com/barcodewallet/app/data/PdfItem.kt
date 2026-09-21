package com.barcodewallet.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdfs")
data class PdfItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val filePath: String,
    val protected: Boolean = false
)
