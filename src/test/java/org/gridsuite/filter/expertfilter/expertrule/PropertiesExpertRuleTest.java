/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter.expertrule;

import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesExpertRuleTest {

    @Test
    void toRuleCopiesPropertyCriteria() {
        PropertiesExpertRule rule = PropertiesExpertRule.builder()
                .field(FieldType.FREE_PROPERTIES)
                .operator(OperatorType.IN)
                .propertyName("region")
                .propertyValues(List.of("north", "south"))
                .build();

        org.gridsuite.filter.wip.rule.PropertiesExpertRule result =
                (org.gridsuite.filter.wip.rule.PropertiesExpertRule) rule.toRule(null);

        assertThat(result.getField()).isEqualTo(FieldType.FREE_PROPERTIES);
        assertThat(result.getOperator()).isEqualTo(OperatorType.IN);
        assertThat(result.getPropertyName()).isEqualTo("region");
        assertThat(result.getPropertyValues()).containsExactly("north", "south");
    }
}
