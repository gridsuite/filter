/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.dto.expertfilter.expertrule;

import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StringExpertRuleTest {

    @Test
    void toRuleCopiesFieldsAndValues() {
        StringExpertRule rule = StringExpertRule.builder()
                .field(FieldType.ID)
                .operator(OperatorType.IN)
                .values(Set.of("GENERATOR_1", "GENERATOR_2"))
                .build();

        org.gridsuite.filter.rule.StringExpertRule result =
                (org.gridsuite.filter.rule.StringExpertRule) rule.toRule(null);

        assertThat(result.getField()).isEqualTo(FieldType.ID);
        assertThat(result.getOperator()).isEqualTo(OperatorType.IN);
        assertThat(result.getValues()).containsExactlyInAnyOrder("GENERATOR_1", "GENERATOR_2");
    }
}
