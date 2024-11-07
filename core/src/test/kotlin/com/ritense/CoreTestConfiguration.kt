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

package com.ritense

import com.ritense.authorization.AuthorizationService
import com.ritense.case.configuration.CaseAutoConfiguration
import com.ritense.case_.configuration.CaseWidgetAutoConfiguration
import com.ritense.valtimo.FakeUserRepository
import com.ritense.valtimo.camunda.authorization.UnauthorizedProcessBean
import com.ritense.valtimo.contract.annotation.ProcessBean
import com.ritense.valtimo.contract.config.LiquibaseRunnerAutoConfiguration
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.logging.impl.LoggingTestBean
import org.mockito.Mockito.mock
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@SpringBootApplication(
    scanBasePackageClasses = [
        LiquibaseRunnerAutoConfiguration::class,
        CaseAutoConfiguration::class,
        CaseWidgetAutoConfiguration::class
    ]
)
class CoreTestConfiguration {

    fun main(args: Array<String>) {
        SpringApplication.run(CoreTestConfiguration::class.java, *args)
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        fun fakeUserRepository(): FakeUserRepository {
            return FakeUserRepository()
        }

        @Bean
        fun mailSender(): MailSender {
            return mock(MailSender::class.java)
        }

        @Bean
        @ProcessBean
        fun unauthBean(
            authorizationService: AuthorizationService
        ): UnauthorizedProcessBean {
            return UnauthorizedProcessBean(authorizationService)
        }

        @Bean
        @ProcessBean
        fun loggingTestBean(): LoggingTestBean {
            return LoggingTestBean()
        }
    }
}