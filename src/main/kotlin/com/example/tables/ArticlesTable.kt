package com.example.tables

import org.jetbrains.exposed.sql.Table

object ArticlesTable : Table(){
    val id = integer("id").autoIncrement()
    val title = varchar("title", 128)
    val body = varchar("body", 1024)

    override val primaryKey = PrimaryKey(id)
}