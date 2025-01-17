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

package com.ritense.form.web.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ritense.form.BaseIntegrationTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

class FormIoFormDefinitionResourceIntTest extends BaseIntegrationTest {

    @Inject
    private FormDefinitionResource resource;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        formDefinitionRepository.deleteAll();
        mockMvc = MockMvcBuilders
            .standaloneSetup(resource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    @Transactional
    void shouldReturn200WithForm() throws Exception {
        formDefinitionRepository.save(formDefinition(UUID.randomUUID(), "form2"));
        formDefinitionRepository.save(formDefinition(UUID.randomUUID(), "form1"));
        formDefinitionRepository.save(formDefinition(UUID.randomUUID(), "form3"));

        mockMvc.perform(
                get("/api/v1/form-definition")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].name", equalTo("form1")))
            .andExpect(jsonPath("$[1].name", equalTo("form2")))
            .andExpect(jsonPath("$[2].name", equalTo("form3")));
    }
}
