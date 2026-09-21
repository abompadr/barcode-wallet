package com.barcodewallet.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfDao {
    @Query("SELECT * FROM pdfs ORDER BY name ASC")
    fun getAll(): Flow<List<PdfItem>>

    @Insert
    suspend fun insert(item: PdfItem)

    @Delete
    suspend fun delete(item: PdfItem)

    @Update
    suspend fun update(item: PdfItem)
}
