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

package com.ritense.zakenapi.uploadprocess

import com.ritense.logging.LoggableResource
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.zakenapi.uploadprocess.UploadProcessService.Companion.DOCUMENT_UPLOAD
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class UploadProcessResource(
    private val documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
) {

    @Deprecated("Marked for removal since 9.22.0")
    @GetMapping("/v1/uploadprocess/case/{caseDefinitionName}/check-link")
    fun checkCaseProcessLink(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<CheckLinkResponse> {
        val link = documentDefinitionProcessLinkService.getDocumentDefinitionProcessLink(caseDefinitionName, DOCUMENT_UPLOAD)
        return ResponseEntity.ok(CheckLinkResponse(link.isPresent))
    }
}
