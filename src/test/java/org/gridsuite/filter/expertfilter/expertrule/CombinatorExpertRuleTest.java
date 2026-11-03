/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter.expertrule;

import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CombinatorExpertRuleTest {

    @Test
    void toRuleRecursivelyConvertsRules() {
        CombinatorExpertRule rule = CombinatorExpertRule.builder()
                .combinator(CombinatorType.AND)
                .rules(List.of(
                        BooleanExpertRule.builder().field(FieldType.VOLTAGE_REGULATOR_ON).operator(OperatorType.EQUALS).value(true).build(),
                        StringExpertRule.builder().field(FieldType.ID).operator(OperatorType.IS).value("GENERATOR_1").build()))
                .build();

        org.gridsuite.filter.wip.rule.CombinatorExpertRule result =
                (org.gridsuite.filter.wip.rule.CombinatorExpertRule) rule.toRule(null);

        assertThat(result.getCombinator()).isEqualTo(CombinatorType.AND);
        assertThat(result.getRules()).hasSize(2);
        assertThat(result.getRules().get(0)).isInstanceOf(org.gridsuite.filter.wip.rule.BooleanExpertRule.class);
        assertThat(result.getRules().get(1)).isInstanceOf(org.gridsuite.filter.wip.rule.StringExpertRule.class);
        assertThat(((org.gridsuite.filter.wip.rule.StringExpertRule) result.getRules().get(1)).getValue()).isEqualTo("GENERATOR_1");
    }
}
