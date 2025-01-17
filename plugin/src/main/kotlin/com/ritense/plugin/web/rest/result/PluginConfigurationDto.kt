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

package com.ritense.plugin.web.rest.result

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginDefinition

class PluginConfigurationDto(
    pluginConfiguration: PluginConfiguration
) {
    val id: String
    val title: String
    var properties: ObjectNode? = null
    val pluginDefinition: PluginDefinition

    init {
        id = pluginConfiguration.id.id.toString()
        title = pluginConfiguration.title
        pluginDefinition = pluginConfiguration.pluginDefinition

        if (pluginConfiguration.properties != null) {
            val configurationProperties: ObjectNode = pluginConfiguration.properties!!.deepCopy()

            val secretDefinitionProperties = pluginConfiguration.pluginDefinition.properties.filter {
                it.secret
            }.map {
                it.fieldName
            }

            configurationProperties.remove(secretDefinitionProperties)

            properties = configurationProperties
        }

    }
}