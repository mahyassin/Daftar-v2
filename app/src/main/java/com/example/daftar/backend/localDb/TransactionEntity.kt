package com.example.daftar.backend.localDb

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index


@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = Pages::class, parentColumns = ["id"], childColumns = ["pageId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("pageId")],
)
data class Transaction (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val details: String,
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

/** Balance is derived from entries, never from the legacy Pages.credit field. */
data class PageBalance(val id: Int, val name: String, val number: String, val credit: Long)
