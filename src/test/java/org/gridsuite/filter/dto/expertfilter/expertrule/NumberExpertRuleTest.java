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

class NumberExpertRuleTest {

    @Test
    void toRuleCopiesFieldsAndSingleAndMultipleValues() {
        NumberExpertRule rule = NumberExpertRule.builder()
                .field(FieldType.TARGET_P)
                .operator(OperatorType.BETWEEN)
                .value(15.0)
                .values(Set.of(10.0, 20.0))
                .build();

        org.gridsuite.filter.rule.NumberExpertRule result =
                (org.gridsuite.filter.rule.NumberExpertRule) rule.toRule(null);

        assertThat(result.getField()).isEqualTo(FieldType.TARGET_P);
        assertThat(result.getOperator()).isEqualTo(OperatorType.BETWEEN);
        assertThat(result.getValue()).isEqualTo(15.0);
        assertThat(result.getValues()).containsExactlyInAnyOrder(10.0, 20.0);
    }
}
