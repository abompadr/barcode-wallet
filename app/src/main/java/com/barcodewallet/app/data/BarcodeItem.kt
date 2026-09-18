package com.barcodewallet.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barcodes")
data class BarcodeItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val value: String
)
