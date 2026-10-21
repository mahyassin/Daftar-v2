package com.example.daftar.backend.localDb

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "transactions")
data class Transaction (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val credit: Int,
    val isPayed: Boolean,
    val pageId: Int,
)

@Entity(tableName = "Pages")
data class Pages(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val number: String,
    val credit: Int,
)