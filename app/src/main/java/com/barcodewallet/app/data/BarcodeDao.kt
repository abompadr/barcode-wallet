package com.barcodewallet.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeDao {
    @Query("SELECT * FROM barcodes ORDER BY name ASC")
    fun getAll(): Flow<List<BarcodeItem>>

    @Insert
    suspend fun insert(item: BarcodeItem)

    @Delete
    suspend fun delete(item: BarcodeItem)
}
