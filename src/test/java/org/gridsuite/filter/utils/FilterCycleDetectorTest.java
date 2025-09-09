/*
  Copyright (c) 2025, RTE (http://www.rte-france.com)
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.FilterUuidExpertRule;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.FilterCycleDetector;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mohamed BENREJEB <mohamed.ben-rejeb at rte-france.com>
 */
class FilterCycleDetectorTest {

    @Test
    void testDetectCycleBetweenExpertFilters() {
        UUID filterIdA = UUID.randomUUID();
        UUID filterIdB = UUID.randomUUID();

        AbstractExpertRule ruleA = FilterUuidExpertRule.builder()
                .field(FieldType.ID)
                .operator(OperatorType.IS_PART_OF)
                .values(Set.of(filterIdB.toString()))
                .build();
        CombinatorExpertRule combA = CombinatorExpertRule.builder()
                .combinator(CombinatorType.AND)
                .rules(List.of(ruleA))
                .build();
        ExpertFilter filterA = new ExpertFilter(filterIdA, new Date(), EquipmentType.LINE, combA);

        AbstractExpertRule ruleB = FilterUuidExpertRule.builder()
                .field(FieldType.ID)
                .operator(OperatorType.IS_PART_OF)
                .values(Set.of(filterIdA.toString()))
                .build();
        CombinatorExpertRule combB = CombinatorExpertRule.builder()
                .combinator(CombinatorType.AND)
                .rules(List.of(ruleB))
                .build();
        ExpertFilter filterB = new ExpertFilter(filterIdB, new Date(), EquipmentType.LINE, combB);

        Map<UUID, AbstractFilter> filters = Map.of(filterIdA, filterA, filterIdB, filterB);
        FilterLoader loader = ids -> ids.stream().map(filters::get).toList();

        PowsyblException ex = assertThrows(PowsyblException.class, () -> FilterCycleDetector.checkNoCycle(filterA, loader));
        assertEquals("Cycle detected in filters", ex.getMessage());
    }

    @Test
    void testNoCycle() {
        UUID filterIdA = UUID.randomUUID();
        UUID filterIdB = UUID.randomUUID();
        UUID filterIdC = UUID.randomUUID();

        AbstractExpertRule ruleA = FilterUuidExpertRule.builder()
                .field(FieldType.ID)
                .operator(OperatorType.IS_PART_OF)
                .values(Set.of(filterIdB.toString()))
                .build();
        ExpertFilter filterA = new ExpertFilter(filterIdA, new Date(), EquipmentType.LINE,
                CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(ruleA)).build());

        AbstractExpertRule ruleB = FilterUuidExpertRule.builder()
                .field(FieldType.ID)
                .operator(OperatorType.IS_PART_OF)
                .values(Set.of(filterIdC.toString()))
                .build();
        ExpertFilter filterB = new ExpertFilter(filterIdB, new Date(), EquipmentType.LINE,
                CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(ruleB)).build());

        ExpertFilter filterC = new ExpertFilter(filterIdC, new Date(), EquipmentType.LINE,
                CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of()).build());

        Map<UUID, AbstractFilter> filters = Map.of(filterIdA, filterA, filterIdB, filterB, filterIdC, filterC);
        FilterLoader loader = ids -> ids.stream().map(filters::get).toList();

        try {
            FilterCycleDetector.checkNoCycle(filterA, loader);
        } catch (PowsyblException e) {
            fail("Unexpected cycle detected");
        }
    }
}
