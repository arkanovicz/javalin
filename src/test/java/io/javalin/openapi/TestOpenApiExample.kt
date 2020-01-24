package io.javalin.openapi

import io.javalin.Javalin
import io.javalin.plugin.openapi.JavalinOpenApi
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.info.Info
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private data class ExampleUser(val name: String, val address: ExampleAddress)
private data class ExampleAddress(val street: String, val number: Int)

val examples = mapOf(
        ExampleUser::class.java to mapOf(
                "User example" to Example().apply {
                    summary = "A correctly configured user"
                    value = ExampleUser("John", ExampleAddress("Some street", 123))
                }
        ),
        ExampleAddress::class.java to mapOf(
                "Address example" to Example().apply {
                    summary = "A correctly configured address"
                    value = ExampleAddress("Some street", 123)
                }
        )
)

class TestOpenApiExample {

    private fun createApp(openApiPlugin: OpenApiPlugin) = Javalin.create {
        it.registerPlugin(openApiPlugin)
    }.apply {
        get("/user", documented(document().result<ExampleUser>("200")) {})
        get("/address", documented(document().result<ExampleAddress>("200")) {})
    }

    @Test
    fun `examples are generated when added in addExampleForSchema`() {
        val app = createApp(OpenApiPlugin(OpenApiOptions(Info()).apply {
            addExampleForSchema<ExampleUser>("User example", examples[ExampleUser::class.java]!!["User example"]!!)
            addExampleForSchema<ExampleAddress>("Address example", examples[ExampleAddress::class.java]!!["Address example"]!!)
        }))
        val openApiJson = JavalinOpenApi.createSchema(app).toString()
        assertThat(openApiJson).contains("value: ExampleUser(name=John, address=ExampleAddress(street=Some street, number=123))")
    }

    @Test
    fun `examples are generated when added as a map`() {
        val app = createApp(OpenApiPlugin(OpenApiOptions(Info()).apply {
            examples(examples)
        }))
        val openApiJson = JavalinOpenApi.createSchema(app).toString()
        assertThat(openApiJson).contains("value: ExampleUser(name=John, address=ExampleAddress(street=Some street, number=123))")
    }

}