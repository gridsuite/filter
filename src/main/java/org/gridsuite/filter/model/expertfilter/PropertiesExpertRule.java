/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

import java.util.List;

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;

/**
 * @author maissa SOUISSI <maissa.souissi at rte-france.com>
 */
public class PropertiesExpertRule extends AbstractFieldExpertRule<List<String>> {

    private String propertyName;

    protected PropertiesExpertRule(
            OperatorType operatorType,
            FieldType fieldType,
            List<String> value,
            String propertyName
    ) {
        super(operatorType, fieldType, value);
        this.propertyName = propertyName;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        if (propertyName == null) {
            return false;
        }
        String propertyValue = getFieldValue(fieldType, propertyName, identifiable);
        if (propertyValue == null) {
            return false;
        }
        return switch (operatorType) {
            case IN -> value.stream()
                    .anyMatch(propertyValue::equalsIgnoreCase);
            case NOT_IN -> value.stream()
                    .noneMatch(propertyValue::equalsIgnoreCase);
            default -> throw new PowsyblException(
                    operatorType + " operator not supported with properties rule data type");
        };
    }
}
