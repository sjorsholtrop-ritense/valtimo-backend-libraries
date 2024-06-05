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

package com.ritense.formviewmodel

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.formviewmodel.autoconfigure.FormViewModelAutoConfiguration
import com.ritense.formviewmodel.submission.TestStartFormSubmissionHandler
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandler
import com.ritense.formviewmodel.viewmodel.TestViewModelLoader
import com.ritense.valtimo.contract.json.MapperSingleton
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.runApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@SpringBootApplication(
    scanBasePackageClasses = [FormViewModelAutoConfiguration::class]
)
class TestApplication {

    fun main(args: Array<String>) {
        runApplication<TestApplication>(*args)
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        @ConditionalOnMissingBean(ObjectMapper::class)
        fun objectMapper(): ObjectMapper {
            return MapperSingleton.get()
        }

        @Bean
        fun testViewModelLoader() = TestViewModelLoader()

        @Bean
        fun testSubmissionHandler(): FormViewModelStartFormSubmissionHandler<*> = TestStartFormSubmissionHandler()
    }
}
