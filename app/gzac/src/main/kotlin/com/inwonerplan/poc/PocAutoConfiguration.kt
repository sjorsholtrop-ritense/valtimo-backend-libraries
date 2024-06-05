package com.inwonerplan.poc

import com.inwonerplan.poc.aanbod.AanbodViewModelLoader
import com.inwonerplan.poc.aanbod.OnAanbodSubmittedEventHandler
import com.inwonerplan.poc.aanbod.command.SaveAanbodSubmissionCommandHandler
import com.inwonerplan.poc.start.StartSubmissionHandler
import com.inwonerplan.poc.start.StartViewModelLoader
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandler
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandler
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class PocAutoConfiguration {

    @Bean
    fun aanbodViewModelLoader() = AanbodViewModelLoader()
    @Bean
    fun onAanbodSubmittedEventHandler() : FormViewModelUserTaskSubmissionHandler<*> = OnAanbodSubmittedEventHandler()

    @Bean
    fun startViewModelLoader() = StartViewModelLoader()
    @Bean
    fun startSubmissionHandler(
        jsonSchemaDocumentService: JsonSchemaDocumentService
    ) : FormViewModelStartFormSubmissionHandler<*> = StartSubmissionHandler(
        jsonSchemaDocumentService
    )

    @Bean
    fun saveAanbodSubmissionCommandHandler(
        documentService: DocumentService
    ) = SaveAanbodSubmissionCommandHandler(
        documentService
    )

}