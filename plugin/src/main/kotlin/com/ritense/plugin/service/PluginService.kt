package com.ritense.plugin.service

import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.web.rest.dto.PluginActionDefinitionDto

class PluginService(
    private var pluginDefinitionRepository: PluginDefinitionRepository,
    private var pluginConfigurationRepository: PluginConfigurationRepository,
    private var pluginActionDefinitionRepository: PluginActionDefinitionRepository
) {

    fun getPluginDefinitions(): List<PluginDefinition> {
        return pluginDefinitionRepository.findAll()
    }

    fun getPluginConfigurations(): List<PluginConfiguration> {
        return pluginConfigurationRepository.findAll()
    }

    fun createPluginConfiguration(
        key: String,
        title: String,
        properties: String,
        pluginDefinitionKey: String
    ): PluginConfiguration {
        val pluginDefinition = pluginDefinitionRepository.getById(pluginDefinitionKey)

        return pluginConfigurationRepository.save(PluginConfiguration(key, title, properties, pluginDefinition))
    }

    fun getPluginDefinitionActions(
        pluginDefinitionKey: String,
        activityType: ActivityType?
    ): List<PluginActionDefinitionDto> {
        val actions = if (activityType == null)
            pluginActionDefinitionRepository.findByIdPluginDefinitionKey(pluginDefinitionKey)
        else
            pluginActionDefinitionRepository.findByIdPluginDefinitionKeyAndActivityTypes(pluginDefinitionKey, activityType)

        return actions.map {
            PluginActionDefinitionDto(
                it.id.key,
                it.title,
                it.description
            )
        }
    }
}