package com.llmchat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "providers")
data class Provider(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val models: String,
    val isDefault: Boolean = false
)
