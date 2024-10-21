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

package com.ritense.extension.web.rest

import com.ritense.extension.ExtensionManager
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.pf4j.update.UpdateManager
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files

@RestController
@SkipComponentScan
@RequestMapping(value = ["/api"])
class ExtensionResource(
    private val extensionManager: ExtensionManager,
    private val updateManager: UpdateManager,
) {

    @PostMapping("/v1/extension/install", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun installPlugin(
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<Unit> {

        val jarFile = Files.createTempFile("temporaryResource", ".jar")
        jarFile.toFile().outputStream().use { file.inputStream.copyTo(it) }

        val pluginId = extensionManager.loadPlugin(jarFile)
        extensionManager.startPlugin(pluginId)

        return ResponseEntity.noContent().build()
    }

    @PostMapping("/v1/extension/uninstall")
    fun uninstallAllPlugins(): ResponseEntity<Unit> {
        extensionManager.stopPlugins()
        extensionManager.unloadPlugins()
        return ResponseEntity.noContent().build()
    }
}