/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.dashboard.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.dashboard.domain.Dashboard
import com.ritense.dashboard.domain.WidgetConfiguration
import com.ritense.dashboard.repository.DashboardRepository
import com.ritense.dashboard.repository.WidgetConfigurationRepository
import com.ritense.dashboard.web.rest.dto.DashboardUpdateRequestDto
import com.ritense.dashboard.web.rest.dto.WidgetConfigurationUpdateRequestDto
import com.ritense.valtimo.contract.authentication.UserManagementService
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Transactional
class DashboardService(
    private val dashboardRepository: DashboardRepository,
    private val widgetConfigurationRepository: WidgetConfigurationRepository,
    private val userManagementService: UserManagementService
) {

    @Transactional(readOnly = true)
    fun getDashboards(): List<Dashboard> {
        return dashboardRepository.findAllByOrderByOrder()
    }

    @Transactional(readOnly = true)
    fun getDashboard(dashboardKey: String): Dashboard {
        return dashboardRepository.findById(dashboardKey)
            .orElseThrow { RuntimeException("No dashboard found with key '$dashboardKey'") }
    }

    fun createDashboard(title: String, description: String): Dashboard {
        val key = generateDashboardKey(title)
        val order = dashboardRepository.count().toInt()
        val createdBy = userManagementService.currentUser.fullName
        return dashboardRepository.save(
            Dashboard(
                key = key,
                title = title,
                description = description,
                order = order,
                createdBy = createdBy
            )
        )
    }

    fun updateDashboards(dashboardUpdateDtos: List<DashboardUpdateRequestDto>): List<Dashboard> {
        val dashboards = dashboardUpdateDtos.mapIndexed { index, dashboardUpdateDto ->
            dashboardRepository.findById(dashboardUpdateDto.key)
                .getOrElse { throw RuntimeException("Failed to update dashboard. Dashboard with key '${dashboardUpdateDto.key}' doesn't exist.") }
                .copy(
                    title = dashboardUpdateDto.title,
                    description = dashboardUpdateDto.description,
                    order = index
                )
        }

        dashboardRepository.deleteAll()
        return dashboardRepository.saveAll(dashboards)
    }

    fun deleteDashboard(dashboardKey: String) {
        dashboardRepository.deleteById(dashboardKey)
        updateDashboardOrder()
    }

    @Transactional(readOnly = true)
    fun getWidgetConfigurations(dashboardKey: String): List<WidgetConfiguration> {
        return widgetConfigurationRepository.findAllByDashboardKey(dashboardKey)
    }

    fun createWidgetConfiguration(
        dashboardKey: String,
        title: String,
        dataSourceKey: String,
        displayType: String,
        dataSourceProperties: ObjectNode
    ): WidgetConfiguration {
        val key = generateWidgetKey(title)
        val order = widgetConfigurationRepository.countAllByDashboardKey(dashboardKey).toInt()
        return widgetConfigurationRepository.save(
            WidgetConfiguration(
                title = title,
                key = key,
                dashboard = getDashboard(dashboardKey),
                dataSourceKey = dataSourceKey,
                dataSourceProperties = dataSourceProperties,
                displayType = displayType,
                order = order,
            )
        )
    }

    fun updateWidgetConfigurations(
        dashboardKey: String,
        widgetConfigurationUpdateDtos: List<WidgetConfigurationUpdateRequestDto>
    ): List<WidgetConfiguration> {
        widgetConfigurationUpdateDtos.forEach {
            if (!widgetConfigurationRepository.existsByDashboardKeyAndKey(dashboardKey, it.key)) {
                throw RuntimeException("Failed to update widget configuration. Widget configuration with key '${it.key}' and dashboard '$dashboardKey' doesn't exist.")
            }
        }

        val dashboard = getDashboard(dashboardKey)
        val widgetConfigurations = widgetConfigurationUpdateDtos.mapIndexed { index, widgetConfigurationUpdateDto ->
            WidgetConfiguration(
                key = widgetConfigurationUpdateDto.key,
                title = widgetConfigurationUpdateDto.title,
                dashboard = dashboard,
                dataSourceKey = widgetConfigurationUpdateDto.dataSourceKey,
                dataSourceProperties = widgetConfigurationUpdateDto.dataSourceProperties,
                displayType = widgetConfigurationUpdateDto.displayType,
                order = index,
            )
        }

        widgetConfigurationRepository.deleteAll()
        return widgetConfigurationRepository.saveAll(widgetConfigurations)
    }

    @Transactional(readOnly = true)
    fun getWidgetConfiguration(dashboardKey: String, widgetKey: String): WidgetConfiguration {
        return widgetConfigurationRepository.findByDashboardKeyAndKey(dashboardKey, widgetKey)
            ?: throw RuntimeException("No widget configuration found with key '$widgetKey' for dashboard '$dashboardKey'")
    }

    fun deleteWidgetConfiguration(dashboardKey: String, widgetConfigurationKey: String) {
        widgetConfigurationRepository.deleteByDashboardKeyAndKey(dashboardKey, widgetConfigurationKey)
        updateWidgetConfigurationOrder(dashboardKey)
    }

    private fun updateDashboardOrder() {
        val dashboards = dashboardRepository.findAllByOrderByOrder()
            .mapIndexed { index, dashboard -> dashboard.copy(order = index) }
        dashboardRepository.saveAll(dashboards)
    }

    private fun updateWidgetConfigurationOrder(dashboardKey: String) {
        val widgetConfigurations = widgetConfigurationRepository.findAllByDashboardKeyOrderByOrder(dashboardKey)
            .mapIndexed { index, widgetConfiguration -> widgetConfiguration.copy(order = index) }
        widgetConfigurationRepository.saveAll(widgetConfigurations)
    }

    private fun generateDashboardKey(title: String): String {
        val baseKey = generateKey(title)
        var key = baseKey
        var i = 2
        while (dashboardRepository.existsById(key)) {
            key = "${baseKey}_${i++}"
        }
        return key
    }

    private fun generateWidgetKey(title: String): String {
        val baseKey = generateKey(title)
        var key = baseKey
        var i = 2
        while (widgetConfigurationRepository.existsById(key)) {
            key = "${baseKey}_${i++}"
        }
        return key
    }

    private fun generateKey(title: String): String {
        return title
            .lowercase()
            .replace("(^[^a-z]+)|([^0-9a-z]+\$)".toRegex(), "") // trim start and end
            .replace("[^0-9a-z]+".toRegex(), "_") // replace all non-alphanumeric characters with '_'
    }
}