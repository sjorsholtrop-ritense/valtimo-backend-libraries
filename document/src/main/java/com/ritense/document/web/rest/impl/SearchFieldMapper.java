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

package com.ritense.document.web.rest.impl;

import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.impl.searchfield.SearchFieldDto;

import java.util.ArrayList;
import java.util.List;

public class SearchFieldMapper {

    private SearchFieldMapper() {

    }

    public static List<SearchFieldDto> toDtoList(List<SearchField> entities) {
        List<SearchFieldDto> result = new ArrayList<>();
        entities.forEach(entity -> result.add(SearchFieldMapper.toDto(entity)));
        return result;
    }

    public static SearchFieldDto toDto(SearchField searchField) {
        return new SearchFieldDto(searchField.getKey(),
                searchField.getPath(),
                searchField.getDataType(),
                searchField.getFieldType(),
                searchField.getMatchType(),
                searchField.getTitle());
    }

    public static SearchField toEntity(SearchFieldDto searchField, int order) {
        return new SearchField(
                searchField.getKey(),
                searchField.getPath(),
                searchField.getDataType(),
                searchField.getFieldType(),
                searchField.getMatchType(),
                order,
                searchField.getTitle()
        );
    }
}