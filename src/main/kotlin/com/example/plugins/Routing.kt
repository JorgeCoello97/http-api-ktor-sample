package com.example.plugins

import com.example.models.Customer
import com.example.models.customerStorage
import com.example.models.orderStorage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting() {

    routing {
        customersRouting()
        ordersRouting()

        filesRouting()
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
