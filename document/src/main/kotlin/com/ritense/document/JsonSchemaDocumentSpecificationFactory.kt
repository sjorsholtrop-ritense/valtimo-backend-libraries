/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.document

import com.ritense.authorization.permission.Permission
import com.ritense.authorization.request.AuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.authorization.specification.AuthorizationSpecificationFactory
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.service.JsonSchemaDocumentSpecification
import com.ritense.valtimo.contract.database.QueryDialectHelper

class JsonSchemaDocumentSpecificationFactory(
    private val documentRepository: JsonSchemaDocumentRepository,
    private var queryDialectHelper: QueryDialectHelper
) : AuthorizationSpecificationFactory<JsonSchemaDocument> {

    override fun create(
        request: AuthorizationRequest<JsonSchemaDocument>,
        permissions: List<Permission>
    ): AuthorizationSpecification<JsonSchemaDocument> {
        return JsonSchemaDocumentSpecification(
            request,
            permissions,
            documentRepository,
            queryDialectHelper
        )
    }

    override fun canCreate(request: AuthorizationRequest<*>, permissions: List<Permission>): Boolean {
        return JsonSchemaDocument::class.java == request.resourceType
    }
}