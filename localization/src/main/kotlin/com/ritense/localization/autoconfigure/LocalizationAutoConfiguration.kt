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

package com.ritense.localization.autoconfigure

import com.ritense.localization.repository.LocalizationRepository
import com.ritense.localization.security.config.LocalizationHttpSecurityConfigurer
import com.ritense.localization.service.LocalizationService
import com.ritense.localization.web.rest.LocalizationResource
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource

@AutoConfiguration
@EnableJpaRepositories(basePackages = ["com.ritense.localization.repository"])
@EntityScan("com.ritense.localization.domain")
@EnableCaching
class LocalizationAutoConfiguration {

    @Order(HIGHEST_PRECEDENCE + 2)
    @Bean
    @ConditionalOnClass(DataSource::class)
    @ConditionalOnMissingBean(name = ["localizationLiquibaseMasterChangeLogLocation"])
    fun localizationLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/localization-master.xml")
    }

    @Order(270)
    @Bean
    @ConditionalOnMissingBean(LocalizationHttpSecurityConfigurer::class)
    fun localizationHttpSecurityConfigurer(): LocalizationHttpSecurityConfigurer {
        return LocalizationHttpSecurityConfigurer()
    }
    @Bean
    @ConditionalOnMissingBean(LocalizationService::class)
    fun localizationService(
        localizationRepository: LocalizationRepository,
    ): LocalizationService {
        return LocalizationService(
            localizationRepository
        )
    }
//
//    @Bean
//    @ConditionalOnMissingBean(DashboardDataService::class)
//    fun dashboardDataService(
//        applicationContext: ApplicationContext,
//        widgetDataSourceResolver: WidgetDataSourceResolver,
//        widgetConfigurationRepository: WidgetConfigurationRepository,
//        objectMapper: ObjectMapper
//    ): DashboardDataService {
//        return DashboardDataService(
//            applicationContext,
//            widgetDataSourceResolver,
//            widgetConfigurationRepository,
//            objectMapper
//        )
//    }
//
//    @Bean
//    @ConditionalOnMissingBean(AdminDashboardResource::class)
//    fun adminDashboardResource(
//        dashboardService: DashboardService
//    ): AdminDashboardResource {
//        return AdminDashboardResource(dashboardService)
//    }
//
    @Bean
    @ConditionalOnMissingBean(LocalizationResource::class)
    fun localizationResource(
        localizationService: LocalizationService,
    ): LocalizationResource {
        return LocalizationResource(localizationService)
    }
//
//    @Bean
//    @ConditionalOnMissingBean(WidgetDataSourceResolver::class)
//    fun widgetDataSourceResolver(context: ApplicationContext): WidgetDataSourceResolver {
//        return WidgetDataSourceResolver(context)
//    }
//
//    @Bean
//    @ConditionalOnMissingBean(DashboardDeployer::class)
//    fun dashboardDeployer(
//        objectMapper: ObjectMapper,
//        dashboardRepository: DashboardRepository,
//        changelogService: ChangelogService,
//        @Value("\${valtimo.changelog.dashboard.clear-tables:false}") clearTables: Boolean
//    ): DashboardDeployer {
//        return DashboardDeployer(
//            objectMapper,
//            dashboardRepository,
//            changelogService,
//            clearTables
//        )
//    }
}
