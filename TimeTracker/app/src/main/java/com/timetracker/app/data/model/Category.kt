package com.timetracker.app.data.model

data class Category(
    val id: Long = 0,
    val name: String,
    val color: String,
    val icon: String,
    val sortOrder: Int = 0
)

fun Category.toEntity() = com.timetracker.app.data.local.entity.CategoryEntity(
    id = id,
    name = name,
    color = color,
    icon = icon,
    sortOrder = sortOrder
)

fun com.timetracker.app.data.local.entity.CategoryEntity.toModel() = Category(
    id = id,
    name = name,
    color = color,
    icon = icon,
    sortOrder = sortOrder
)
