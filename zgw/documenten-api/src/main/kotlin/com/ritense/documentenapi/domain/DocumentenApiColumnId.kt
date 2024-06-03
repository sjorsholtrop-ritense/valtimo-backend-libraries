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

package com.ritense.documentenapi.domain

import com.ritense.valtimo.contract.domain.AbstractId
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated

@Embeddable
data class DocumentenApiColumnId(
    @Column(name = "case_definition_name")
    val caseDefinitionName: String,
    @Column(name = "column_key")
    @Enumerated(STRING)
    val key: DocumentenApiColumnKey
) : AbstractId<DocumentenApiColumnId>()