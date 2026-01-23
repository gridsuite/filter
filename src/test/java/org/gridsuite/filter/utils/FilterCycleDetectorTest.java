/*
  Copyright (c) 2025, RTE (http://www.rte-france.com)
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import org.gridsuite.filter.AbstractFilterDto;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.exception.FilterCycleException;
import org.gridsuite.filter.expertfilter.ExpertFilterDto;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRuleDto;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRuleDto;
import org.gridsuite.filter.expertfilter.expertrule.FilterUuidExpertRuleDto;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.FilterCycleDetector;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mohamed BENREJEB <mohamed.ben-rejeb at rte-france.com>
 */
class FilterCycleDetectorTest {

    @Test
    void testDetectCycleBetweenExpertFilters() {
        UUID filterIdA = UUID.randomUUID();
        UUID filterIdB = UUID.randomUUID();

        AbstractExpertRuleDto ruleA = FilterUuidExpertRuleDto.builder()
                .field(FieldType.ID)
                .operator(OperatorType.IS_PART_OF)
                .values(Set.of(filterIdB.toString()))
                .build();
        CombinatorExpertRuleDto combA = CombinatorExpertRuleDto.builder()
                .combinator(CombinatorType.AND)
                .rules(List.of(ruleA))
                .build();
        ExpertFilterDto filterA = new ExpertFilterDto(filterIdA, new Date(), EquipmentType.LINE, combA);

        AbstractExpertRuleDto ruleB = FilterUuidExpertRuleDto.builder()
                .field(FieldType.ID)
                .operator(OperatorType.IS_PART_OF)
                .values(Set.of(filterIdA.toString()))
                .build();
        CombinatorExpertRuleDto combB = CombinatorExpertRuleDto.builder()
                .combinator(CombinatorType.AND)
                .rules(List.of(ruleB))
                .build();
        ExpertFilterDto filterB = new ExpertFilterDto(filterIdB, new Date(), EquipmentType.LINE, combB);

        Map<UUID, AbstractFilterDto> filters = Map.of(filterIdA, filterA, filterIdB, filterB);
        FilterLoader loader = ids -> ids.stream().map(filters::get).toList();

        FilterCycleException ex = assertThrows(FilterCycleException.class, () -> FilterCycleDetector.checkNoCycle(filterA, loader));
        assertEquals("Cycle detected in filters", ex.getMessage());
        assertThat(ex.getCycleFilterIds()).containsExactly(filterIdA, filterIdB, filterIdA);
    }

    @Test
    void testNoCycle() {
        UUID filterIdA = UUID.randomUUID();
        UUID filterIdB = UUID.randomUUID();
        UUID filterIdC = UUID.randomUUID();

        AbstractExpertRuleDto ruleA = FilterUuidExpertRuleDto.builder()
                .field(FieldType.ID)
                .operator(OperatorType.IS_PART_OF)
                .values(Set.of(filterIdB.toString()))
                .build();
        ExpertFilterDto filterA = new ExpertFilterDto(filterIdA, new Date(), EquipmentType.LINE,
                CombinatorExpertRuleDto.builder().combinator(CombinatorType.AND).rules(List.of(ruleA)).build());

        AbstractExpertRuleDto ruleB = FilterUuidExpertRuleDto.builder()
                .field(FieldType.ID)
                .operator(OperatorType.IS_PART_OF)
                .values(Set.of(filterIdC.toString()))
                .build();
        ExpertFilterDto filterB = new ExpertFilterDto(filterIdB, new Date(), EquipmentType.LINE,
                CombinatorExpertRuleDto.builder().combinator(CombinatorType.AND).rules(List.of(ruleB)).build());

        ExpertFilterDto filterC = new ExpertFilterDto(filterIdC, new Date(), EquipmentType.LINE,
                CombinatorExpertRuleDto.builder().combinator(CombinatorType.AND).rules(List.of()).build());

        Map<UUID, AbstractFilterDto> filters = Map.of(filterIdA, filterA, filterIdB, filterB, filterIdC, filterC);
        FilterLoader loader = ids -> ids.stream().map(filters::get).toList();

        try {
            FilterCycleDetector.checkNoCycle(filterA, loader);
        } catch (FilterCycleException e) {
            fail("Unexpected cycle detected");
        }
    }
}
