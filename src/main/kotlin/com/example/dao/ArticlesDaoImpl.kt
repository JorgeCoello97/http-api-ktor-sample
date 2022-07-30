package com.example.dao

import com.example.models.Article
import com.example.tables.ArticlesTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*

class ArticlesDaoImpl : ArticlesDao{

    private fun resultRowToArticle(row: ResultRow) = Article(
        id = row[ArticlesTable.id],
        title = row[ArticlesTable.title],
        body = row[ArticlesTable.body],
    )

    override suspend fun allArticles(): List<Article> = DatabaseFactory.dbQuery {
        ArticlesTable.selectAll().map(::resultRowToArticle)
    }

    override suspend fun article(id: Int): Article? = DatabaseFactory.dbQuery {
        ArticlesTable
            .select { ArticlesTable.id eq id }
            .map(::resultRowToArticle)
            .singleOrNull()
    }


    override suspend fun addNewArticle(title: String, body: String): Article? = DatabaseFactory.dbQuery {
        val insertStatement = ArticlesTable.insert {
            it[ArticlesTable.title] = title
            it[ArticlesTable.body] = body
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToArticle)
    }


    override suspend fun editArticle(id: Int, title: String, body: String): Boolean = DatabaseFactory.dbQuery {
        ArticlesTable.update({ ArticlesTable.id eq id }) {
            it[ArticlesTable.title] = title
            it[ArticlesTable.body] = body
        } > 0
    }

    override suspend fun deleteArticle(id: Int): Boolean = DatabaseFactory.dbQuery {
        ArticlesTable.deleteWhere { ArticlesTable.id eq id } > 0
    }

}

val dao: ArticlesDao = ArticlesDaoImpl().apply {
    runBlocking {
        if(allArticles().isEmpty()) {
            addNewArticle("The drive to develop!", "...it's what keeps me going.")
        }
    }
}