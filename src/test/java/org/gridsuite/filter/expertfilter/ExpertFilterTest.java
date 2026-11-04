/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter;

import com.powsybl.iidm.network.TopologyKind;
import org.gridsuite.filter.expertfilter.expertrule.BooleanExpertRule;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpertFilterTest {

    @Test
    void toFilterConvertsRuleAndFilterAttributes() {
        BooleanExpertRule rule = BooleanExpertRule.builder()
                .field(FieldType.VOLTAGE_REGULATOR_ON)
                .operator(OperatorType.EQUALS)
                .value(true)
                .build();
        org.gridsuite.filter.expertfilter.ExpertFilter filter = org.gridsuite.filter.expertfilter.ExpertFilter.builder()
                .equipmentType(EquipmentType.GENERATOR)
                .rules(rule)
                .topologyKind(TopologyKind.NODE_BREAKER)
                .build();

        org.gridsuite.filter.wip.ExpertFilter result =
                (org.gridsuite.filter.wip.ExpertFilter) filter.toFilter(null);

        assertThat(result.getEquipmentType()).isEqualTo(EquipmentType.GENERATOR);
        assertThat(result.getTopologyKind()).isEqualTo(TopologyKind.NODE_BREAKER);
        assertThat(result.getRule()).isInstanceOf(org.gridsuite.filter.wip.rule.BooleanExpertRule.class);
        org.gridsuite.filter.wip.rule.BooleanExpertRule convertedRule =
                (org.gridsuite.filter.wip.rule.BooleanExpertRule) result.getRule();
        assertThat(convertedRule.getField()).isEqualTo(FieldType.VOLTAGE_REGULATOR_ON);
        assertThat(convertedRule.getOperator()).isEqualTo(OperatorType.EQUALS);
        assertThat(convertedRule.getValue()).isTrue();
    }
}
