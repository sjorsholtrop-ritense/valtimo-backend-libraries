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

package com.ritense.plugin

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.plugin.annotation.PluginCategory
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProperty
import com.ritense.plugin.service.PluginService
import java.util.UUID
import org.apache.commons.lang3.reflect.FieldUtils

/**
 *  This factory is meant to be extended for a specific type of plugin. It can create a plugin of type T given a
 *  configuration. The class extending this factory has to be registered as a bean of type PluginFactory<T>.
 */
abstract class PluginFactory<T>(
    var pluginService: PluginService
) {
    private var fullyQualifiedClassName: String = ""

    /**
     * Create the base plugin instance, without any additional plugin properties.
     *
     * @return plugin instance of type T
     */
    protected abstract fun create(): T

    /**
     * Creates a plugin of type T with configured properties of the provided PluginConfiguration
     * @param configuration
     *
     * @return plugin instance of type T
     */
    fun create(configuration: PluginConfiguration): T {
        val instance = create()

        injectProperties(instance, configuration)

        return instance
    }

    fun canCreate(configuration: PluginConfiguration): Boolean {
        if (fullyQualifiedClassName.isEmpty()) {
            val instance = create()
            fullyQualifiedClassName = instance!!::class.java.name
        }

        return this.fullyQualifiedClassName == configuration.pluginDefinition.fullyQualifiedClassName
    }

    private fun injectProperties(instance: T, configuration: PluginConfiguration) {
        if (configuration.properties == null) {
            return
        }

        val pluginDefinition = configuration.pluginDefinition
        val mapper = pluginService.getObjectMapper()
        val propertyIterator = configuration.properties!!.fields()

        while (propertyIterator.hasNext()) {
            val configuredPropertyEntry = propertyIterator.next()

            if (configuredPropertyEntry.value.isNull) {
                continue
            }

            val propertyDefinition = pluginDefinition.findPluginProperty(configuredPropertyEntry.key)

            setProperty(instance, propertyDefinition!!, configuredPropertyEntry.value, mapper)
        }
    }

    private fun setProperty(instance: T,
        propertyDefinition: PluginProperty,
        configuredProperty: JsonNode,
        mapper: ObjectMapper
    ) {
        val propertyType = Class.forName(propertyDefinition.fieldType)

        val propertyValue = if (propertyType.isAnnotationPresent(PluginCategory::class.java)) {
                val configurationId = PluginConfigurationId.existingId(UUID.fromString(configuredProperty.textValue()))

                pluginService.createInstance(configurationId)
            } else {
                mapper.treeToValue(
                    configuredProperty,
                    propertyType
                )
            }

        if(propertyDefinition.required) {
            requireNotNull(propertyValue) { "${propertyDefinition.fieldName} value was null on plugin '${propertyDefinition.pluginDefinition.key}'"}
        }

        FieldUtils.writeField(instance, propertyDefinition.fieldName, propertyValue, true)
    }
}