package com.example.plugins

import com.example.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.util.*

fun Application.configureRouting() {

    routing {
        customersRouting()
        ordersRouting()

        filesRouting()

        articlesRouting()
    }
}

fun Route.articlesRouting() {
    get("/") {
        call.respondRedirect("articles")
    }
    route("articles") {
        get {
            call.respond(FreeMarkerContent("index.ftl", mapOf("articles" to articlesStorage)))
        }

        get("new") {
            call.respond(FreeMarkerContent("new.ftl", model = null))
        }

        post {
            val formParameters = call.receiveParameters()
            val title = formParameters.getOrFail("title")
            val body = formParameters.getOrFail("body")
            val newEntry = Article.newEntry(title, body)
            articlesStorage.add(newEntry)
            call.respondRedirect("/articles/${newEntry.id}")
        }

        route("{id}") {
            get {
                val id = call.parameters.getOrFail<Int>("id").toInt()
                call.respond(FreeMarkerContent("show.ftl",
                    mapOf(
                        "article" to articlesStorage.find { it.id == id }
                    )
                ))
            }
            get("edit") {
                val id = call.parameters.getOrFail<Int>("id").toInt()
                call.respond(FreeMarkerContent("edit.ftl",
                    mapOf(
                        "article" to articlesStorage.find { it.id == id }
                    )
                ))
            }
            post {
                val id = call.parameters.getOrFail<Int>("id").toInt()
                val formParameters = call.receiveParameters()
                when (formParameters.getOrFail("_action")) {
                    "update" -> {
                        val index = articlesStorage.indexOf(articlesStorage.find { it.id == id })
                        val title = formParameters.getOrFail("title")
                        val body = formParameters.getOrFail("body")
                        articlesStorage[index].title = title
                        articlesStorage[index].body = body
                        call.respondRedirect("/articles/$id")
                    }
                    "delete" -> {
                        articlesStorage.removeIf { it.id == id }
                        call.respondRedirect("/articles")
                    }
                }
            }
        }
    }
}


fun Route.filesRouting() {
    static("/static") {
        resources("files")
    }
}

fun Route.customersRouting() {
    route("/customer") {
        get {
            if (customerStorage.isNotEmpty()) {
                call.respond(customerStorage)
            } else {
                call.respondText(
                    text = "No customers found",
                    status = HttpStatusCode.OK
                )
            }
        }
        post {
            val customer = call.receive<Customer>()
            customerStorage.add(customer)
            call.respondText(
                text = "Customer stored correctly",
                status = HttpStatusCode.Created
            )
        }

        route("{id?}") {
            get {
                val id = call.parameters["id"] ?: return@get call.respondText(
                    text = "Missing id",
                    status = HttpStatusCode.BadRequest
                )
                val customer = customerStorage.find { it.id == id } ?: return@get call.respondText(
                    text = "No customer with id $id",
                    status = HttpStatusCode.NotFound
                )
                call.respond(customer)
            }
            delete {
                val id = call.parameters["id"] ?: return@delete call.respondText(
                    text = "Missing id",
                    status = HttpStatusCode.BadRequest
                )
                if (customerStorage.removeIf { it.id == id }) {
                    call.respondText(
                        text = "Customer removed correctly",
                        status = HttpStatusCode.Accepted
                    )
                } else {
                    call.respondText(
                        text = "No customer with id $id",
                        status = HttpStatusCode.NotFound
                    )
                }
            }
        }
    }
}

fun Route.ordersRouting() {
    route("/order") {
        get {
            if (orderStorage.isNotEmpty()) {
                call.respond(orderStorage)
            } else {
                call.respondText(
                    text = "No orders found",
                    status = HttpStatusCode.OK
                )
            }
        }
        route("/{id?}") {
            get {
                val id = call.parameters["id"] ?: return@get call.respondText(
                    text = "Missing id",
                    status = HttpStatusCode.BadRequest
                )
                val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
                    text = "No order with number $id",
                    status = HttpStatusCode.NotFound
                )
                call.respond(order)
            }
            get("/total") {
                val id = call.parameters["id"] ?: return@get call.respondText(
                    text = "Missing id",
                    status = HttpStatusCode.BadRequest
                )
                val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
                    text = "No order with number $id",
                    status = HttpStatusCode.NotFound
                )
                val total = order.contents.sumOf { it.price * it.amount }
                call.respond(total)
            }
        }
    }
}
