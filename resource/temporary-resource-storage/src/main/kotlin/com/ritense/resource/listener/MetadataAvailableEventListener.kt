package com.ritense.resource.listener

import com.ritense.resource.domain.ResourceStorageMetadata
import com.ritense.resource.domain.ResourceStorageMetadataId
import com.ritense.resource.domain.StorageMetadataKeys
import com.ritense.resource.event.ResourceStorageMetadataAvailableEvent
import com.ritense.resource.repository.ResourceStorageMetadataRepository
import org.springframework.context.event.EventListener
import java.util.*

class MetadataAvailableEventListener(
    private val repository: ResourceStorageMetadataRepository
) {

    @EventListener(ResourceStorageMetadataAvailableEvent::class)
    fun storeResourceMetadata(event: ResourceStorageMetadataAvailableEvent) {
        val storageFileId: String = UUID.randomUUID().toString()

        repository.save(ResourceStorageMetadata(
            ResourceStorageMetadataId(storageFileId, StorageMetadataKeys.DOCUMENT_ID),
            event.documentId
        ))

        repository.save(ResourceStorageMetadata(
            ResourceStorageMetadataId(storageFileId, StorageMetadataKeys.DOWNLOAD_URL),
            event.downloadUrl
        ))
    }
}