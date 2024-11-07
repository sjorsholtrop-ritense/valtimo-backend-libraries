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

package com.ritense.zaakdetails.documentobjectenapisync

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.domain.Document
import com.ritense.document.domain.event.DocumentCreatedEvent
import com.ritense.document.domain.event.DocumentModifiedEvent
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.service.DocumentService
import com.ritense.logging.LoggableResource
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.Comparator.EQUAL_TO
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.client.ObjectSearchParameter
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.management.ObjectManagementInfo
import com.ritense.objectenapi.management.ObjectManagementInfoProvider
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.zaakdetails.domain.ZaakdetailsObject
import com.ritense.zaakdetails.service.ZaakdetailsObjectService
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.ZaakObject
import com.ritense.zakenapi.link.ZaakInstanceLinkNotFoundException
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.LocalDate

@Transactional
@Service
@SkipComponentScan
class DocumentObjectenApiSyncService(
    private val documentObjectenApiSyncRepository: DocumentObjectenApiSyncRepository,
    private val objectObjectManagementInfoProvider: ObjectManagementInfoProvider,
    private val documentService: DocumentService,
    private val pluginService: PluginService,
    private val objectSyncService: ObjectSyncService,
    private val zaakUrlProvider: ZaakUrlProvider,
    private val zaakdetailsObjectService: ZaakdetailsObjectService
) {
    fun getSyncConfiguration(
        @LoggableResource(resourceType = JsonSchemaDocumentDefinition::class) documentDefinitionName: String,
        documentDefinitionVersion: Long
    ): DocumentObjectenApiSync? {
        logger.debug { "Get sync configuration documentDefinitionName=$documentDefinitionName" }
        return documentObjectenApiSyncRepository.findByDocumentDefinitionNameAndDocumentDefinitionVersion(
            documentDefinitionName,
            documentDefinitionVersion
        )
    }

    fun saveSyncConfiguration(sync: DocumentObjectenApiSync) {
        logger.info { "Save sync configuration documentDefinitionName=${sync.documentDefinitionName}" }
        val modifiedSync = getSyncConfiguration(sync.documentDefinitionName, sync.documentDefinitionVersion)
            ?.copy(
                objectManagementConfigurationId = sync.objectManagementConfigurationId,
                enabled = sync.enabled
            )
            ?: sync

        // Remove old connector configuration
        objectSyncService.getObjectSyncConfig(sync.documentDefinitionName).content
            .forEach { objectSyncService.removeObjectSyncConfig(it.id.id) }

        documentObjectenApiSyncRepository.save(modifiedSync)
    }

    fun deleteSyncConfigurationByDocumentDefinition(
        @LoggableResource(resourceType = JsonSchemaDocumentDefinition::class) documentDefinitionName: String,
        documentDefinitionVersion: Long
    ) {
        logger.info {
            """Delete sync configuration documentDefinitionName=$documentDefinitionName
                documentDefinitionVersion=$documentDefinitionVersion"""
        }
        documentObjectenApiSyncRepository.deleteByDocumentDefinitionNameAndDocumentDefinitionVersion(
            documentDefinitionName,
            documentDefinitionVersion
        )
    }

    @EventListener(DocumentCreatedEvent::class)
    fun handleDocumentCreatedEvent(event: DocumentCreatedEvent) {
        logger.info { "handle documentCreatedEvent documentId=${event.documentId()} definitionId=${event.definitionId()}" }
        sync(documentService.get(event.documentId().id.toString()))
    }

    @EventListener(DocumentModifiedEvent::class)
    fun handleDocumentModifiedEvent(event: DocumentModifiedEvent) {
        logger.info { "handle documentModifiedEvent documentId=${event.documentId()}" }
        sync(documentService.get(event.documentId().id.toString()))
    }

    private fun sync(document: Document) {
        val syncConfiguration = getSyncConfiguration(document.definitionId().name(), document.definitionId().version())
        if (syncConfiguration?.enabled == true) {
            val objectManagementConfiguration =
                objectObjectManagementInfoProvider.getObjectManagementInfo(syncConfiguration.objectManagementConfigurationId)
            val objectenApiPlugin =
                pluginService.createInstance<ObjectenApiPlugin>(objectManagementConfiguration.objectenApiPluginConfigurationId)
            val objecttypenApiPlugin =
                pluginService.createInstance<ObjecttypenApiPlugin>(objectManagementConfiguration.objecttypenApiPluginConfigurationId)

            val objectRequest = getObjectRequest(document, objecttypenApiPlugin, objectManagementConfiguration)

            val zaakdetailsObject: ZaakdetailsObject;
            val zaakdetailsObjectOptional = zaakdetailsObjectService.findById(document.id().id)

            val checkExistingZaakObjectBeforeCreating: Boolean;

            //Object exists and reference has been stored: update
            if(zaakdetailsObjectOptional.isPresent) {
                zaakdetailsObject = zaakdetailsObjectOptional.get()

                objectenApiPlugin.objectUpdate(zaakdetailsObject.objectURI, objectRequest)

                //The reference exists, the zaakobject has been created before
                checkExistingZaakObjectBeforeCreating = false
            } else {
                val existingObjectWrapper = getObjectWithCaseId(document, objectenApiPlugin, objectManagementConfiguration, objecttypenApiPlugin)

                if(existingObjectWrapper == null) { //Object does not exist: create and store reference
                    val newObjectWrapper = objectenApiPlugin.createObject(objectRequest)
                    zaakdetailsObject = ZaakdetailsObject(
                        id = document.id().id,
                        objectURI = newObjectWrapper.url
                    )

                    //New object, the zaakobject can't exist yet
                    checkExistingZaakObjectBeforeCreating = false
                } else { //Object exists, but reference has not been stored: update and store reference
                    objectenApiPlugin.objectUpdate(existingObjectWrapper.url, objectRequest)

                    zaakdetailsObject = ZaakdetailsObject(
                        id = document.id().id,
                        objectURI = existingObjectWrapper.url
                    )

                    //Existing object, we don't know whether the zaakobject already exists
                    checkExistingZaakObjectBeforeCreating = true
                }

                zaakdetailsObjectService.save(zaakdetailsObject)
            }

            if(!zaakdetailsObject.linkedToZaak) {
                linkToZaak(zaakdetailsObject, checkExistingZaakObjectBeforeCreating)
            }
        }
    }

    private fun getObjectRequest(
        document: Document,
        objecttypenApiPlugin: ObjecttypenApiPlugin,
        objectManagementConfiguration: ObjectManagementInfo
    ): ObjectRequest {
        val content = document.content().asJson() as ObjectNode
        content.put("caseId", document.id().id.toString())

        return ObjectRequest(
            type = objecttypenApiPlugin.getObjectTypeUrlById(objectManagementConfiguration.objecttypeId),
            record = ObjectRecord(
                typeVersion = objectManagementConfiguration.objecttypeVersion,
                data = content,
                startAt = LocalDate.now()
            )
        )
    }

    private fun getObjectWithCaseId(
        document: Document,
        objectenApiPlugin: ObjectenApiPlugin,
        objectManagementConfiguration: ObjectManagementInfo,
        objecttypenApiPlugin: ObjecttypenApiPlugin
        ): ObjectWrapper? {

        val searchString = ObjectSearchParameter.toQueryParameter(
            ObjectSearchParameter("caseId", EQUAL_TO, document.id().toString())
        )

        return objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = objecttypenApiPlugin.url,
            objecttypeId = objectManagementConfiguration.objecttypeId,
            searchString = searchString,
            pageable = PageRequest.of(0, 2)
        ).results.firstOrNull()
    }

    private fun linkToZaak(
        zaakdetailsObject: ZaakdetailsObject,
        checkExistingZaakObjectBeforeCreating: Boolean
    ) {
        try {
            val zaakUri = zaakUrlProvider.getZaakUrl(zaakdetailsObject.id)

            val zakenApiPlugin = pluginService.createInstance(
                ZakenApiPlugin::class.java,
                ZakenApiPlugin.findConfigurationByUrl(zaakUri)
            )

            if(zakenApiPlugin == null) {
                //Valid scenario: Zaken API plugin has not been configured. Ignore.
                return
            }

            if(checkExistingZaakObjectBeforeCreating) {
                val zaakobject = zakenApiPlugin.getZaakObject(zaakUri, zaakdetailsObject.objectURI)
                if (zaakobject == null) {
                    createZaakObject(zaakUri, zakenApiPlugin, zaakdetailsObject)
                }
            } else {
                createZaakObject(zaakUri, zakenApiPlugin, zaakdetailsObject)
            }

            zaakdetailsObject.linkedToZaak = true
            zaakdetailsObjectService.save(zaakdetailsObject)
        } catch (e: ZaakInstanceLinkNotFoundException) {
            //Valid scenario: zaak does not exist yet. Ignore.
            return
        }
    }

    private fun createZaakObject(
        zaakUri: URI,
        zakenApiPlugin: ZakenApiPlugin,
        zaakdetailsObject: ZaakdetailsObject
    ) {
        zakenApiPlugin.createZaakObject(
            zaakUrl = zaakUri,
            objectUrl = zaakdetailsObject.objectURI,
            objectTypeOverige = "zaakdetails",
            documentId = zaakdetailsObject.id
        )
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }

}
