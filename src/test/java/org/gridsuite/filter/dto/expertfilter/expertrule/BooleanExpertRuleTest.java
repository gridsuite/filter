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

import static org.assertj.core.api.Assertions.assertThat;

class BooleanExpertRuleTest {

    @Test
    void toRuleCopiesFieldsAndValue() {
        BooleanExpertRule rule = BooleanExpertRule.builder()
                .field(FieldType.VOLTAGE_REGULATOR_ON)
                .operator(OperatorType.EQUALS)
                .value(true)
                .build();

        org.gridsuite.filter.rule.BooleanExpertRule result =
                (org.gridsuite.filter.rule.BooleanExpertRule) rule.toRule(null);

        assertThat(result.getField()).isEqualTo(FieldType.VOLTAGE_REGULATOR_ON);
        assertThat(result.getOperator()).isEqualTo(OperatorType.EQUALS);
        assertThat(result.getValue()).isTrue();
    }
}
