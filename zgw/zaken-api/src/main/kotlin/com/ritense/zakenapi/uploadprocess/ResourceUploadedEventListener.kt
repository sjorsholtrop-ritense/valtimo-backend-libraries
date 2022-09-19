/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.uploadprocess

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.domain.MetadataType
import com.ritense.resource.domain.TemporaryResourceUploadedEvent
import com.ritense.resource.service.TemporaryResourceStorageService
import org.springframework.context.event.EventListener
import java.util.UUID

class ResourceUploadedEventListener(
    private val resourceService: TemporaryResourceStorageService,
    private val processDocumentService: ProcessDocumentService,
) {

    @EventListener(TemporaryResourceUploadedEvent::class)
    fun handle(event: TemporaryResourceUploadedEvent) {
        val metadata = resourceService.getResourceMetadata(event.resourceId)
        val documentId = metadata[MetadataType.DOCUMENT_ID.key] as String?

        if (documentId != null) {
            processDocumentService.startProcessForDocument(
                StartProcessForDocumentRequest(
                    JsonSchemaDocumentId.existingId(UUID.fromString(documentId)),
                    UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY,
                    mapOf(
                        RESOURCE_ID_PROCESS_VAR to event.resourceId,
                    ),
                )
            )
        }
    }

    companion object {
        const val RESOURCE_ID_PROCESS_VAR = "resourceId"
        const val UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY = "document-upload"
    }
}