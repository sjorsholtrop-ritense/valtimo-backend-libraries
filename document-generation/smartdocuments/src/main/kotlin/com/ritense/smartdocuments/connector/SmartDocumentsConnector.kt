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

package com.ritense.smartdocuments.connector

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.documentgeneration.domain.GeneratedDocument
import com.ritense.smartdocuments.client.SmartDocumentsClient
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.smartdocuments.domain.GeneratedSmartDocument
import com.ritense.smartdocuments.domain.SmartDocumentsRequest
import java.util.Base64
import org.apache.commons.io.FilenameUtils

@Deprecated("Since 12.0.0", ReplaceWith("com.ritense.smartdocuments.plugin.SmartDocumentsPlugin"))
@ConnectorType(name = "SmartDocuments")
class SmartDocumentsConnector(
    private var smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties,
    private val smartDocumentsClient: SmartDocumentsClient,
) : Connector {

    override fun getProperties(): ConnectorProperties {
        return smartDocumentsConnectorProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        smartDocumentsConnectorProperties = connectorProperties as SmartDocumentsConnectorProperties
        smartDocumentsClient.setProperties(smartDocumentsConnectorProperties)
    }

    fun generateDocument(
        templateGroup: String,
        templateName: String,
        templateData: Map<String, Any>,
        format: DocumentFormatOption
    ): GeneratedDocument {
        smartDocumentsClient.setProperties(smartDocumentsConnectorProperties)
        val filesResponse = smartDocumentsClient.generateDocument(
            SmartDocumentsRequest(
                templateData,
                SmartDocumentsRequest.SmartDocument(
                    SmartDocumentsRequest.Selection(
                        templateGroup,
                        templateName
                    )
                )
            )
        )
        val outputFormat = filesResponse.file.map { it.outputFormat }
        val pdfResponse = filesResponse.file
            .firstOrNull { it.outputFormat.equals(format.toString(), ignoreCase = true) }
            ?: throw NoSuchElementException("Requested document format is '$format' but the available formats are '${outputFormat}'")

        return GeneratedSmartDocument(
            pdfResponse.filename,
            FilenameUtils.getExtension(pdfResponse.filename),
            format.mediaType.toString(),
            Base64.getDecoder().decode(pdfResponse.document.data),
        )
    }

}
