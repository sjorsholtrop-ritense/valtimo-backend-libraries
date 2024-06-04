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

package com.ritense.formviewmodel.autoconfigure

import com.ritense.formviewmodel.SpringContextHelper
import com.ritense.formviewmodel.commandhandling.Command
import com.ritense.formviewmodel.commandhandling.CommandDispatcher
import com.ritense.formviewmodel.commandhandling.CommandHandler
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class CommandDispatcherAutoConfiguration {

    @Bean
    fun springContextHelper() = SpringContextHelper()

    @Bean
    fun commandDispatcher(handlers: List<CommandHandler<*, *>>): CommandDispatcher {
        return CommandDispatcher().apply {
            handlers.map { this.registerCommandHandler(it as CommandHandler<Command<Any>, Any>) }
        }
    }

    @Bean
    fun commandSpringContextHelper() = SpringContextHelper()

}