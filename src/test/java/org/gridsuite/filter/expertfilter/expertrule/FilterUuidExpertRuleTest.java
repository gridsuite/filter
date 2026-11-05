/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter.expertrule;

import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.Filter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FilterUuidExpertRuleTest {

    @Test
    void toRuleLoadsAndConvertsReferencedFilters() {
        UUID filterId = UUID.randomUUID();
        FilterLoader filterLoader = mock(FilterLoader.class);
        AbstractFilter loadedFilter = mock(AbstractFilter.class);
        Filter convertedFilter = mock(Filter.class);
        when(filterLoader.getFilters(List.of(filterId))).thenReturn(List.of(loadedFilter));
        when(loadedFilter.toFilter(filterLoader)).thenReturn(convertedFilter);
        FilterUuidExpertRule rule = FilterUuidExpertRule.builder()
                .field(FieldType.ID)
                .operator(OperatorType.IS_PART_OF)
                .values(Set.of(filterId.toString()))
                .build();

        org.gridsuite.filter.wip.rule.FilterExpertRule result =
                (org.gridsuite.filter.wip.rule.FilterExpertRule) rule.toRule(filterLoader);

        assertThat(result.getField()).isEqualTo(FieldType.ID);
        assertThat(result.getOperator()).isEqualTo(OperatorType.IS_PART_OF);
        assertThat(result.getFilters()).containsExactly(convertedFilter);
        verify(filterLoader).getFilters(List.of(filterId));
        verify(loadedFilter).toFilter(filterLoader);
    }
}
