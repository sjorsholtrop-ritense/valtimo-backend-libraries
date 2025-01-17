package com.ritense.exact.client.endpoints

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.exact.client.endpoints.structs.AuthorizedExactEndpoint
import org.springframework.web.client.RestClient

class PutEndpoint(
    accessToken: String,
    private val uri: String,
    private val content: String
) : AuthorizedExactEndpoint<JsonNode>(JsonNode::class.java, accessToken) {
    override fun create(client: RestClient): RestClient.RequestHeadersSpec<*> {
        return client
            .put()
            .uri(uri)
            .body(content)
    }
}